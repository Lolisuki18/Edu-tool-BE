package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_details", indexes = {
    @Index(name = "idx_report_id", columnList = "report_id"),
    @Index(name = "idx_student_id", columnList = "student_id"),
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_report_student", columnList = "report_id, student_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ReportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportDetailId;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private PeriodicReport periodicReport;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String attachmentUrl;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private LocalDateTime submittedAt;

    @UpdateTimestamp
    private LocalDateTime lastUpdatedAt;

    private String status; // DRAFT | SUBMITTED | LATE
}
