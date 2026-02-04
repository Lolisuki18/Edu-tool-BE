package com.edutool.service;

import com.edutool.dto.request.AssignProjectRequest;
import com.edutool.dto.request.EnrollStudentRequest;
import com.edutool.dto.response.EnrollmentResponse;
import com.edutool.exception.ResourceNotFoundException;
import com.edutool.exception.ValidationException;
import com.edutool.model.Course;
import com.edutool.model.CourseEnrollment;
import com.edutool.model.Project;
import com.edutool.model.Student;
import com.edutool.repository.CourseEnrollmentRepository;
import com.edutool.repository.CourseRepository;
import com.edutool.repository.ProjectRepository;
import com.edutool.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {
    
    private final CourseEnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ProjectRepository projectRepository;
    
    /**
     * Nghiệp vụ 1: Thêm sinh viên vào course
     */
    @Transactional
    public EnrollmentResponse enrollStudent(EnrollStudentRequest request) {
        // 1. Kiểm tra SV phải tồn tại
        Student student = studentRepository.findById(request.getStudentId())
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));
        
        // 2. Kiểm tra Course phải tồn tại
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));
        
        // 3. Kiểm tra không được enroll trùng
        if (enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseId(
                request.getStudentId(), request.getCourseId())) {
            throw new ValidationException("Student is already enrolled in this course");
        }
        
        // Tạo enrollment mới
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        
        CourseEnrollment savedEnrollment = enrollmentRepository.save(enrollment);
        
        return EnrollmentResponse.fromEntity(savedEnrollment);
    }
    
    /**
     * Nghiệp vụ 2: Gán sinh viên vào project (phân nhóm)
     */
    @Transactional
    public EnrollmentResponse assignStudentToProject(AssignProjectRequest request) {
        // 1. Kiểm tra Project phải tồn tại
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));
        
        CourseEnrollment enrollment;
        
        // Nếu có enrollmentId, dùng trực tiếp
        if (request.getEnrollmentId() != null) {
            enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + request.getEnrollmentId()));
            
            // Kiểm tra enrollment có thuộc course của project không
            if (!enrollment.getCourse().getCourseId().equals(project.getCourse().getCourseId())) {
                throw new ValidationException("Enrollment does not belong to the same course as the project");
            }
        } else {
            // Nếu không có enrollmentId, dùng studentId (backward compatibility)
            // 2. Kiểm tra SV phải tồn tại
            Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));
            
            // 3. Kiểm tra sinh viên đã enroll vào course của project chưa
            enrollment = enrollmentRepository
                .findByStudent_StudentIdAndCourse_CourseId(request.getStudentId(), project.getCourse().getCourseId())
                .orElseThrow(() -> new ValidationException(
                    "Student must be enrolled in the course before being assigned to a project"));
        }
        
        // 4. Kiểm tra SV chỉ có 1 project / course
        if (enrollment.getProject() != null) {
            throw new ValidationException("Student already has a project in this course. " +
                "Current project: " + enrollment.getProject().getProjectName());
        }
        
        // Gán project cho enrollment
        enrollment.setProject(project);
        enrollment.setRoleInProject(request.getRoleInProject());
        enrollment.setGroupNumber(request.getGroupNumber());
        
        CourseEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        
        return EnrollmentResponse.fromEntity(updatedEnrollment);
    }
    
    /**
     * Lấy enrollment theo ID
     */
    public EnrollmentResponse getEnrollmentById(Integer enrollmentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        return EnrollmentResponse.fromEntity(enrollment);
    }
    
    /**
     * Lấy danh sách enrollment của course
     */
    public List<EnrollmentResponse> getEnrollmentsByCourse(Integer courseId) {
        // Kiểm tra course tồn tại
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        
        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse_CourseId(courseId);
        return enrollments.stream()
            .map(EnrollmentResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách enrollment của student
     */
    public List<EnrollmentResponse> getEnrollmentsByStudent(Integer studentId) {
        // Kiểm tra student tồn tại
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentId);
        }
        
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent_StudentId(studentId);
        return enrollments.stream()
            .map(EnrollmentResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách sinh viên trong project
     */
    public List<EnrollmentResponse> getStudentsByProject(Integer projectId) {
        // Kiểm tra project tồn tại
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }
        
        List<CourseEnrollment> enrollments = enrollmentRepository.findByProject_ProjectId(projectId);
        return enrollments.stream()
            .map(EnrollmentResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Xóa sinh viên khỏi course (soft delete)
     */
    @Transactional
    public void removeStudentFromCourse(Integer studentId, Integer courseId) {
        CourseEnrollment enrollment = enrollmentRepository
            .findByStudent_StudentIdAndCourse_CourseId(studentId, courseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Enrollment not found for student " + studentId + " in course " + courseId));
        
        // Soft delete
        enrollment.setDeletedAt(java.time.LocalDateTime.now());
        enrollmentRepository.save(enrollment);
    }
    
    /**
     * Xóa sinh viên khỏi course (soft delete) - Overload với enrollmentId
     */
    @Transactional
    public void removeStudentFromCourseById(Integer enrollmentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        // Soft delete
        enrollment.setDeletedAt(java.time.LocalDateTime.now());
        enrollmentRepository.save(enrollment);
    }
    
    /**
     * Khôi phục enrollment đã xóa
     */
    @Transactional
    public EnrollmentResponse restoreEnrollment(Integer enrollmentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        if (enrollment.getDeletedAt() == null) {
            throw new ValidationException("Enrollment is not deleted");
        }
        
        enrollment.setDeletedAt(null);
        CourseEnrollment restoredEnrollment = enrollmentRepository.save(enrollment);
        
        return EnrollmentResponse.fromEntity(restoredEnrollment);
    }
    
    /**
     * Xóa vĩnh viễn enrollment (hard delete)
     */
    @Transactional
    public void permanentlyDeleteEnrollment(Integer enrollmentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        enrollmentRepository.delete(enrollment);
    }
    
    /**
     * Xóa sinh viên khỏi project (soft delete - giữ lịch sử)
     */
    @Transactional
    public EnrollmentResponse removeStudentFromProject(Integer studentId, Integer courseId) {
        CourseEnrollment enrollment = enrollmentRepository
            .findByStudent_StudentIdAndCourse_CourseId(studentId, courseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Enrollment not found for student " + studentId + " in course " + courseId));
        
        if (enrollment.getProject() == null) {
            throw new ValidationException("Student is not assigned to any project in this course");
        }
        
        if (enrollment.getRemovedFromProjectAt() != null) {
            throw new ValidationException("Student has already been removed from the project");
        }
        
        // Soft delete - giữ nguyên project, role, groupNumber để lưu lịch sử
        enrollment.setRemovedFromProjectAt(java.time.LocalDateTime.now());
        
        CourseEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.fromEntity(updatedEnrollment);
    }
    
    /**
     * Xóa sinh viên khỏi project (soft delete - giữ lịch sử) - Overload với enrollmentId
     */
    @Transactional
    public EnrollmentResponse removeStudentFromProjectById(Integer enrollmentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        if (enrollment.getProject() == null) {
            throw new ValidationException("Student is not assigned to any project in this course");
        }
        
        if (enrollment.getRemovedFromProjectAt() != null) {
            throw new ValidationException("Student has already been removed from the project");
        }
        
        // Soft delete - giữ nguyên project, role, groupNumber để lưu lịch sử
        enrollment.setRemovedFromProjectAt(java.time.LocalDateTime.now());
        
        CourseEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.fromEntity(updatedEnrollment);
    }
    
    /**
     * Khôi phục sinh viên vào lại project
     */
    @Transactional
    public EnrollmentResponse restoreStudentToProject(Integer studentId, Integer courseId) {
        CourseEnrollment enrollment = enrollmentRepository
            .findByStudent_StudentIdAndCourse_CourseId(studentId, courseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Enrollment not found for student " + studentId + " in course " + courseId));
        
        if (enrollment.getRemovedFromProjectAt() == null) {
            throw new ValidationException("Student is already active in the project");
        }
        
        // Restore
        enrollment.setRemovedFromProjectAt(null);
        
        CourseEnrollment restoredEnrollment = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.fromEntity(restoredEnrollment);
    }
    
    /**
     * Khôi phục sinh viên vào lại project - Overload với enrollmentId
     */
    @Transactional
    public EnrollmentResponse restoreStudentToProjectById(Integer enrollmentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        if (enrollment.getRemovedFromProjectAt() == null) {
            throw new ValidationException("Student is already active in the project");
        }
        
        // Restore
        enrollment.setRemovedFromProjectAt(null);
        
        CourseEnrollment restoredEnrollment = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.fromEntity(restoredEnrollment);
    }
    
    /**
     * Lấy lịch sử sinh viên đã bị xóa khỏi project
     */
    public List<EnrollmentResponse> getRemovedStudentsByProject(Integer projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }
        
        List<CourseEnrollment> removedEnrollments = enrollmentRepository.findRemovedStudentsByProject(projectId);
        return removedEnrollments.stream()
            .map(EnrollmentResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Update enrollment: thay đổi project, role, hoặc group number
     */
    @Transactional
    public EnrollmentResponse updateEnrollment(Integer studentId, Integer courseId, 
                                               com.edutool.dto.request.UpdateEnrollmentRequest request) {
        // Lấy enrollment hiện tại
        CourseEnrollment enrollment = enrollmentRepository
            .findByStudent_StudentIdAndCourse_CourseId(studentId, courseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Enrollment not found for student " + studentId + " in course " + courseId));
        
        // Nếu có projectId mới
        if (request.getProjectId() != null) {
            // Kiểm tra project tồn tại và thuộc course
            Project newProject = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));
            
            if (!newProject.getCourse().getCourseId().equals(courseId)) {
                throw new ValidationException("Project does not belong to this course");
            }
            
            enrollment.setProject(newProject);
        }
        
        // Update role và group number nếu có
        if (request.getRoleInProject() != null) {
            enrollment.setRoleInProject(request.getRoleInProject());
        }
        
        if (request.getGroupNumber() != null) {
            enrollment.setGroupNumber(request.getGroupNumber());
        }
        
        CourseEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.fromEntity(updatedEnrollment);
    }
    
    /**
     * Update enrollment: thay đổi project, role, hoặc group number - Overload với enrollmentId
     */
    @Transactional
    public EnrollmentResponse updateEnrollmentById(Integer enrollmentId, 
                                                   com.edutool.dto.request.UpdateEnrollmentRequest request) {
        // Lấy enrollment hiện tại
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        // Nếu có projectId mới
        if (request.getProjectId() != null) {
            // Kiểm tra project tồn tại và thuộc course
            Project newProject = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));
            
            if (!newProject.getCourse().getCourseId().equals(enrollment.getCourse().getCourseId())) {
                throw new ValidationException("Project does not belong to this course");
            }
            
            enrollment.setProject(newProject);
        }
        
        // Update role và group number nếu có
        if (request.getRoleInProject() != null) {
            enrollment.setRoleInProject(request.getRoleInProject());
        }
        
        if (request.getGroupNumber() != null) {
            enrollment.setGroupNumber(request.getGroupNumber());
        }
        
        CourseEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.fromEntity(updatedEnrollment);
    }
    
    /**
     * Xóa sinh viên khỏi project (nhưng vẫn giữ enrollment trong course)
     */
    // @Transactional
    // public EnrollmentResponse removeStudentFromProject(Integer studentId, Integer courseId) {
    //     CourseEnrollment enrollment = enrollmentRepository
    //         .findByStudent_StudentIdAndCourse_CourseId(studentId, courseId)
    //         .orElseThrow(() -> new ResourceNotFoundException(
    //             "Enrollment not found for student " + studentId + " in course " + courseId));
        
    //     if (enrollment.getProject() == null) {
    //         throw new ValidationException("Student is not assigned to any project in this course");
    //     }
        
    //     enrollment.setProject(null);
    //     enrollment.setRoleInProject(null);
    //     enrollment.setGroupNumber(null);
        
    //     CourseEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);
    //     return EnrollmentResponse.fromEntity(updatedEnrollment);
    // }
}
