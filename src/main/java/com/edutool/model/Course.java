package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_course_code", columnList = "courseCode", unique = true),
    @Index(name = "idx_semester_id", columnList = "semester_id"),
    @Index(name = "idx_lecturer_id", columnList = "lecturer_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer courseId;

    private String courseCode;
    private String courseName;

    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "lecturer_id")
    private Lecturer lecturer;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "course")
    private List<Project> projects;

    @OneToMany(mappedBy = "course")
    private List<CourseEnrollment> enrollments;

    @OneToMany(mappedBy = "course")
    private List<PeriodicReport> reports;
}

