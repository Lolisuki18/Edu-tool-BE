package com.edutool.dto.response;

import com.edutool.model.PeriodicReport;
import com.edutool.model.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicReportResponse {
    
    private Integer reportId;
    private Integer courseId;
    private String courseCode;
    private String courseName;
    private LocalDateTime reportFromDate;
    private LocalDateTime reportToDate;
    private LocalDateTime submitStartAt;
    private LocalDateTime submitEndAt;
    private String description;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private Long reportDetailCount; // Số lượng report details
    
    public static PeriodicReportResponse fromEntity(PeriodicReport periodicReport) {
        PeriodicReportResponse response = new PeriodicReportResponse();
        response.setReportId(periodicReport.getReportId());
        response.setReportFromDate(periodicReport.getReportFromDate());
        response.setReportToDate(periodicReport.getReportToDate());
        response.setSubmitStartAt(periodicReport.getSubmitStartAt());
        response.setSubmitEndAt(periodicReport.getSubmitEndAt());
        response.setDescription(periodicReport.getDescription());
        response.setStatus(periodicReport.getStatus());
        response.setCreatedAt(periodicReport.getCreatedAt());
        
        if (periodicReport.getCourse() != null) {
            response.setCourseId(periodicReport.getCourse().getCourseId());
            response.setCourseCode(periodicReport.getCourse().getCourseCode());
            response.setCourseName(periodicReport.getCourse().getCourseName());
        }
        
        if (periodicReport.getReportDetails() != null) {
            response.setReportDetailCount((long) periodicReport.getReportDetails().size());
        } else {
            response.setReportDetailCount(0L);
        }
        
        return response;
    }
    
    public static PeriodicReportResponse fromEntityWithDetailCount(PeriodicReport periodicReport, Long detailCount) {
        PeriodicReportResponse response = fromEntity(periodicReport);
        response.setReportDetailCount(detailCount);
        return response;
    }
}
