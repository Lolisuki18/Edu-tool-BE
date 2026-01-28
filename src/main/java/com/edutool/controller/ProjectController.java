package com.edutool.controller;

import com.edutool.dto.request.ProjectRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.ProjectResponse;
import com.edutool.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "APIs quản lý project")
public class ProjectController {
    
    private final ProjectService projectService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Tạo project mới")
    public ResponseEntity<BaseResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectRequest request) {
        
        ProjectResponse response = projectService.createProject(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.success("Project created successfully", response));
    }
    
    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy thông tin project theo ID")
    public ResponseEntity<BaseResponse<ProjectResponse>> getProjectById(
            @PathVariable Integer projectId) {
        
        ProjectResponse response = projectService.getProjectById(projectId);
        
        return ResponseEntity.ok(BaseResponse.success("Project retrieved successfully", response));
    }
    
    @GetMapping("/code/{projectCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy thông tin project theo code")
    public ResponseEntity<BaseResponse<ProjectResponse>> getProjectByCode(
            @PathVariable String projectCode) {
        
        ProjectResponse response = projectService.getProjectByCode(projectCode);
        
        return ResponseEntity.ok(BaseResponse.success("Project retrieved successfully", response));
    }
    
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy tất cả project của course")
    public ResponseEntity<BaseResponse<List<ProjectResponse>>> getProjectsByCourse(
            @PathVariable Integer courseId) {
        
        List<ProjectResponse> projects = projectService.getProjectsByCourse(courseId);
        
        return ResponseEntity.ok(BaseResponse.success(
            "Retrieved " + projects.size() + " projects", projects));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Lấy tất cả project")
    public ResponseEntity<BaseResponse<List<ProjectResponse>>> getAllProjects() {
        
        List<ProjectResponse> projects = projectService.getAllProjects();
        
        return ResponseEntity.ok(BaseResponse.success(
            "Retrieved " + projects.size() + " projects", projects));
    }
    
    @PutMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Cập nhật project")
    public ResponseEntity<BaseResponse<ProjectResponse>> updateProject(
            @PathVariable Integer projectId,
            @Valid @RequestBody ProjectRequest request) {
        
        ProjectResponse response = projectService.updateProject(projectId, request);
        
        return ResponseEntity.ok(BaseResponse.success("Project updated successfully", response));
    }
    
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xóa project (soft delete)")
    public ResponseEntity<BaseResponse<Void>> deleteProject(
            @PathVariable Integer projectId) {
        
        projectService.deleteProject(projectId);
        
        return ResponseEntity.ok(BaseResponse.success("Project deleted successfully", null));
    }
    
    @PostMapping("/restore/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Khôi phục project đã xóa")
    public ResponseEntity<BaseResponse<ProjectResponse>> restoreProject(
            @PathVariable Integer projectId) {
        
        ProjectResponse response = projectService.restoreProject(projectId);
        
        return ResponseEntity.ok(BaseResponse.success("Project restored successfully", response));
    }
    
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xem danh sách project đã xóa (chỉ admin)")
    public ResponseEntity<BaseResponse<List<ProjectResponse>>> getDeletedProjects() {
        
        List<ProjectResponse> projects = projectService.getDeletedProjects();
        
        return ResponseEntity.ok(BaseResponse.success(
            "Retrieved " + projects.size() + " deleted projects", projects));
    }
    
    @DeleteMapping("/permanent/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa vĩnh viễn project (hard delete - chỉ admin)")
    public ResponseEntity<BaseResponse<Void>> permanentlyDeleteProject(
            @PathVariable Integer projectId) {
        
        projectService.permanentlyDeleteProject(projectId);
        
        return ResponseEntity.ok(BaseResponse.success("Project permanently deleted", null));
    }
}
