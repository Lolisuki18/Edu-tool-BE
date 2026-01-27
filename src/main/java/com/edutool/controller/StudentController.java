package com.edutool.controller;

import com.edutool.dto.request.CreateStudentRequest;
import com.edutool.dto.request.UpdateStudentRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.StudentResponse;
import com.edutool.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<BaseResponse<StudentResponse>> createStudent(
            @Valid @RequestBody CreateStudentRequest request) {
        StudentResponse student = studentService.createStudent(request);
        BaseResponse<StudentResponse> response = BaseResponse.success(
                HttpStatus.CREATED.value(),
                "Student created successfully",
                student
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<BaseResponse<StudentResponse>> getStudentById(@PathVariable Integer id) {
        StudentResponse student = studentService.getStudentById(id);
        return ResponseEntity.ok(BaseResponse.success("Student retrieved successfully", student));
    }

    @GetMapping("/student-code/{studentCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<BaseResponse<StudentResponse>> getStudentByStudentCode(@PathVariable String studentCode) {
        StudentResponse student = studentService.getStudentByStudentCode(studentCode);
        return ResponseEntity.ok(BaseResponse.success("Student retrieved successfully", student));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<BaseResponse<StudentResponse>> getStudentByUserId(@PathVariable Long userId) {
        StudentResponse student = studentService.getStudentByUserId(userId);
        return ResponseEntity.ok(BaseResponse.success("Student retrieved successfully", student));
    }

    @GetMapping("/github/{githubUsername}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<BaseResponse<StudentResponse>> getStudentByGithubUsername(@PathVariable String githubUsername) {
        StudentResponse student = studentService.getStudentByGithubUsername(githubUsername);
        return ResponseEntity.ok(BaseResponse.success("Student retrieved successfully", student));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<BaseResponse<Page<StudentResponse>>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "studentId") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<StudentResponse> students = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(BaseResponse.success("Students retrieved successfully", students));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<BaseResponse<Page<StudentResponse>>> searchStudents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "studentId") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<StudentResponse> students = studentService.searchStudents(keyword, pageable);
        return ResponseEntity.ok(BaseResponse.success("Students retrieved successfully", students));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<BaseResponse<StudentResponse>> updateStudent(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateStudentRequest request) {
        StudentResponse student = studentService.updateStudent(id, request);
        return ResponseEntity.ok(BaseResponse.success("Student updated successfully", student));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteStudent(@PathVariable Integer id) {
        studentService.deleteStudent(id);
        BaseResponse<Void> response = BaseResponse.success(
                HttpStatus.NO_CONTENT.value(),
                "Student deleted successfully",
                null
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
