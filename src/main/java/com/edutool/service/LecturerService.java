package com.edutool.service;

import com.edutool.dto.request.CreateLecturerRequest;
import com.edutool.dto.request.UpdateLecturerRequest;
import com.edutool.dto.response.LecturerResponse;
import com.edutool.exception.ValidationException;
import com.edutool.model.Lecturer;
import com.edutool.model.User;
import com.edutool.model.UserStatus;
import com.edutool.repository.LecturerRepository;
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
public class LecturerService {

    private final LecturerRepository lecturerRepository;
    private final UserRepository userRepository;

    @Transactional
    public LecturerResponse createLecturer(CreateLecturerRequest request) {
        // Check if staff code already exists
        if (lecturerRepository.findByStaffCode(request.getStaffCode()).isPresent()) {
            throw new ValidationException("Staff code already exists");
        }

        // Check if user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ValidationException("User not found"));

        // Check role of the user
        if (user.getRole() != com.edutool.model.Role.LECTURER) {
            throw new ValidationException("User role must be LECTURER to create a lecturer profile");
        }

        // Check if user is already a lecturer
        if (lecturerRepository.findByUserUserId(request.getUserId()).isPresent()) {
            throw new ValidationException("User is already a lecturer");
        }

        Lecturer lecturer = new Lecturer();
        lecturer.setStaffCode(request.getStaffCode());
        lecturer.setUser(user);

        lecturer = lecturerRepository.save(lecturer);
        return convertToResponse(lecturer);
    }

    public LecturerResponse getLecturerById(Integer id) {
        Lecturer lecturer = lecturerRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Lecturer not found"));
        return convertToResponse(lecturer);
    }

    public LecturerResponse getLecturerByStaffCode(String staffCode) {
        Lecturer lecturer = lecturerRepository.findByStaffCode(staffCode)
                .orElseThrow(() -> new ValidationException("Lecturer not found"));
        return convertToResponse(lecturer);
    }

    public LecturerResponse getLecturerByUserId(Long userId) {
        Lecturer lecturer = lecturerRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ValidationException("Lecturer not found for this user"));
        return convertToResponse(lecturer);
    }

    public Page<LecturerResponse> getAllLecturers(Pageable pageable) {
        Page<Lecturer> lecturers = lecturerRepository.findAll(pageable);
        return new PageImpl<>(
                lecturers.getContent().stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()),
                pageable,
                lecturers.getTotalElements()
        );
    }

    public Page<LecturerResponse> searchLecturers(String keyword, Pageable pageable) {
        Page<Lecturer> lecturers = lecturerRepository.searchByFullNameOrStaffCode(keyword, pageable);
        return new PageImpl<>(
                lecturers.getContent().stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()),
                pageable,
                lecturers.getTotalElements()
        );
    }

    @Transactional
    public LecturerResponse updateLecturer(Integer id, UpdateLecturerRequest request) {
        Lecturer lecturer = lecturerRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Lecturer not found"));

        // Check if new staff code already exists (and it's not the same lecturer)
        if (!lecturer.getStaffCode().equals(request.getStaffCode())
                && lecturerRepository.findByStaffCode(request.getStaffCode()).isPresent()) {
            throw new ValidationException("Staff code already exists");
        }

        lecturer.setStaffCode(request.getStaffCode());
        lecturer = lecturerRepository.save(lecturer);
        return convertToResponse(lecturer);
    }

    @Transactional
    public void deleteLecturer(Integer id) {
        Lecturer lecturer = lecturerRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Lecturer not found"));
        // Find user by lecturer
        User user = lecturer.getUser();
        // Delete lecturer by changing user status to INACTIVE
        if (user != null && user.getStatus() != UserStatus.INACTIVE) {
            user.setStatus(UserStatus.INACTIVE);
            userRepository.save(user);
        }
    }

    private LecturerResponse convertToResponse(Lecturer lecturer) {
        LecturerResponse response = new LecturerResponse();
        response.setLecturerId(lecturer.getLecturerId());
        response.setStaffCode(lecturer.getStaffCode());
        response.setCreatedAt(lecturer.getCreatedAt());

        if (lecturer.getUser() != null) {
            response.setUser(convertUserToResponse(lecturer.getUser()));
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
