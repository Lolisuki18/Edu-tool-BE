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
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Search hoặc lấy thông tin project")
    public ResponseEntity<BaseResponse<?>> getProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false, defaultValue = "false") Boolean deleted) {
        
        // Nếu có search, tìm kiếm theo code hoặc name (partial match)
        if (search != null && !search.trim().isEmpty()) {
            List<ProjectResponse> projects = projectService.searchProjects(search);
            return ResponseEntity.ok(BaseResponse.success(
                "Found " + projects.size() + " projects", projects));
        }
        
        // Nếu có courseId, lấy theo course
        if (courseId != null) {
            List<ProjectResponse> projects = projectService.getProjectsByCourse(courseId);
            return ResponseEntity.ok(BaseResponse.success(
                "Retrieved " + projects.size() + " projects", projects));
        }
        
        // Nếu deleted = true, lấy projects đã xóa (chỉ admin)
        if (deleted) {
            List<ProjectResponse> projects = projectService.getDeletedProjects();
            return ResponseEntity.ok(BaseResponse.success(
                "Retrieved " + projects.size() + " deleted projects", projects));
        }
        
        // Mặc định lấy tất cả
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

    
    @PatchMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Khôi phục project đã xóa")
    public ResponseEntity<BaseResponse<ProjectResponse>> patchProject(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String action) {
        
        if ("restore".equals(action)) {
            ProjectResponse response = projectService.restoreProject(projectId);
            return ResponseEntity.ok(BaseResponse.success("Project restored successfully", response));
        }
        
        return ResponseEntity.badRequest()
            .body(BaseResponse.error("Invalid action. Supported actions: restore"));
    }
    
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xóa project (soft delete) hoặc xóa vĩnh viễn (permanent=true, chỉ admin)")
    public ResponseEntity<BaseResponse<Void>> deleteProject(
            @PathVariable Integer projectId,
            @RequestParam(required = false, defaultValue = "false") Boolean permanent) {
        
        if (permanent) {
            projectService.permanentlyDeleteProject(projectId);
            return ResponseEntity.ok(BaseResponse.success("Project permanently deleted", null));
        }
        
        projectService.deleteProject(projectId);
        return ResponseEntity.ok(BaseResponse.success("Project deleted successfully", null));
    }
}
