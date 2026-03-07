package com.edutool.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitReportResponse {

    private Integer projectId;
    private List<String> repositories;
    private PeriodInfo period;
    private String generatedAt;
    private DiagnosticInfo diagnostic;
    private List<StudentSummary> summary;
    private List<StudentRepoSummary> summaryByRepository;
    private List<WeeklyDetail> weeklyDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodInfo {
        private String since;
        private String until;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosticInfo {
        private boolean githubTokenConfigured;
        private List<RepoDiagnostic> repositories;
        private List<String> registeredGithubUsernames;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepoDiagnostic {
        private String repository;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSummary {
        private String group;
        private String studentCode;
        private String fullName;
        private String githubUsername;
        private String role;
        private int totalCommits;
        private int totalAdditions;
        private int totalDeletions;
        private double avgCommitsPerWeek;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentRepoSummary {
        private String group;
        private String studentCode;
        private String fullName;
        private String githubUsername;
        private String role;
        private String repository;
        private int totalCommits;
        private int totalAdditions;
        private int totalDeletions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyDetail {
        private String group;
        private String studentCode;
        private String fullName;
        private String githubUsername;
        private String repository;
        private int year;
        private int week;
        private int commits;
        private int additions;
        private int deletions;
    }
}
