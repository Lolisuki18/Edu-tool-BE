package com.edutool.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_student_code", columnList = "studentCode", unique = true),
    @Index(name = "idx_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_github_username", columnList = "githubUsername"),
    @Index(name = "idx_is_deleted", columnList = "isDeleted")
})
@Getter
@Setter
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer studentId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(unique = true)
    private String studentCode;

    private String githubUsername;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "student")
    private List<CourseEnrollment> enrollments;

    @OneToMany(mappedBy = "student")
    private List<CommitContribution> contributions;

    @OneToMany(mappedBy = "student")
    private List<ReportDetail> reportDetails;

    private Boolean isDeleted = false;
}

