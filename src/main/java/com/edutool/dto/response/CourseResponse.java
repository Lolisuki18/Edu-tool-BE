package com.edutool.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
public class CourseResponse {

    private Integer courseId;
    private String courseCode;
    private String courseName;
    private LocalDateTime createdAt;

    private Long semester;
    private Long lecturer;
}
