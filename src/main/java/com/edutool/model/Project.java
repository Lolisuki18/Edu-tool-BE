package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects")
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

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "project")
    private List<CourseEnrollment> enrollments;

    @OneToMany(mappedBy = "project")
    private List<GithubRepository> repositories;

    @OneToMany(mappedBy = "project")
    private List<JiraProject> jiraProjects;
}

