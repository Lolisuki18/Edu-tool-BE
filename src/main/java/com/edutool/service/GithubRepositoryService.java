package com.edutool.service;

import com.edutool.dto.request.GithubRepositoryRequest;
import com.edutool.dto.response.GroupRepositoryResponse;
import com.edutool.dto.response.GithubRepositoryResponse;
import com.edutool.dto.response.StudentSummaryResponse;
import com.edutool.exception.ResourceNotFoundException;
import com.edutool.exception.ValidationException;
import com.edutool.model.CourseEnrollment;
import com.edutool.model.GithubRepository;
import com.edutool.model.Project;
import com.edutool.repository.CourseEnrollmentRepository;
import com.edutool.repository.GithubRepositoryRepository;
import com.edutool.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GithubRepositoryService {

    private final GithubRepositoryRepository repoRepository;
    private final ProjectRepository projectRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    // Matches https://github.com/owner/repo (with optional .git suffix or trailing slash)
    private static final Pattern GITHUB_URL_PATTERN =
            Pattern.compile("(?:https?://)?github\\.com/([^/]+)/([^/?#]+?)(?:\\.git)?(?:[/?#].*)?$");

    @Transactional
    public GithubRepositoryResponse createRepository(GithubRepositoryRequest request) {
        Project project = projectRepository.findActiveById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with ID: " + request.getProjectId()));

        String normalizedUrl = normalizeUrl(request.getRepoUrl());

        if (repoRepository.existsByRepoUrlAndProject_ProjectId(normalizedUrl, request.getProjectId())) {
            throw new ValidationException("This repository URL is already submitted for this project");
        }

        String[] ownerAndRepo = parseGithubUrl(request.getRepoUrl());

        GithubRepository repo = new GithubRepository();
        repo.setRepoUrl(normalizedUrl);
        repo.setOwner(ownerAndRepo[0]);
        repo.setRepoName(request.getRepoName() != null && !request.getRepoName().isBlank()
                ? request.getRepoName() : ownerAndRepo[1]);
        repo.setProject(project);
        repo.setIsSelected(false);

        return GithubRepositoryResponse.fromEntity(repoRepository.save(repo));
    }

    public List<GithubRepositoryResponse> getRepositoriesByProject(Integer projectId) {
        projectRepository.findActiveById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with ID: " + projectId));
        return repoRepository.findByProject_ProjectId(projectId).stream()
                .map(GithubRepositoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<GithubRepositoryResponse> getRepositoriesByCourse(Integer courseId) {
        return repoRepository.findByCourseId(courseId).stream()
                .map(GithubRepositoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public GithubRepositoryResponse getRepositoryById(Integer repoId) {
        GithubRepository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Repository not found with ID: " + repoId));
        return GithubRepositoryResponse.fromEntity(repo);
    }

    @Transactional
    public GithubRepositoryResponse updateRepository(Integer repoId, GithubRepositoryRequest request) {
        GithubRepository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Repository not found with ID: " + repoId));

        String normalizedUrl = normalizeUrl(request.getRepoUrl());
        String[] ownerAndRepo = parseGithubUrl(request.getRepoUrl());

        repo.setRepoUrl(normalizedUrl);
        repo.setOwner(ownerAndRepo[0]);
        repo.setRepoName(request.getRepoName() != null && !request.getRepoName().isBlank()
                ? request.getRepoName() : ownerAndRepo[1]);

        return GithubRepositoryResponse.fromEntity(repoRepository.save(repo));
    }

    /**
     * Marks a specific repository as selected for commit tracking.
     * Automatically deselects all other repositories in the same project.
     */
    @Transactional
    public GithubRepositoryResponse selectRepository(Integer repoId) {
        GithubRepository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Repository not found with ID: " + repoId));

        // Deselect all repos in the same project first
        List<GithubRepository> siblings = repoRepository.findByProject_ProjectId(
                repo.getProject().getProjectId());
        siblings.forEach(r -> r.setIsSelected(false));
        repoRepository.saveAll(siblings);

        // Select the target repo
        repo.setIsSelected(true);
        return GithubRepositoryResponse.fromEntity(repoRepository.save(repo));
    }

    @Transactional
    public void deleteRepository(Integer repoId) {
        GithubRepository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Repository not found with ID: " + repoId));
        repoRepository.delete(repo);
    }

    /**
     * Returns all groups (projects) inside a course together with their submitted
     * repositories and member list. Used by the "Quản lý Repository" page to display
     * repos grouped by group number.
     *
     * Groups are sorted by groupNumber (null last).
     */
    public List<GroupRepositoryResponse> getGroupedByCourse(Integer courseId) {
        // Active enrollments that have a project assigned
        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse_CourseId(courseId)
                .stream()
                .filter(e -> e.getProject() != null && e.getProject().getDeletedAt() == null)
                .collect(Collectors.toList());

        // Group by project ID
        Map<Integer, List<CourseEnrollment>> byProject = new LinkedHashMap<>();
        for (CourseEnrollment e : enrollments) {
            byProject.computeIfAbsent(e.getProject().getProjectId(), k -> new ArrayList<>()).add(e);
        }

        List<GroupRepositoryResponse> result = new ArrayList<>();
        for (Map.Entry<Integer, List<CourseEnrollment>> entry : byProject.entrySet()) {
            List<CourseEnrollment> projectEnrollments = entry.getValue();
            Project project = projectEnrollments.get(0).getProject();

            // Build member list
            List<StudentSummaryResponse> members = projectEnrollments.stream()
                    .map(StudentSummaryResponse::fromEnrollment)
                    .collect(Collectors.toList());

            // Build repo list
            List<GithubRepositoryResponse> repos = repoRepository
                    .findByProject_ProjectId(project.getProjectId())
                    .stream()
                    .map(GithubRepositoryResponse::fromEntity)
                    .collect(Collectors.toList());

            // groupNumber comes from enrollment; pick first non-null value
            Integer groupNumber = projectEnrollments.stream()
                    .map(CourseEnrollment::getGroupNumber)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

            GroupRepositoryResponse grp = new GroupRepositoryResponse();
            grp.setGroupNumber(groupNumber);
            grp.setProjectId(project.getProjectId());
            grp.setProjectCode(project.getProjectCode());
            grp.setProjectName(project.getProjectName());
            grp.setProjectDescription(project.getDescription());
            grp.setProjectTechnologies(project.getTechnologies());
            grp.setMemberCount(members.size());
            grp.setRepoCount(repos.size());
            grp.setMembers(members);
            grp.setRepositories(repos);

            if (project.getCourse() != null) {
                grp.setCourseId(project.getCourse().getCourseId());
                grp.setCourseCode(project.getCourse().getCourseCode());
                grp.setCourseName(project.getCourse().getCourseName());
            }

            result.add(grp);
        }

        // Sort by groupNumber (null last)
        result.sort(Comparator.comparing(g -> g.getGroupNumber() != null ? g.getGroupNumber() : Integer.MAX_VALUE));
        return result;
    }

    // --------------- helpers ---------------

    private String[] parseGithubUrl(String repoUrl) {
        Matcher matcher = GITHUB_URL_PATTERN.matcher(repoUrl.trim());
        if (!matcher.find()) {
            throw new ValidationException(
                    "Invalid GitHub URL. Expected format: https://github.com/owner/repo");
        }
        return new String[]{matcher.group(1), matcher.group(2)};
    }

    private String normalizeUrl(String url) {
        return url.trim().replaceAll("\\.git$", "").replaceAll("/$", "");
    }
}
