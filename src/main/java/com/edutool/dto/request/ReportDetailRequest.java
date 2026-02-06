package com.edutool.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailRequest {

    @NotNull(message = "Periodic report ID is required")
    private Integer periodicReportId;

    @NotNull(message = "Project ID is required")
    private Integer projectId;

    @NotNull(message = "Student ID is required")
    private Integer studentId;

    @NotBlank(message = "Title is required")
    private String title;

    private String content;

    private String attachmentUrl;

    @DecimalMin(value = "0.0", message = "Score must be at least 0")
    @DecimalMax(value = "10.0", message = "Score must be at most 10")
    private BigDecimal score;

    private String feedback;

    private String status; // DRAFT | SUBMITTED | LATE
}
