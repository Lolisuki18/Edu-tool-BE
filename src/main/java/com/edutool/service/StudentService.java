package com.edutool.service;

import com.edutool.dto.request.CreateStudentRequest;
import com.edutool.dto.request.UpdateStudentRequest;
import com.edutool.dto.response.StudentResponse;
import com.edutool.exception.ValidationException;
import com.edutool.model.Role;
import com.edutool.model.Student;
import com.edutool.model.User;
import com.edutool.model.UserStatus;
import com.edutool.repository.StudentRepository;
import com.edutool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        // Check if student code already exists
        if (studentRepository.findByStudentCode(request.getStudentCode()).isPresent()) {
            throw new ValidationException("Student code already exists");
        }

        // Check if user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ValidationException("User not found"));

        // Check if user is already a student
        if (studentRepository.findByUserUserId(request.getUserId()).isPresent()) {
            throw new ValidationException("User is already a student");
        }

        // Check role of the user
        if (user.getRole() != Role.STUDENT) {
            throw new ValidationException("User role must be STUDENT to create a student profile");
        }

        // Check if github username already exists (if provided)
        if (request.getGithubUsername() != null && !request.getGithubUsername().isEmpty()
                && studentRepository.findByGithubUsername(request.getGithubUsername()).isPresent()) {
            throw new ValidationException("GitHub username already exists");
        }

        Student student = new Student();
        student.setStudentCode(request.getStudentCode());
        student.setUser(user);
        student.setGithubUsername(request.getGithubUsername());

        student = studentRepository.save(student);
        return convertToResponse(student);
    }

    public StudentResponse getStudentById(Integer id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Student not found"));
        return convertToResponse(student);
    }

    public StudentResponse getStudentByStudentCode(String studentCode) {
        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new ValidationException("Student not found"));
        return convertToResponse(student);
    }

    public StudentResponse getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ValidationException("Student not found for this user"));
        return convertToResponse(student);
    }

    public StudentResponse getStudentByGithubUsername(String githubUsername) {
        Student student = studentRepository.findByGithubUsername(githubUsername)
                .orElseThrow(() -> new ValidationException("Student not found for this GitHub username"));
        return convertToResponse(student);
    }

    public Page<StudentResponse> getAllStudents(Pageable pageable) {
        Page<Student> students = studentRepository.findAll(pageable);
        return new PageImpl<>(
                students.getContent().stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()),
                pageable,
                students.getTotalElements()
        );
    }
    /**
     * Search students with multiple filters (intersection of all conditions)
     * @param fullName - Fuzzy search on fullName (null = ignored)
     * @param studentCode - Fuzzy search on studentCode (null = ignored)
     * @param githubUsername - Fuzzy search on githubUsername (null = ignored)
     * @param keyword - Fuzzy search on fullName/studentCode/githubUsername/username/email (null = ignored)
     * @param pageable - Pagination parameters
     * @return Page of students matching all applied filters
     */
    public Page<StudentResponse> searchStudentsWithMultipleFilters(
            String fullName, String studentCode, String githubUsername, String keyword, Pageable pageable) {
        
        // Normalize empty strings to null
        fullName = (fullName != null && fullName.trim().isEmpty()) ? null : fullName;
        studentCode = (studentCode != null && studentCode.trim().isEmpty()) ? null : studentCode;
        githubUsername = (githubUsername != null && githubUsername.trim().isEmpty()) ? null : githubUsername;
        keyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;
        
        Page<Student> studentsPage = studentRepository.searchStudentsWithMultipleFilters(
                fullName, studentCode, githubUsername, keyword, pageable);
        
        return new PageImpl<>(
                studentsPage.getContent().stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()),
                pageable,
                studentsPage.getTotalElements()
        );
    }

    @Transactional
    public StudentResponse updateStudent(Integer id, UpdateStudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Student not found"));

        // Check if new student code already exists (and it's not the same student)
        if (!student.getStudentCode().equals(request.getStudentCode())
                && studentRepository.findByStudentCode(request.getStudentCode()).isPresent()) {
            throw new ValidationException("Student code already exists");
        }

        // Check if new github username already exists (if provided and different)
        if (request.getGithubUsername() != null && !request.getGithubUsername().isEmpty()
                && (student.getGithubUsername() == null || !student.getGithubUsername().equals(request.getGithubUsername()))
                && studentRepository.findByGithubUsername(request.getGithubUsername()).isPresent()) {
            throw new ValidationException("GitHub username already exists");
        }

        student.setStudentCode(request.getStudentCode());
        student.setGithubUsername(request.getGithubUsername());
        student = studentRepository.save(student);
        return convertToResponse(student);
    }

    @Transactional
    public void deleteStudent(Integer id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Student not found"));
        // Find user by student id
        User user = student.getUser();
        // Delete student by changing user status to INACTIVE
        if (user != null && user.getStatus() != UserStatus.INACTIVE) {
            user.setStatus(UserStatus.INACTIVE);
        }        
        userRepository.save(user);
    }

    private StudentResponse convertToResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setStudentId(student.getStudentId());
        response.setStudentCode(student.getStudentCode());
        response.setGithubUsername(student.getGithubUsername());
        response.setCreatedAt(student.getCreatedAt());

        if (student.getUser() != null) {
            response.setUser(convertUserToResponse(student.getUser()));
        }

        return response;
    }

    private com.edutool.dto.response.UserResponse convertUserToResponse(User user) {
        com.edutool.dto.response.UserResponse userResponse = new com.edutool.dto.response.UserResponse();
        userResponse.setUserId(user.getUserId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setFullName(user.getFullName());
        userResponse.setRole(user.getRole().toString());
        userResponse.setStatus(user.getStatus().toString());
        return userResponse;
    }
}
