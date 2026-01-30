package com.edutool.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicReportRequest {
    
    @NotNull(message = "Course ID is required")
    private Integer courseId;
    
    @NotNull(message = "Report from date is required")
    private LocalDateTime reportFromDate;
    
    @NotNull(message = "Report to date is required")
    private LocalDateTime reportToDate;
    
    @NotNull(message = "Submit start at is required")
    private LocalDateTime submitStartAt;
    
    @NotNull(message = "Submit end at is required")
    private LocalDateTime submitEndAt;
    
    private String description;
}
