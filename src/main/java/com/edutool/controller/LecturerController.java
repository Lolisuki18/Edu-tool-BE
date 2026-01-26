package com.edutool.controller;

import com.edutool.dto.request.CreateLecturerRequest;
import com.edutool.dto.request.UpdateLecturerRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.LecturerResponse;
import com.edutool.service.LecturerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lecturers")
@RequiredArgsConstructor
public class LecturerController {

    private final LecturerService lecturerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<BaseResponse<LecturerResponse>> createLecturer(
            @Valid @RequestBody CreateLecturerRequest request) {
        LecturerResponse lecturer = lecturerService.createLecturer(request);
        BaseResponse<LecturerResponse> response = BaseResponse.success(
                HttpStatus.CREATED.value(),
                "Lecturer created successfully",
                lecturer
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<BaseResponse<LecturerResponse>> getLecturerById(@PathVariable Integer id) {
        LecturerResponse lecturer = lecturerService.getLecturerById(id);
        return ResponseEntity.ok(BaseResponse.success("Lecturer retrieved successfully", lecturer));
    }

    @GetMapping("/staff-code/{staffCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<BaseResponse<LecturerResponse>> getLecturerByStaffCode(@PathVariable String staffCode) {
        LecturerResponse lecturer = lecturerService.getLecturerByStaffCode(staffCode);
        return ResponseEntity.ok(BaseResponse.success("Lecturer retrieved successfully", lecturer));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<BaseResponse<LecturerResponse>> getLecturerByUserId(@PathVariable Long userId) {
        LecturerResponse lecturer = lecturerService.getLecturerByUserId(userId);
        return ResponseEntity.ok(BaseResponse.success("Lecturer retrieved successfully", lecturer));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<BaseResponse<Page<LecturerResponse>>> getAllLecturers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lecturerId") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<LecturerResponse> lecturers = lecturerService.getAllLecturers(pageable);
        return ResponseEntity.ok(BaseResponse.success("Lecturers retrieved successfully", lecturers));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<BaseResponse<Page<LecturerResponse>>> searchLecturers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lecturerId") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<LecturerResponse> lecturers = lecturerService.searchLecturers(keyword, pageable);
        return ResponseEntity.ok(BaseResponse.success("Lecturers retrieved successfully", lecturers));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<BaseResponse<LecturerResponse>> updateLecturer(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateLecturerRequest request) {
        LecturerResponse lecturer = lecturerService.updateLecturer(id, request);
        return ResponseEntity.ok(BaseResponse.success("Lecturer updated successfully", lecturer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteLecturer(@PathVariable Integer id) {
        lecturerService.deleteLecturer(id);
        BaseResponse<Void> response = BaseResponse.success(
                HttpStatus.NO_CONTENT.value(),
                "Lecturer deleted successfully",
                null
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
