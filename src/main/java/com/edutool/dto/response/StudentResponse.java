package com.edutool.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentResponse {

    private Integer studentId;

    private String studentCode;

    private String githubUsername;

    private UserResponse user;

    private LocalDateTime createdAt;
}
