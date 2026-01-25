package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lecturers", indexes = {
    @Index(name = "idx_staff_code", columnList = "staffCode", unique = true),
    @Index(name = "idx_lecturer_user_id", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class Lecturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lecturerId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(unique = true)
    private String staffCode;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "lecturer")
    private List<Course> courses;
}

