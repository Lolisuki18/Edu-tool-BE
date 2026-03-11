package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commit_reports")
@Getter
@Setter
@NoArgsConstructor
public class CommitReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commitReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String storageUrl;

    @Column(length = 500)
    private String storageKey;

    @Column(length = 255)
    private String storageId;

    private LocalDate sinceDate;

    private LocalDate untilDate;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
