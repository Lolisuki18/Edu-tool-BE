package com.edutool.service;

import com.edutool.dto.request.ProjectRequest;
import com.edutool.dto.response.ProjectResponse;
import com.edutool.exception.ResourceNotFoundException;
import com.edutool.exception.ValidationException;
import com.edutool.model.Course;
import com.edutool.model.Project;
import com.edutool.repository.CourseEnrollmentRepository;
import com.edutool.repository.CourseRepository;
import com.edutool.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    
    /**
     * Tạo project mới
     */
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        // Kiểm tra project code đã tồn tại chưa
        if (projectRepository.existsByProjectCode(request.getProjectCode())) {
            throw new ValidationException("Project code already exists: " + request.getProjectCode());
        }
        
        // Kiểm tra course tồn tại
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));
        
        // Tạo project
        Project project = new Project();
        project.setProjectCode(request.getProjectCode());
        project.setProjectName(request.getProjectName());
        project.setCourse(course);
        project.setDescription(request.getDescription());
        project.setTechnologies(request.getTechnologies());
        
        Project savedProject = projectRepository.save(project);
        
        return ProjectResponse.fromEntity(savedProject);
    }
    
    /**
     * Lấy thông tin project theo ID
     */
    public ProjectResponse getProjectById(Integer projectId) {
        Project project = projectRepository.findActiveById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        
        Long memberCount = enrollmentRepository.countByProject_ProjectId(projectId);
        
        return ProjectResponse.fromEntityWithMemberCount(project, memberCount);
    }
    
    /**
     * Lấy project theo code (exact match)
     */
    public ProjectResponse getProjectByCode(String projectCode) {
        Project project = projectRepository.findByProjectCode(projectCode)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with code: " + projectCode));
        
        Long memberCount = enrollmentRepository.countByProject_ProjectId(project.getProjectId());
        
        return ProjectResponse.fromEntityWithMemberCount(project, memberCount);
    }
    
    /**
     * Search project theo code hoặc name (partial match)
     */
    public List<ProjectResponse> searchProjects(String keyword) {
        List<Project> projects = projectRepository.searchByCodeOrName(keyword);
        
        return projects.stream()
            .map(project -> {
                Long memberCount = enrollmentRepository.countByProject_ProjectId(project.getProjectId());
                return ProjectResponse.fromEntityWithMemberCount(project, memberCount);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy tất cả project của course
     */
    public List<ProjectResponse> getProjectsByCourse(Integer courseId) {
        // Kiểm tra course tồn tại
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        
        List<Project> projects = projectRepository.findByCourse_CourseId(courseId);
        
        return projects.stream()
            .map(project -> {
                Long memberCount = enrollmentRepository.countByProject_ProjectId(project.getProjectId());
                return ProjectResponse.fromEntityWithMemberCount(project, memberCount);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy tất cả project
     */
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAllActive();
        
        return projects.stream()
            .map(project -> {
                Long memberCount = enrollmentRepository.countByProject_ProjectId(project.getProjectId());
                return ProjectResponse.fromEntityWithMemberCount(project, memberCount);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Cập nhật project
     */
    @Transactional
    public ProjectResponse updateProject(Integer projectId, ProjectRequest request) {
        Project project = projectRepository.findActiveById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        
        // Kiểm tra project code trùng (nếu thay đổi)
        if (!project.getProjectCode().equals(request.getProjectCode())) {
            if (projectRepository.existsByProjectCode(request.getProjectCode())) {
                throw new ValidationException("Project code already exists: " + request.getProjectCode());
            }
            project.setProjectCode(request.getProjectCode());
        }
        
        // Kiểm tra course mới (nếu thay đổi)
        if (!project.getCourse().getCourseId().equals(request.getCourseId())) {
            Course newCourse = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));
            
            // Kiểm tra có sinh viên trong project không (tính CẢ removed để tránh mất history)
            Long memberCount = enrollmentRepository.countAllMembersByProject(projectId);
            if (memberCount > 0) {
                throw new ValidationException("Cannot change course when project has members (including removed). Remove all members first.");
            }
            
            project.setCourse(newCourse);
        }
        
        // Update các field khác
        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setTechnologies(request.getTechnologies());
        
        Project updatedProject = projectRepository.save(project);
        
        return ProjectResponse.fromEntity(updatedProject);
    }
    
    /**
     * Xóa project (soft delete)
     */
    @Transactional
    public void deleteProject(Integer projectId) {
        Project project = projectRepository.findActiveById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        
        // Kiểm tra có bất kỳ enrollment nào reference đến project không (tính CẢ removed để tránh mất history)
        Long memberCount = enrollmentRepository.countAllMembersByProject(projectId);
        if (memberCount > 0) {
            throw new ValidationException("Cannot delete project with members (including removed). Remove all members first.");
        }
        
        // Soft delete
        project.setDeletedAt(java.time.LocalDateTime.now());
        projectRepository.save(project);
    }
    
    /**
     * Khôi phục project đã xóa
     */
    @Transactional
    public ProjectResponse restoreProject(Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        
        if (project.getDeletedAt() == null) {
            throw new ValidationException("Project is not deleted");
        }
        
        // Kiểm tra project code có bị trùng với project active khác không
        if (projectRepository.existsByProjectCode(project.getProjectCode())) {
            throw new ValidationException("Cannot restore: Project code already exists in active projects");
        }
        
        // Restore
        project.setDeletedAt(null);
        Project restoredProject = projectRepository.save(project);
        
        return ProjectResponse.fromEntity(restoredProject);
    }
    
    /**
     * Lấy danh sách project đã xóa (cho admin)
     */
    public List<ProjectResponse> getDeletedProjects() {
        List<Project> deletedProjects = projectRepository.findDeleted();
        
        return deletedProjects.stream()
            .map(ProjectResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Xóa vĩnh viễn project (hard delete - chỉ admin)
     */
    @Transactional
    public void permanentlyDeleteProject(Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        
        // Kiểm tra có dữ liệu liên quan không
        Long enrollmentCount = enrollmentRepository.countByProject_ProjectId(projectId);
        if (enrollmentCount > 0) {
            throw new ValidationException("Cannot permanently delete project. It has " + enrollmentCount + 
                " enrollment records. Please remove or clean up all related data first.");
        }
        
        projectRepository.delete(project);
    }
}
