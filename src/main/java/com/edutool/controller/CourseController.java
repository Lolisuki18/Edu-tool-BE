package com.edutool.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.edutool.dto.request.CourseRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.CourseResponse;
import com.edutool.service.CourseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Course created successfully", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<CourseResponse>>> getAllCourses() {
        List<CourseResponse> courses = courseService.getAllCourses();
        return ResponseEntity.ok(BaseResponse.success("Courses retrieved successfully", courses));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<BaseResponse<CourseResponse>> getCourseById(
            @PathVariable Integer courseId) {
        CourseResponse response = courseService.getCourseById(courseId);
        return ResponseEntity.ok(BaseResponse.success("Course retrieved successfully", response));
    }

    @GetMapping("/code/{courseCode}")
    public ResponseEntity<BaseResponse<CourseResponse>> getCourseByCourseCode(
            @PathVariable String courseCode) {
        CourseResponse response = courseService.getCourseByCourseCode(courseCode);
        return ResponseEntity.ok(BaseResponse.success("Course retrieved successfully", response));
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CourseResponse>> updateCourse(
            @PathVariable Integer courseId,
            @Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.updateCourse(courseId, request);
        return ResponseEntity.ok(BaseResponse.success("Course updated successfully", response));
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteCourse(
            @PathVariable Integer courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok(BaseResponse.success("Course deleted successfully", null));
    }
}
