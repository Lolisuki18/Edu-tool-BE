package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_code", columnList = "projectCode", unique = true),
    @Index(name = "idx_project_course_id", columnList = "course_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;

    private String projectCode;
    private String projectName;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String technologies;

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "project")
    private List<CourseEnrollment> enrollments;

    @OneToMany(mappedBy = "project")
    private List<GithubRepository> repositories;

    @OneToMany(mappedBy = "project")
    private List<JiraProject> jiraProjects;

    @OneToMany(mappedBy = "project")
    private List<ReportDetail> reportDetails;
}

