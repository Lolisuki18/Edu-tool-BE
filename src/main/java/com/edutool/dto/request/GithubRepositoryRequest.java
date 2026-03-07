package com.edutool.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubRepositoryRequest {

    @NotNull(message = "Project ID is required")
    private Integer projectId;

    @NotBlank(message = "Repository URL is required")
    private String repoUrl;

    /**
     * Optional: custom display name.
     * If omitted, the repo name is extracted from the URL.
     */
    private String repoName;
}
