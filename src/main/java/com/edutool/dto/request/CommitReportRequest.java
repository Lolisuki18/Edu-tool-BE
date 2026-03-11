package com.edutool.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommitReportRequest {

    @NotNull(message = "Project ID is required")
    private Integer projectId;

    @NotBlank(message = "Storage URL is required")
    private String storageUrl;

    private String storageKey;

    private String storageId;

    private String since;

    private String until;
}
