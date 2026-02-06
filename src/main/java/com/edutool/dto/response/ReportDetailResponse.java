package com.edutool.dto.response;

import com.edutool.model.ReportDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailResponse {
    
    private Integer reportDetailId;
    private Integer periodicReportId;
    private String periodicReportDescription;
    private Integer projectId;
    private String projectName;
    private Integer studentId;
    private String studentName;
    private String studentCode;
    private String title;
    private String content;
    private String attachmentUrl;
    private BigDecimal score;
    private String feedback;
    private LocalDateTime submittedAt;
    private LocalDateTime lastUpdatedAt;
    private String status;
    
    public static ReportDetailResponse fromEntity(ReportDetail reportDetail) {
        ReportDetailResponse response = new ReportDetailResponse();
        response.setReportDetailId(reportDetail.getReportDetailId());
        response.setTitle(reportDetail.getTitle());
        response.setContent(reportDetail.getContent());
        response.setAttachmentUrl(reportDetail.getAttachmentUrl());
        response.setScore(reportDetail.getScore());
        response.setFeedback(reportDetail.getFeedback());
        response.setSubmittedAt(reportDetail.getSubmittedAt());
        response.setLastUpdatedAt(reportDetail.getLastUpdatedAt());
        response.setStatus(reportDetail.getStatus());
        
        if (reportDetail.getPeriodicReport() != null) {
            response.setPeriodicReportId(reportDetail.getPeriodicReport().getReportId());
            response.setPeriodicReportDescription(reportDetail.getPeriodicReport().getDescription());
        }
        
        if (reportDetail.getProject() != null) {
            response.setProjectId(reportDetail.getProject().getProjectId());
            response.setProjectName(reportDetail.getProject().getProjectName());
        }
        
        if (reportDetail.getStudent() != null) {
            response.setStudentId(reportDetail.getStudent().getStudentId());
            response.setStudentCode(reportDetail.getStudent().getStudentCode());
            if (reportDetail.getStudent().getUser() != null) {
                response.setStudentName(reportDetail.getStudent().getUser().getFullName());
            }
        }
        
        return response;
    }
}
