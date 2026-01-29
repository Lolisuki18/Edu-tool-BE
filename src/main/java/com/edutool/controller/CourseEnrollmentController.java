package com.edutool.controller;

import com.edutool.dto.request.AssignProjectRequest;
import com.edutool.dto.request.EnrollStudentRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.EnrollmentResponse;
import com.edutool.service.CourseEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Course Enrollment", description = "APIs quản lý enrollment và phân nhóm project")
public class CourseEnrollmentController {
    
    private final CourseEnrollmentService enrollmentService;
    
    @PostMapping("/enroll")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Thêm sinh viên vào course", 
               description = "Nghiệp vụ 1: Enroll sinh viên vào một course")
    public ResponseEntity<BaseResponse<EnrollmentResponse>> enrollStudent(
            @Valid @RequestBody EnrollStudentRequest request) {
        
        EnrollmentResponse response = enrollmentService.enrollStudent(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.success("Student enrolled successfully", response));
    }
    
    @PostMapping("/assign-project")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Gán sinh viên vào project", 
               description = "Nghiệp vụ 2: Phân nhóm - gán sinh viên vào project")
    public ResponseEntity<BaseResponse<EnrollmentResponse>> assignStudentToProject(
            @Valid @RequestBody AssignProjectRequest request) {
        
        EnrollmentResponse response = enrollmentService.assignStudentToProject(request);
        
        return ResponseEntity.ok(BaseResponse.success("Student assigned to project successfully", response));
    }
    
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy danh sách enrollment của course")
    public ResponseEntity<BaseResponse<List<EnrollmentResponse>>> getEnrollmentsByCourse(
            @PathVariable Integer courseId) {
        
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);
        
        return ResponseEntity.ok(BaseResponse.success(
            "Retrieved " + enrollments.size() + " enrollments", enrollments));
    }
    
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy danh sách enrollment của sinh viên")
    public ResponseEntity<BaseResponse<List<EnrollmentResponse>>> getEnrollmentsByStudent(
            @PathVariable Integer studentId) {
        
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        
        return ResponseEntity.ok(BaseResponse.success(
            "Retrieved " + enrollments.size() + " enrollments", enrollments));
    }
    
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy danh sách sinh viên trong project")
    public ResponseEntity<BaseResponse<List<EnrollmentResponse>>> getStudentsByProject(
            @PathVariable Integer projectId) {
        
        List<EnrollmentResponse> students = enrollmentService.getStudentsByProject(projectId);
        
        return ResponseEntity.ok(BaseResponse.success(
            "Retrieved " + students.size() + " students in project", students));
    }
    
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Cập nhật enrollment", 
               description = "Thay đổi project, role hoặc group number của sinh viên")
    public ResponseEntity<BaseResponse<EnrollmentResponse>> updateEnrollment(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId,
            @Valid @RequestBody com.edutool.dto.request.UpdateEnrollmentRequest request) {
        
        EnrollmentResponse response = enrollmentService.updateEnrollment(studentId, courseId, request);
        
        return ResponseEntity.ok(BaseResponse.success("Enrollment updated successfully", response));
    }
    
    @DeleteMapping("/remove")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xóa sinh viên khỏi course (soft delete)")
    public ResponseEntity<BaseResponse<Void>> removeStudentFromCourse(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId) {
        
        enrollmentService.removeStudentFromCourse(studentId, courseId);
        
        return ResponseEntity.ok(BaseResponse.success("Student removed from course successfully", null));
    }
    
    @PostMapping("/restore/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Khôi phục enrollment đã xóa")
    public ResponseEntity<BaseResponse<EnrollmentResponse>> restoreEnrollment(
            @PathVariable Integer enrollmentId) {
        
        EnrollmentResponse response = enrollmentService.restoreEnrollment(enrollmentId);
        
        return ResponseEntity.ok(BaseResponse.success("Enrollment restored successfully", response));
    }
    
    @DeleteMapping("/permanent/{enrollmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa vĩnh viễn enrollment (hard delete - chỉ ADMIN)")
    public ResponseEntity<BaseResponse<Void>> permanentlyDeleteEnrollment(
            @PathVariable Integer enrollmentId) {
        
        enrollmentService.permanentlyDeleteEnrollment(enrollmentId);
        
        return ResponseEntity.ok(BaseResponse.success("Enrollment permanently deleted", null));
    }
    
    @PutMapping("/remove-project")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xóa sinh viên khỏi project (soft delete - giữ lịch sử)")
    public ResponseEntity<BaseResponse<EnrollmentResponse>> removeStudentFromProject(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId) {
        
        EnrollmentResponse response = enrollmentService.removeStudentFromProject(studentId, courseId);
        
        return ResponseEntity.ok(BaseResponse.success(
            "Student removed from project but history is kept", response));
    }
    
    @PostMapping("/restore-project")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Khôi phục sinh viên vào lại project")
    public ResponseEntity<BaseResponse<EnrollmentResponse>> restoreStudentToProject(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId) {
        
        EnrollmentResponse response = enrollmentService.restoreStudentToProject(studentId, courseId);
        
        return ResponseEntity.ok(BaseResponse.success(
            "Student restored to project successfully", response));
    }
    
    @GetMapping("/project/{projectId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xem lịch sử sinh viên đã bị xóa khỏi project")
    public ResponseEntity<BaseResponse<List<EnrollmentResponse>>> getRemovedStudentsByProject(
            @PathVariable Integer projectId) {
        
        List<EnrollmentResponse> history = enrollmentService.getRemovedStudentsByProject(projectId);
        
        return ResponseEntity.ok(BaseResponse.success(
            "Retrieved " + history.size() + " removed students", history));
    }
}
