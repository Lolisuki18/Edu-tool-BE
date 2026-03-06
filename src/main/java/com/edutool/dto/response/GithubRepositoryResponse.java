package com.edutool.dto.response;

import com.edutool.model.GithubRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubRepositoryResponse {

    private Integer repoId;
    private String repoUrl;
    private String repoName;
    private String owner;
    private Boolean isSelected;
    private Integer projectId;
    private String projectName;
    private String projectCode;
    private LocalDateTime createdAt;

    public static GithubRepositoryResponse fromEntity(GithubRepository repo) {
        GithubRepositoryResponse response = new GithubRepositoryResponse();
        response.setRepoId(repo.getRepoId());
        response.setRepoUrl(repo.getRepoUrl());
        response.setRepoName(repo.getRepoName());
        response.setOwner(repo.getOwner());
        response.setIsSelected(repo.getIsSelected());
        response.setCreatedAt(repo.getCreatedAt());

        if (repo.getProject() != null) {
            response.setProjectId(repo.getProject().getProjectId());
            response.setProjectName(repo.getProject().getProjectName());
            response.setProjectCode(repo.getProject().getProjectCode());
        }

        return response;
    }
}
