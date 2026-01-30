package com.edutool.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import jakarta.persistence.criteria.CriteriaBuilder.In;


@Getter
@Setter
@NoArgsConstructor
public class CourseResponse {

    private Integer courseId;
    private String courseCode;
    private String courseName;
    private Boolean status;
    private LocalDateTime createdAt;
    
    private SemesterResponse semester;
    private LecturerResponse lecturer;
}
