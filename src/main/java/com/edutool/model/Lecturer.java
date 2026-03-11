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
@Table(name = "lecturers", indexes = {
    @Index(name = "idx_staff_code", columnList = "staffCode", unique = true),
    @Index(name = "idx_lecturer_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_is_deleted", columnList = "isDeleted")
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

    private Boolean isDeleted = false;

}

