package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "students")
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
}

