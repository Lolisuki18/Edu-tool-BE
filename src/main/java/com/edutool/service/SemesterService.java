package com.edutool.service;


import java.util.stream.Collectors;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.edutool.dto.request.SemesterRequest;
import com.edutool.dto.response.SemesterResponse;
import com.edutool.model.Semester;
import com.edutool.repository.SemesterRepository;
import com.edutool.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SemesterService {
    @Autowired
    private final SemesterRepository semesterRepository;
    
    private final UserRepository userRepository;

    @Autowired
    private final UserService userService;
   


    //tạo kì học
    @Transactional
    public SemesterResponse createSemester(SemesterRequest request) {
        try {
            // Validate user exists
            Long userId = getCurrentUserId();
            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            
            validateDates(request);

            Semester semester = new Semester();
            semester.setSemesterName(request.getName());
            semester.setStartDate(request.getStartDate());
            semester.setEndDate(request.getEndDate());
            
            return toResponse(semesterRepository.save(semester));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error creating semester: " + e.getMessage(), e);
        }
    }

    //Get semesters by ID
    public SemesterResponse getSemesterById(Long semesterId) {
            // Validate user exists
            Long userId = getCurrentUserId();
            userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

            Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found with ID: " + semesterId));
            return toResponse(semester);
            }
        //Get all semesters
    public List<SemesterResponse> getAllSemesters() {
        // Validate user exists
        Long userId = getCurrentUserId();
        userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<Semester> semesters = semesterRepository.findAll();
        return semesters.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        }
    //Update kì học
    @Transactional
    public SemesterResponse updateSemester(Long semesterId, SemesterRequest request) {
        try {
            // Validate user exists
            Long userId = getCurrentUserId();
            
            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            validateDates(request);

            Semester semester = semesterRepository.findById(semesterId)
                    .orElseThrow(() -> new IllegalArgumentException("Semester not found with ID: " + semesterId));

            semester.setSemesterName(request.getName());
            semester.setStartDate(request.getStartDate());
            semester.setEndDate(request.getEndDate());
            
            return toResponse(semesterRepository.save(semester));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating semester: " + e.getMessage(), e);
        }
    }
 
    private SemesterResponse toResponse(Semester semester) {
        SemesterResponse response = new SemesterResponse();
        Integer semesterId = semester.getSemesterId();
        response.setSemesterId(semesterId != null ? semesterId.longValue() : null);
        response.setName(semester.getSemesterName());
        response.setStartDate(semester.getStartDate());
        response.setEndDate(semester.getEndDate());
        response.setCreatedAt(semester.getCreatedAt());
        return response;
    }
    //Check startDate and endDate
    private void validateDates(SemesterRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

     protected Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserIdFromUsername(username);

    }
    

}
