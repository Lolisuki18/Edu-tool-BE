package com.edutool.service;

import com.edutool.dto.response.CommitReportResponse;
import com.edutool.exception.ResourceNotFoundException;
import com.edutool.exception.ValidationException;
import com.edutool.model.CommitContribution;
import com.edutool.model.CourseEnrollment;
import com.edutool.model.GithubRepository;
import com.edutool.model.Student;
import com.edutool.repository.CommitContributionRepository;
import com.edutool.repository.CourseEnrollmentRepository;
import com.edutool.repository.GithubRepositoryRepository;
import com.edutool.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Integrates with the GitHub public REST API (v3) to gather commit statistics
 * for all repositories submitted by a project/group, then produces a CSV report.
 *
 * <h3>Data source: /repos/{owner}/{repo}/stats/contributors</h3>
 * Returns weekly additions, deletions and commit counts per contributor — no extra
 * calls per commit needed. GitHub may respond 202 while computing stats; the service
 * retries up to 3 times with a short back-off.
 *
 * <h3>Rate limits</h3>
 * Unauthenticated: 60 req/h · Authenticated: 5 000 req/h<br>
 * Set {@code GITHUB_TOKEN} environment variable to supply a Personal Access Token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GithubApiService {

    private final GithubRepositoryRepository repoRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final ProjectRepository projectRepository;
    private final CommitContributionRepository contributionRepository;
    private final RestTemplate restTemplate;

    @Value("${github.token:}")
    private String githubToken;

    private static final String GITHUB_API_BASE = "https://api.github.com";

    // =========================================================================
    //  Public API
    // =========================================================================

    /**
     * Fetches contributor statistics from GitHub for <b>all repositories</b> of the
     * given project, persists weekly aggregates to {@code commit_contributions}, and
     * returns a fully populated CSV stream.
     *
     * <p>The CSV has two sections:
     * <ol>
     *   <li><b>Summary</b> – one row per student: total commits, total lines added / deleted</li>
     *   <li><b>Weekly Detail</b> – one row per student × repo × week with commits/additions/deletions</li>
     * </ol>
     *
     * @param projectId target project (group)
     * @param since     ISO date string yyyy-MM-dd, inclusive – weeks before this date are excluded (nullable)
     * @param until     ISO date string yyyy-MM-dd, inclusive – weeks after this date are excluded (nullable)
     */
    @Transactional
    public ByteArrayInputStream generateCommitCsvReport(
            Integer projectId, String since, String until) {

        // 1. All repos for the project (no need to pick "selected" only)
        List<GithubRepository> repos = repoRepository.findByProject_ProjectId(projectId);
        if (repos.isEmpty()) {
            throw new ValidationException(
                    "No repositories found for project ID: " + projectId
                            + ". Please submit at least one GitHub repository first.");
        }

        // 2. Active students enrolled in the project
        List<CourseEnrollment> enrollments = enrollmentRepository.findByProject_ProjectId(projectId);
        if (enrollments.isEmpty()) {
            throw new ValidationException("No students are enrolled in project ID: " + projectId);
        }

        // Map: lowercase githubUsername → StudentStats
        Map<String, StudentStats> statsByLogin = new LinkedHashMap<>();
        for (CourseEnrollment e : enrollments) {
            Student s = e.getStudent();
            if (s.getGithubUsername() != null && !s.getGithubUsername().isBlank()) {
                String key = s.getGithubUsername().toLowerCase(Locale.ROOT);
                statsByLogin.put(key, new StudentStats(s, e.getRoleInProject(), e.getGroupNumber()));
            }
        }

        // Convert since/until to epoch seconds for week filtering
        Long sinceEpoch = since != null && !since.isBlank()
                ? LocalDate.parse(since).atStartOfDay(ZoneOffset.UTC).toEpochSecond() : null;
        Long untilEpoch = until != null && !until.isBlank()
                ? LocalDate.parse(until).atTime(23, 59, 59).toInstant(ZoneOffset.UTC).getEpochSecond() : null;

        // 3. For EACH repo: try stats/contributors (has additions/deletions) then fall back to /commits
        boolean hasToken = githubToken != null && !githubToken.isBlank();
        Map<Integer, String> repoDiagnostic = new LinkedHashMap<>();

        for (GithubRepository repo : repos) {
            List<Map<String, Object>> contributors = Collections.emptyList();
            String diagnostic;

            // --- Primary: /stats/contributors (has additions + deletions, but may need 202 retries) ---
            try {
                contributors = fetchContributorStats(repo.getOwner(), repo.getRepoName());
            } catch (Exception ex) {
                String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                log.warn("stats/contributors failed for {}/{}: {}", repo.getOwner(), repo.getRepoName(), msg);
                if (msg.contains("404")) {
                    diagnostic = "(ERROR 404: repo không tìm thấy hoặc là private — "
                            + (hasToken ? "kiểm tra quyền của token" : "chưa set GITHUB_TOKEN env variable") + ")";
                } else if (msg.contains("401") || msg.contains("403")) {
                    diagnostic = "(ERROR " + (msg.contains("401") ? "401" : "403") + ": token không hợp lệ hay hết hạn — kiểm tra GITHUB_TOKEN)";
                } else {
                    diagnostic = "(ERROR: " + msg + ")";
                }
                repoDiagnostic.put(repo.getRepoId(), diagnostic);
                // Skip fallback — error is auth/access, not a compute issue
                continue;
            }

            if (!contributors.isEmpty()) {
                // Success via stats/contributors
                List<String> rawLogins = new ArrayList<>();
                for (Map<String, Object> c : contributors) {
                    String login = extractLogin(c);
                    if (login != null) rawLogins.add(login);
                }
                for (String login : rawLogins) {
                    if (!statsByLogin.containsKey(login.toLowerCase(Locale.ROOT))) {
                        log.warn("GitHub contributor '{}' in {}/{} does not match any registered student username",
                                login, repo.getOwner(), repo.getRepoName());
                    }
                }
                diagnostic = String.join(" | ", rawLogins);
                accumulateContributorStats(contributors, statsByLogin, repo, sinceEpoch, untilEpoch);
            } else {
                // 202 timeout: fallback to /commits API (immediate, no 202 issue, but no additions/deletions)
                log.info("stats/contributors still computing for {}/{} — falling back to /commits API", repo.getOwner(), repo.getRepoName());
                List<String> commitLogins = accumulateFromCommitsApi(repo, statsByLogin, sinceEpoch, untilEpoch);
                if (commitLogins.isEmpty()) {
                    diagnostic = hasToken
                            ? "(still computing — gọi lại sau 30s để có đủ additions/deletions)"
                            : "(chưa set GITHUB_TOKEN — set env variable GITHUB_TOKEN rồi restart server)";
                } else {
                    diagnostic = String.join(" | ", commitLogins) + " [via /commits API — additions/deletions=0, gọi lại sau để có đủ data]";
                }
            }

            repoDiagnostic.put(repo.getRepoId(), diagnostic);
        }

        // 4. Persist weekly aggregates
        for (GithubRepository repo : repos) {
            persistWeeklyContributions(repo, statsByLogin);
        }

        // 5. Generate CSV
        return buildCsv(repos, statsByLogin, since, until, projectId, repoDiagnostic, hasToken);
    }

    /**
     * Same logic as {@link #generateCommitCsvReport} but returns a structured
     * {@link CommitReportResponse} JSON object — easier for frontend to consume.
     */
    @Transactional
    public CommitReportResponse generateCommitJsonReport(
            Integer projectId, String since, String until) {

        List<GithubRepository> repos = repoRepository.findByProject_ProjectId(projectId);
        if (repos.isEmpty()) {
            throw new ValidationException(
                    "No repositories found for project ID: " + projectId
                            + ". Please submit at least one GitHub repository first.");
        }

        List<CourseEnrollment> enrollments = enrollmentRepository.findByProject_ProjectId(projectId);
        if (enrollments.isEmpty()) {
            throw new ValidationException("No students are enrolled in project ID: " + projectId);
        }

        Map<String, StudentStats> statsByLogin = new LinkedHashMap<>();
        for (CourseEnrollment e : enrollments) {
            Student s = e.getStudent();
            if (s.getGithubUsername() != null && !s.getGithubUsername().isBlank()) {
                String key = s.getGithubUsername().toLowerCase(Locale.ROOT);
                statsByLogin.put(key, new StudentStats(s, e.getRoleInProject(), e.getGroupNumber()));
            }
        }

        Long sinceEpoch = since != null && !since.isBlank()
                ? LocalDate.parse(since).atStartOfDay(ZoneOffset.UTC).toEpochSecond() : null;
        Long untilEpoch = until != null && !until.isBlank()
                ? LocalDate.parse(until).atTime(23, 59, 59).toInstant(ZoneOffset.UTC).getEpochSecond() : null;

        boolean hasToken = githubToken != null && !githubToken.isBlank();
        Map<Integer, String> repoDiagnostic = new LinkedHashMap<>();

        for (GithubRepository repo : repos) {
            List<Map<String, Object>> contributors = Collections.emptyList();
            String diagnostic;

            try {
                contributors = fetchContributorStats(repo.getOwner(), repo.getRepoName());
            } catch (Exception ex) {
                String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                log.warn("stats/contributors failed for {}/{}: {}", repo.getOwner(), repo.getRepoName(), msg);
                if (msg.contains("404")) {
                    diagnostic = "ERROR 404: repo không tìm thấy hoặc là private";
                } else if (msg.contains("401") || msg.contains("403")) {
                    diagnostic = "ERROR " + (msg.contains("401") ? "401" : "403") + ": token không hợp lệ hay hết hạn";
                } else {
                    diagnostic = "ERROR: " + msg;
                }
                repoDiagnostic.put(repo.getRepoId(), diagnostic);
                continue;
            }

            if (!contributors.isEmpty()) {
                List<String> rawLogins = new ArrayList<>();
                for (Map<String, Object> c : contributors) {
                    String login = extractLogin(c);
                    if (login != null) rawLogins.add(login);
                }
                diagnostic = String.join(", ", rawLogins);
                accumulateContributorStats(contributors, statsByLogin, repo, sinceEpoch, untilEpoch);
            } else {
                List<String> commitLogins = accumulateFromCommitsApi(repo, statsByLogin, sinceEpoch, untilEpoch);
                if (commitLogins.isEmpty()) {
                    diagnostic = hasToken
                            ? "still computing — gọi lại sau 30s"
                            : "chưa set GITHUB_TOKEN — set env variable rồi restart server";
                } else {
                    diagnostic = String.join(", ", commitLogins) + " [via /commits API — additions/deletions=0]";
                }
            }

            repoDiagnostic.put(repo.getRepoId(), diagnostic);
        }

        for (GithubRepository repo : repos) {
            persistWeeklyContributions(repo, statsByLogin);
        }

        return buildJsonReport(repos, statsByLogin, since, until, projectId, repoDiagnostic, hasToken);
    }

    // =========================================================================
    //  GitHub API – stats/contributors
    // =========================================================================

    /**
     * Calls {@code GET /repos/{owner}/{repo}/stats/contributors}.
     * Retries up to 3 times (2 s back-off) when GitHub returns 202 (computing).
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchContributorStats(String owner, String repo) {
        String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/stats/contributors";
        final int MAX_ATTEMPTS = 6;
        final long WAIT_MS = 5000;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                ResponseEntity<List> response = restTemplate.exchange(
                        url, HttpMethod.GET, buildRequestEntity(), List.class);

                int status = response.getStatusCode().value();
                if (status == 200 && response.getBody() != null && !response.getBody().isEmpty()) {
                    log.info("Fetched contributor stats for {}/{} ({} contributors)",
                            owner, repo, response.getBody().size());
                    return response.getBody();
                }
                if (status == 202 || (status == 200 && (response.getBody() == null || response.getBody().isEmpty()))) {
                    log.info("GitHub is computing stats for {}/{}, attempt {}/{} – waiting {} ms",
                            owner, repo, attempt, MAX_ATTEMPTS, WAIT_MS);
                    Thread.sleep(WAIT_MS);
                } else {
                    log.warn("Unexpected status {} for {}/{}", status, owner, repo);
                    break;
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                log.error("GitHub API error for {}/{} attempt {}: {}", owner, repo, attempt, ex.getMessage());
                if (attempt == MAX_ATTEMPTS) throw new RuntimeException("GitHub API unavailable for " + owner + "/" + repo, ex);
            }
        }
        log.warn("GitHub still computing stats for {}/{} after {} attempts – data will be empty", owner, repo, MAX_ATTEMPTS);
        return Collections.emptyList();
    }

    /**
     * Fallback: GET /repos/{owner}/{repo}/commits — returns immediately (no 202).
     * Counts commits per student per week. Additions/deletions are NOT available here.
     * @return list of contributor logins that were found and matched to students
     */
    @SuppressWarnings("unchecked")
    private List<String> accumulateFromCommitsApi(
            GithubRepository repo,
            Map<String, StudentStats> statsByLogin,
            Long sinceEpoch, Long untilEpoch) {

        Set<String> matchedLogins = new LinkedHashSet<>();
        String baseUrl = GITHUB_API_BASE + "/repos/" + repo.getOwner() + "/" + repo.getRepoName() + "/commits";

        for (String loginKey : statsByLogin.keySet()) {
            int page = 1;
            while (true) {
                try {
                    StringBuilder url = new StringBuilder(baseUrl)
                            .append("?author=").append(loginKey)
                            .append("&per_page=100&page=").append(page);
                    if (sinceEpoch != null) {
                        url.append("&since=").append(
                                Instant.ofEpochSecond(sinceEpoch).toString());
                    }
                    if (untilEpoch != null) {
                        url.append("&until=").append(
                                Instant.ofEpochSecond(untilEpoch).toString());
                    }

                    ResponseEntity<List> response = restTemplate.exchange(
                            url.toString(), HttpMethod.GET, buildRequestEntity(), List.class);

                    if (response.getStatusCode().value() != 200 || response.getBody() == null) break;
                    List<Map<String, Object>> commits = response.getBody();
                    if (commits.isEmpty()) break;

                    matchedLogins.add(loginKey);
                    StudentStats stats = statsByLogin.get(loginKey);

                    for (Map<String, Object> commit : commits) {
                        try {
                            Map<String, Object> commitInner = (Map<String, Object>) commit.get("commit");
                            Map<String, Object> authorInner = (Map<String, Object>) commitInner.get("author");
                            String dateStr = (String) authorInner.get("date");
                            Instant instant = Instant.parse(dateStr);
                            long epochSec = instant.getEpochSecond();

                            if (sinceEpoch != null && epochSec < sinceEpoch) continue;
                            if (untilEpoch != null && epochSec > untilEpoch) continue;

                            LocalDate d = instant.atZone(ZoneOffset.UTC).toLocalDate();
                            int isoWeek = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                            int isoYear = d.get(IsoFields.WEEK_BASED_YEAR);
                            int weekKey = isoYear * 100 + isoWeek;

                            stats.weeklyDetails
                                    .computeIfAbsent(repo.getRepoId(), k -> new TreeMap<>())
                                    .merge(weekKey, new WeekStat(1, 0, 0), WeekStat::add);
                            stats.totalCommits += 1;
                        } catch (Exception e) {
                            log.debug("Could not parse commit entry: {}", e.getMessage());
                        }
                    }

                    if (commits.size() < 100) break; // last page
                    page++;
                } catch (Exception ex) {
                    log.warn("/commits API error for {}/{} author={}: {}", repo.getOwner(), repo.getRepoName(), loginKey, ex.getMessage());
                    break;
                }
            }
        }
        return new ArrayList<>(matchedLogins);
    }

    private HttpEntity<Void> buildRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        if (githubToken != null && !githubToken.isBlank()) {
            headers.set("Authorization", "Bearer " + githubToken);
        }
        return new HttpEntity<>(headers);
    }

    // =========================================================================
    //  Accumulation – match GitHub contributors → students
    // =========================================================================

    @SuppressWarnings("unchecked")
    private void accumulateContributorStats(
            List<Map<String, Object>> contributors,
            Map<String, StudentStats> statsByLogin,
            GithubRepository repo,
            Long sinceEpoch, Long untilEpoch) {

        for (Map<String, Object> contributor : contributors) {
            String login = extractLogin(contributor);
            if (login == null) continue;

            String loginKey = login.toLowerCase(Locale.ROOT);
            if (!statsByLogin.containsKey(loginKey)) continue; // not a registered student

            StudentStats stats = statsByLogin.get(loginKey);
            List<Map<String, Object>> weeks = (List<Map<String, Object>>) contributor.get("weeks");
            if (weeks == null) continue;

            for (Map<String, Object> week : weeks) {
                int commits = toInt(week.get("c"));
                if (commits == 0) continue;

                long weekStartEpoch = toLong(week.get("w"));

                // Apply date filter
                if (sinceEpoch != null && weekStartEpoch < sinceEpoch) continue;
                if (untilEpoch != null && weekStartEpoch > untilEpoch) continue;

                int additions = toInt(week.get("a"));
                int deletions = toInt(week.get("d"));

                // Convert epoch to ISO year-week
                LocalDate weekDate = Instant.ofEpochSecond(weekStartEpoch)
                        .atZone(ZoneOffset.UTC).toLocalDate();
                int isoWeek = weekDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                int isoYear = weekDate.get(IsoFields.WEEK_BASED_YEAR);

                int weekKey = isoYear * 100 + isoWeek;

                // Accumulate per-repo weekly detail
                stats.weeklyDetails
                        .computeIfAbsent(repo.getRepoId(), k -> new TreeMap<>())
                        .merge(weekKey, new WeekStat(commits, additions, deletions), WeekStat::add);

                // Accumulate totals
                stats.totalCommits  += commits;
                stats.totalAdditions += additions;
                stats.totalDeletions += deletions;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String extractLogin(Map<String, Object> contributor) {
        try {
            Map<String, Object> author = (Map<String, Object>) contributor.get("author");
            if (author != null && author.get("login") != null) {
                return author.get("login").toString();
            }
        } catch (Exception e) {
            log.debug("Could not extract login: {}", e.getMessage());
        }
        return null;
    }

    // =========================================================================
    //  Persistence – upsert weekly aggregates into commit_contributions
    // =========================================================================

    @Transactional
    protected void persistWeeklyContributions(
            GithubRepository repo, Map<String, StudentStats> statsByLogin) {

        for (StudentStats stats : statsByLogin.values()) {
            Map<Integer, WeekStat> repoWeeks = stats.weeklyDetails.get(repo.getRepoId());
            if (repoWeeks == null) continue;

            for (Map.Entry<Integer, WeekStat> e : repoWeeks.entrySet()) {
                int yearWeek = e.getKey();
                int year = yearWeek / 100;
                int week = yearWeek % 100;
                WeekStat ws = e.getValue();

                Optional<CommitContribution> existing =
                        contributionRepository.findByStudent_StudentIdAndRepository_RepoIdAndWeekNumberAndYear(
                                stats.student.getStudentId(), repo.getRepoId(), week, year);

                CommitContribution c = existing.orElseGet(CommitContribution::new);
                c.setStudent(stats.student);
                c.setRepository(repo);
                c.setGithubAuthor(stats.student.getGithubUsername());
                c.setWeekNumber(week);
                c.setYear(year);
                c.setTotalCommits(ws.commits);
                c.setAdditions(ws.additions);
                c.setDeletions(ws.deletions);
                contributionRepository.save(c);
            }
        }
    }

    // =========================================================================
    //  CSV generation
    // =========================================================================

    private ByteArrayInputStream buildCsv(
            List<GithubRepository> repos,
            Map<String, StudentStats> statsByLogin,
            String since, String until,
            Integer projectId,
            Map<Integer, String> repoDiagnostic,
            boolean hasToken) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter w = new PrintWriter(out)) {

            // ---- Metadata ----
            w.println("# GitHub Commit Report");
            w.println("# Project ID:," + projectId);
            w.println("# Repositories:," + repos.stream()
                    .map(r -> r.getOwner() + "/" + r.getRepoName())
                    .collect(Collectors.joining(" | ")));
            w.println("# Period:," + (since != null ? since : "All") + " to " + (until != null ? until : "Now"));
            w.println("# Generated:," + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            w.println();

            // ---- Diagnostic ----
            w.println("=== Diagnostic: GitHub Contributors Found ===");
            w.println("# GitHub Token configured: " + (hasToken ? "YES" : "NO — set GITHUB_TOKEN env variable"));
            w.println("Repository,GitHub Logins Found / Status");
            for (GithubRepository repo : repos) {
                String info = repoDiagnostic.getOrDefault(repo.getRepoId(), "(not fetched)");
                w.println(escapeCsv(repo.getOwner() + "/" + repo.getRepoName()) + "," + escapeCsv(info));
            }
            w.println("# Registered student GitHub usernames:," +
                    statsByLogin.keySet().stream().collect(Collectors.joining(" | ")));
            w.println();

            // ---- Summary ----
            w.println("=== Summary (Tổng hợp) ===");
            w.println("Group,Student Code,Full Name,GitHub Username,Role,Total Commits,Total Additions,Total Deletions,Avg Commits/Week");

            List<StudentStats> sorted = statsByLogin.values().stream()
                    .sorted(Comparator.comparing(s -> s.student.getStudentCode()))
                    .collect(Collectors.toList());

            for (StudentStats s : sorted) {
                long weekCount = s.weeklyDetails.values().stream()
                        .mapToLong(Map::size).sum();
                double avgCommitsPerWeek = weekCount > 0
                        ? (double) s.totalCommits / weekCount : 0.0;

                w.println(String.join(",",
                        escapeCsv(s.groupNumber != null ? "Group " + s.groupNumber : ""),
                        escapeCsv(s.student.getStudentCode()),
                        escapeCsv(s.student.getUser().getFullName()),
                        escapeCsv(s.student.getGithubUsername()),
                        escapeCsv(s.roleInProject != null ? s.roleInProject : ""),
                        String.valueOf(s.totalCommits),
                        String.valueOf(s.totalAdditions),
                        String.valueOf(s.totalDeletions),
                        String.format("%.2f", avgCommitsPerWeek)
                ));
            }

            w.println();

            // ---- Weekly Detail ----
            w.println("=== Weekly Detail (Chi tiết theo tuần) ===");
            w.println("Group,Student Code,Full Name,GitHub Username,Repository,Year,Week,Commits,Additions,Deletions");

            // Build lookup: repoId → "owner/repoName"
            Map<Integer, String> repoNames = repos.stream()
                    .collect(Collectors.toMap(GithubRepository::getRepoId,
                            r -> r.getOwner() + "/" + r.getRepoName()));

            for (StudentStats s : sorted) {
                for (Map.Entry<Integer, TreeMap<Integer, WeekStat>> repoEntry : s.weeklyDetails.entrySet()) {
                    String repoLabel = repoNames.getOrDefault(repoEntry.getKey(), "repo-" + repoEntry.getKey());
                    for (Map.Entry<Integer, WeekStat> weekEntry : repoEntry.getValue().entrySet()) {
                        int year = weekEntry.getKey() / 100;
                        int week = weekEntry.getKey() % 100;
                        WeekStat ws = weekEntry.getValue();
                        w.println(String.join(",",
                                escapeCsv(s.groupNumber != null ? "Group " + s.groupNumber : ""),
                                escapeCsv(s.student.getStudentCode()),
                                escapeCsv(s.student.getUser().getFullName()),
                                escapeCsv(s.student.getGithubUsername()),
                                escapeCsv(repoLabel),
                                String.valueOf(year),
                                String.valueOf(week),
                                String.valueOf(ws.commits),
                                String.valueOf(ws.additions),
                                String.valueOf(ws.deletions)
                        ));
                    }
                }
            }

            w.flush();
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    private CommitReportResponse buildJsonReport(
            List<GithubRepository> repos,
            Map<String, StudentStats> statsByLogin,
            String since, String until,
            Integer projectId,
            Map<Integer, String> repoDiagnostic,
            boolean hasToken) {

        // Diagnostic repos
        List<CommitReportResponse.RepoDiagnostic> diagRepos = new ArrayList<>();
        for (GithubRepository repo : repos) {
            String info = repoDiagnostic.getOrDefault(repo.getRepoId(), "(not fetched)");
            diagRepos.add(CommitReportResponse.RepoDiagnostic.builder()
                    .repository(repo.getOwner() + "/" + repo.getRepoName())
                    .status(info)
                    .build());
        }

        CommitReportResponse.DiagnosticInfo diagnostic = CommitReportResponse.DiagnosticInfo.builder()
                .githubTokenConfigured(hasToken)
                .repositories(diagRepos)
                .registeredGithubUsernames(new ArrayList<>(statsByLogin.keySet()))
                .build();

        // Summary
        List<StudentStats> sorted = statsByLogin.values().stream()
                .sorted(Comparator.comparing(s -> s.student.getStudentCode()))
                .collect(Collectors.toList());

        List<CommitReportResponse.StudentSummary> summaryList = new ArrayList<>();
        for (StudentStats s : sorted) {
            long weekCount = s.weeklyDetails.values().stream()
                    .mapToLong(Map::size).sum();
            double avgCommitsPerWeek = weekCount > 0
                    ? (double) s.totalCommits / weekCount : 0.0;

            summaryList.add(CommitReportResponse.StudentSummary.builder()
                    .group(s.groupNumber != null ? "Group " + s.groupNumber : "")
                    .studentCode(s.student.getStudentCode())
                    .fullName(s.student.getUser().getFullName())
                    .githubUsername(s.student.getGithubUsername())
                    .role(s.roleInProject != null ? s.roleInProject : "")
                    .totalCommits(s.totalCommits)
                    .totalAdditions(s.totalAdditions)
                    .totalDeletions(s.totalDeletions)
                    .avgCommitsPerWeek(Math.round(avgCommitsPerWeek * 100.0) / 100.0)
                    .build());
        }

        // Weekly details
        Map<Integer, String> repoNames = repos.stream()
                .collect(Collectors.toMap(GithubRepository::getRepoId,
                        r -> r.getOwner() + "/" + r.getRepoName()));

        List<CommitReportResponse.WeeklyDetail> weeklyList = new ArrayList<>();
        List<CommitReportResponse.StudentRepoSummary> summaryByRepo = new ArrayList<>();

        for (StudentStats s : sorted) {
            for (Map.Entry<Integer, TreeMap<Integer, WeekStat>> repoEntry : s.weeklyDetails.entrySet()) {
                String repoLabel = repoNames.getOrDefault(repoEntry.getKey(), "repo-" + repoEntry.getKey());

                int repoCommits = 0, repoAdditions = 0, repoDeletions = 0;
                for (Map.Entry<Integer, WeekStat> weekEntry : repoEntry.getValue().entrySet()) {
                    int year = weekEntry.getKey() / 100;
                    int week = weekEntry.getKey() % 100;
                    WeekStat ws = weekEntry.getValue();
                    weeklyList.add(CommitReportResponse.WeeklyDetail.builder()
                            .group(s.groupNumber != null ? "Group " + s.groupNumber : "")
                            .studentCode(s.student.getStudentCode())
                            .fullName(s.student.getUser().getFullName())
                            .githubUsername(s.student.getGithubUsername())
                            .repository(repoLabel)
                            .year(year)
                            .week(week)
                            .commits(ws.commits)
                            .additions(ws.additions)
                            .deletions(ws.deletions)
                            .build());
                    repoCommits    += ws.commits;
                    repoAdditions  += ws.additions;
                    repoDeletions  += ws.deletions;
                }

                summaryByRepo.add(CommitReportResponse.StudentRepoSummary.builder()
                        .group(s.groupNumber != null ? "Group " + s.groupNumber : "")
                        .studentCode(s.student.getStudentCode())
                        .fullName(s.student.getUser().getFullName())
                        .githubUsername(s.student.getGithubUsername())
                        .role(s.roleInProject != null ? s.roleInProject : "")
                        .repository(repoLabel)
                        .totalCommits(repoCommits)
                        .totalAdditions(repoAdditions)
                        .totalDeletions(repoDeletions)
                        .build());
            }
        }

        return CommitReportResponse.builder()
                .projectId(projectId)
                .repositories(repos.stream()
                        .map(r -> r.getOwner() + "/" + r.getRepoName())
                        .collect(Collectors.toList()))
                .period(CommitReportResponse.PeriodInfo.builder()
                        .since(since != null ? since : "All")
                        .until(until != null ? until : "Now")
                        .build())
                .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .diagnostic(diagnostic)
                .summary(summaryList)
                .summaryByRepository(summaryByRepo)
                .weeklyDetails(weeklyList)
                .build();
    }

    // =========================================================================
    //  Utilities
    // =========================================================================

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private int toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try { return Integer.parseInt(obj.toString()); } catch (Exception e) { return 0; }
    }

    private long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try { return Long.parseLong(obj.toString()); } catch (Exception e) { return 0L; }
    }

    // =========================================================================
    //  Inner helper classes
    // =========================================================================

    private static class WeekStat {
        int commits;
        int additions;
        int deletions;

        WeekStat(int commits, int additions, int deletions) {
            this.commits = commits;
            this.additions = additions;
            this.deletions = deletions;
        }

        static WeekStat add(WeekStat a, WeekStat b) {
            return new WeekStat(a.commits + b.commits, a.additions + b.additions, a.deletions + b.deletions);
        }
    }

    private static class StudentStats {
        final Student student;
        final String roleInProject;
        final Integer groupNumber;
        int totalCommits = 0;
        int totalAdditions = 0;
        int totalDeletions = 0;
        // repoId → (yearWeek → WeekStat)
        final Map<Integer, TreeMap<Integer, WeekStat>> weeklyDetails = new LinkedHashMap<>();

        StudentStats(Student student, String roleInProject, Integer groupNumber) {
            this.student = student;
            this.roleInProject = roleInProject;
            this.groupNumber = groupNumber;
        }
    }
}

