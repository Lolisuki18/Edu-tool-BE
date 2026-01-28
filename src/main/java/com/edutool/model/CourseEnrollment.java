package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_enrollments", 
    indexes = {
        @Index(name = "idx_enrollment_student_id", columnList = "student_id"),
        @Index(name = "idx_enrollment_course_id", columnList = "course_id"),
        @Index(name = "idx_enrollment_project_id", columnList = "project_id"),
        @Index(name = "idx_student_course", columnList = "student_id, course_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_enrollment_student_course", 
                         columnNames = {"student_id", "course_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer enrollmentId;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    private String roleInProject;
    private Integer groupNumber;

    @CreationTimestamp
    private LocalDateTime enrolledAt;
    
    private LocalDateTime deletedAt;
    
    private LocalDateTime removedFromProjectAt; // Soft delete khỏi project nhưng vẫn trong course
}

