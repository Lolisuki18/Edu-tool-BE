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
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Thêm sinh viên vào course", 
               description = "Nghiệp vụ 1: Enroll sinh viên vào một course")
    public ResponseEntity<BaseResponse<EnrollmentResponse>> createEnrollment(
            @Valid @RequestBody EnrollStudentRequest request) {
        
        EnrollmentResponse response = enrollmentService.enrollStudent(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.success("Student enrolled successfully", response));
    }
    
    @GetMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy thông tin enrollment theo ID")
    public ResponseEntity<BaseResponse<EnrollmentResponse>> getEnrollmentById(
            @PathVariable Integer enrollmentId) {
        
        EnrollmentResponse response = enrollmentService.getEnrollmentById(enrollmentId);
        return ResponseEntity.ok(BaseResponse.success("Enrollment retrieved successfully", response));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy danh sách enrollments theo điều kiện: courseId, studentId, hoặc projectId")
    public ResponseEntity<BaseResponse<List<EnrollmentResponse>>> getEnrollments(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) Integer projectId) {
        
        List<EnrollmentResponse> enrollments;
        
        if (courseId != null) {
            enrollments = enrollmentService.getEnrollmentsByCourse(courseId);
        } else if (studentId != null) {
            enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        } else if (projectId != null) {
            enrollments = enrollmentService.getStudentsByProject(projectId);
        } else {
            return ResponseEntity.badRequest()
                .body(BaseResponse.error("Please provide courseId, studentId, or projectId"));
        }
        
        return ResponseEntity.ok(BaseResponse.success(
            "Retrieved " + enrollments.size() + " enrollments", enrollments));
    }
    
    @PutMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Cập nhật enrollment", 
               description = "Thay đổi project, role hoặc group number của sinh viên")
    public ResponseEntity<BaseResponse<EnrollmentResponse>> updateEnrollment(
            @PathVariable Integer enrollmentId,
            @Valid @RequestBody com.edutool.dto.request.UpdateEnrollmentRequest request) {
        
        EnrollmentResponse response = enrollmentService.updateEnrollmentById(enrollmentId, request);
        
        return ResponseEntity.ok(BaseResponse.success("Enrollment updated successfully", response));
    }
    
    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xóa enrollment (soft delete mặc định, hoặc permanent=true để xóa vĩnh viễn)")
    public ResponseEntity<BaseResponse<Void>> deleteEnrollment(
            @PathVariable Integer enrollmentId,
            @RequestParam(required = false, defaultValue = "false") Boolean permanent) {
        
        if (permanent) {
            enrollmentService.permanentlyDeleteEnrollment(enrollmentId);
            return ResponseEntity.ok(BaseResponse.success("Enrollment permanently deleted", null));
        }
        
        enrollmentService.removeStudentFromCourseById(enrollmentId);
        return ResponseEntity.ok(BaseResponse.success("Student removed from course successfully", null));
    }
    
    @PatchMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Thực hiện actions trên enrollment: restore, remove-from-project, restore-to-project, completely-remove-from-project")
    public ResponseEntity<BaseResponse<EnrollmentResponse>> patchEnrollment(
            @PathVariable Integer enrollmentId,
            @RequestParam String action) {
        
        EnrollmentResponse response;
        
        switch (action) {
            case "restore":
                response = enrollmentService.restoreEnrollment(enrollmentId);
                return ResponseEntity.ok(BaseResponse.success("Enrollment restored successfully", response));
            
            case "remove-from-project":
                response = enrollmentService.removeStudentFromProjectById(enrollmentId);
                return ResponseEntity.ok(BaseResponse.success(
                    "Student removed from project but history is kept", response));
            
            case "restore-to-project":
                response = enrollmentService.restoreStudentToProjectById(enrollmentId);
                return ResponseEntity.ok(BaseResponse.success(
                    "Student restored to project successfully", response));
            
            case "completely-remove-from-project":
                response = enrollmentService.completelyRemoveFromProjectById(enrollmentId);
                return ResponseEntity.ok(BaseResponse.success(
                    "Student completely removed from project. Can now be assigned to new project.", response));
            
            default:
                return ResponseEntity.badRequest()
                    .body(BaseResponse.error("Invalid action. Supported actions: restore, remove-from-project, restore-to-project, completely-remove-from-project"));
        }
    }
    
    @PutMapping("/{enrollmentId}/project")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Gán sinh viên vào project", 
               description = "Nghiệp vụ 2: Phân nhóm - gán sinh viên vào project")
    public ResponseEntity<BaseResponse<EnrollmentResponse>> assignStudentToProject(
            @PathVariable Integer enrollmentId,
            @RequestParam Integer projectId) {
        
        AssignProjectRequest request = new AssignProjectRequest();
        request.setEnrollmentId(enrollmentId);
        request.setProjectId(projectId);
        
        EnrollmentResponse response = enrollmentService.assignStudentToProject(request);
        
        return ResponseEntity.ok(BaseResponse.success("Student assigned to project successfully", response));
    }
    
    @GetMapping("/projects/{projectId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xem lịch sử sinh viên đã bị xóa khỏi project")
    public ResponseEntity<BaseResponse<List<EnrollmentResponse>>> getRemovedStudentsByProject(
            @PathVariable Integer projectId) {
        
        List<EnrollmentResponse> history = enrollmentService.getRemovedStudentsByProject(projectId);
        
        return ResponseEntity.ok(BaseResponse.success(
            "Retrieved " + history.size() + " removed students", history));
    }
}
