package com.edutool.dto.response;

import com.edutool.model.CommitReport;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CommitReportUrlResponse {

    private Integer commitReportId;
    private Integer projectId;
    private String storageUrl;
    private String storageKey;
    private String storageId;
    private LocalDate sinceDate;
    private LocalDate untilDate;
    private LocalDateTime createdAt;

    public static CommitReportUrlResponse fromEntity(CommitReport entity) {
        CommitReportUrlResponse res = new CommitReportUrlResponse();
        res.setCommitReportId(entity.getCommitReportId());
        res.setProjectId(entity.getProject().getProjectId());
        res.setStorageUrl(entity.getStorageUrl());
        res.setStorageKey(entity.getStorageKey());
        res.setStorageId(entity.getStorageId());
        res.setSinceDate(entity.getSinceDate());
        res.setUntilDate(entity.getUntilDate());
        res.setCreatedAt(entity.getCreatedAt());
        return res;
    }
}
