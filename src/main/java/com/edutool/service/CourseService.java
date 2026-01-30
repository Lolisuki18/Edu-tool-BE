package com.edutool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.edutool.dto.request.CourseRequest;
import com.edutool.dto.response.CourseResponse;
import com.edutool.dto.response.LecturerResponse;
import com.edutool.dto.response.SemesterResponse;
import com.edutool.dto.response.UserResponse;
import com.edutool.exception.ValidationException;
import com.edutool.model.Course;
import com.edutool.repository.CourseRepository;
import com.edutool.repository.LecturerRepository;
import com.edutool.repository.SemesterRepository;
import com.edutool.repository.UserRepository;

import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final LecturerRepository lecturerRepository;
    // Create
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        try {
            // Validate user exists
             Long userId = getCurrentUserId();
            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

            if(semesterRepository.findById(request.getSemesterId()).isEmpty()) {
                throw new ValidationException("Semester not found with ID: " + request.getSemesterId());
            }
            if (courseRepository.existsByCourseCode(request.getCourseCode())) {
                throw new ValidationException("Course code already exists: " + request.getCourseCode());
            }
            
            Course course = new Course();
            course.setCourseCode(request.getCourseCode());
            course.setCourseName(request.getCourseName());
            course.setSemester(semesterRepository.findById(request.getSemesterId()).orElse(null));
            course.setStatus(true);
            course.setLecturer(lecturerRepository.findById(request.getLecturerId()).orElse(null));
            Course savedCourse = courseRepository.save(course);
            return convertToResponse(savedCourse);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Error creating course: " + e.getMessage());
        }
    }

    // Read - Get all courses
    public List<CourseResponse> getAllCourses() {
        try {
            Long userId = getCurrentUserId();
            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            return courseRepository.findAll().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Error retrieving courses: " + e.getMessage());
        }
    }

    // Read - Get course by ID
    public CourseResponse getCourseById(Integer courseId) {
        try {
            Long userId = getCurrentUserId();
            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ValidationException("Course not found with ID: " + courseId));
            return convertToResponse(course);
        } catch (ValidationException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Error retrieving course: " + e.getMessage());
        }
    }

    // Read - Get course by code
    public CourseResponse getCourseByCourseCode(String courseCode) {
        try {
             Long userId = getCurrentUserId();
            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            Course course = courseRepository.findByCourseCode(courseCode)
                    .orElseThrow(() -> new ValidationException("Course not found with code: " + courseCode));
            return convertToResponse(course);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Error retrieving course by code: " + e.getMessage());
        }
    }

    // Update
    @Transactional
    public CourseResponse updateCourse(Integer courseId, CourseRequest request) {
        try {
            Long userId = getCurrentUserId();
            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            if(request.getStatus() == false) {
                throw new ValidationException("Cannot set course status to false during update.");
            }
            if(semesterRepository.findById(request.getSemesterId()).isEmpty()) {
                throw new ValidationException("Semester not found with ID: " + request.getSemesterId());

            }
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ValidationException("Course not found with ID: " + courseId));
            
            // Check if new course code already exists for another course
            if (!course.getCourseCode().equals(request.getCourseCode()) && 
                courseRepository.existsByCourseCode(request.getCourseCode())) {
                throw new ValidationException("Course code already exists: " + request.getCourseCode());
            }
            
            course.setCourseCode(request.getCourseCode());
            course.setCourseName(request.getCourseName());
            course.setSemester(semesterRepository.findById(request.getSemesterId()).orElse(null));
            course.setStatus(true);
            Course updatedCourse = courseRepository.save(course);
            return convertToResponse(updatedCourse);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Error updating course: " + e.getMessage());
        }
    }

    // Delete (Soft delete by setting status to false)
    @Transactional
    public void deleteCourse(Integer courseId) {
        try {
            Long userId = getCurrentUserId();
            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ValidationException("Course not found with ID: " + courseId));
            course.setStatus(false);
            courseRepository.save(course);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Error deleting course: " + e.getMessage());
        }
    }

    // Helper method to convert Course entity to CourseResponse
    private CourseResponse convertToResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setCourseId(course.getCourseId());
        response.setCourseCode(course.getCourseCode());
        response.setCourseName(course.getCourseName());
        response.setStatus(course.getStatus());
        response.setCreatedAt(course.getCreatedAt());
        
        if (course.getSemester() != null) {
            response.setSemester(toSemesterResponse(course.getSemester()));
        }
        if (course.getLecturer() != null) {
            response.setLecturer(toLecturerResponse(course.getLecturer()));
        }
        
        return response;
    }
    private SemesterResponse toSemesterResponse(com.edutool.model.Semester semester) {
        SemesterResponse response = new SemesterResponse();
        response.setSemesterId(semester.getSemesterId());
        response.setName(semester.getSemesterName());
        response.setStartDate(semester.getStartDate());
        response.setEndDate(semester.getEndDate());
        response.setStatus(semester.getStatus());
        response.setCreatedAt(semester.getCreatedAt());
        return response;
    }

    private LecturerResponse toLecturerResponse(com.edutool.model.Lecturer lecturer) {
        LecturerResponse response = new LecturerResponse();
        response.setLecturerId(lecturer.getLecturerId());
        response.setStaffCode(lecturer.getStaffCode());
        response.setCreatedAt(lecturer.getCreatedAt());
        if (lecturer.getUser() != null) {
            response.setUser(toUserResponse(lecturer.getUser()));
        }
        return response;
    }

    private UserResponse toUserResponse(com.edutool.model.User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole() != null ? user.getRole().name() : null);
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        return response;
    }
     protected Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserIdFromUsername(username);

    }

}
