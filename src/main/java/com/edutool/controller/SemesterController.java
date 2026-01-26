package com.edutool.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.edutool.dto.request.SemesterRequest;
import com.edutool.dto.response.SemesterResponse;
import com.edutool.dto.response.BaseResponse;
import com.edutool.service.SemesterService;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    //Create new semester

    @PostMapping
    
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SemesterResponse>> createSemester(
            @Valid @RequestBody SemesterRequest request) {
        SemesterResponse response = semesterService.createSemester(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }
    //Update ssemester
    @PutMapping("/{semesterId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SemesterResponse>> updateSemester(
            @PathVariable Long semesterId,
            @Valid @RequestBody SemesterRequest request) {
        SemesterResponse response = semesterService.updateSemester(semesterId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    
    //Get semester by ID
    @GetMapping("/{semesterId}")
    public ResponseEntity<BaseResponse<SemesterResponse>> getSemesterById(
            @PathVariable Long semesterId) {
        SemesterResponse response = semesterService.getSemesterById(semesterId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    //Get all semesters
    @GetMapping
    public ResponseEntity<BaseResponse<List<SemesterResponse>>> getAllSemesters() {
        List<SemesterResponse> response = semesterService.getAllSemesters();
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
