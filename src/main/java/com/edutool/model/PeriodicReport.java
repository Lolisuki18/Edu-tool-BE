package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "periodic_reports", indexes = {
    @Index(name = "idx_course_id", columnList = "course_id"),
    @Index(name = "idx_report_dates", columnList = "report_from_date, report_to_date"),
    @Index(name = "idx_submit_dates", columnList = "submit_start_at, submit_end_at")
})
@Getter
@Setter
@NoArgsConstructor
public class PeriodicReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    private LocalDateTime reportFromDate;
    private LocalDateTime reportToDate;
    private LocalDateTime submitStartAt;
    private LocalDateTime submitEndAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "periodicReport")
    private List<ReportDetail> reportDetails;
}

