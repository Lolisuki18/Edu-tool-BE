package com.edutool.controller;

import com.edutool.dto.request.CreateLecturerRequest;
import com.edutool.dto.request.UpdateLecturerRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.LecturerResponse;
import com.edutool.dto.response.StudentResponse;
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

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<BaseResponse<LecturerResponse>> getLecturerByUserId(@PathVariable Long userId) {
        LecturerResponse lecturer = lecturerService.getLecturerByUserId(userId);
        return ResponseEntity.ok(BaseResponse.success("Lecturer retrieved successfully", lecturer));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<?> searchLecturers(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String staffCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lecturerId") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        
        // ========== VALIDATION: Pagination Limits ==========
        final int MAX_PAGE_SIZE = 100;
        final int MIN_PAGE_SIZE = 1;
        
        if (size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("Invalid size. Minimum: " + MIN_PAGE_SIZE + ", Maximum: " + MAX_PAGE_SIZE + ". Received: " + size));
        }
        
        if (page < 0) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("Invalid page. Must be >= 0. Received: " + page));
        }
        
        // ========== VALIDATION: Whitelist sortBy Fields ==========
        final String[] ALLOWED_SORT_FIELDS = {"lecturerId", "staffCode", "fullName", "createdAt"};
        boolean isValidSortField = false;
        for (String field : ALLOWED_SORT_FIELDS) {
            if (field.equals(sortBy)) {
                isValidSortField = true;
                break;
            }
        }
        
        if (!isValidSortField) {
            StringBuilder allowedFields = new StringBuilder();
            for (int i = 0; i < ALLOWED_SORT_FIELDS.length; i++) {
                allowedFields.append(ALLOWED_SORT_FIELDS[i]);
                if (i < ALLOWED_SORT_FIELDS.length - 1) {
                    allowedFields.append(", ");
                }
            }
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("Invalid sortBy field: " + sortBy + ". Allowed fields: " + allowedFields.toString()));
        }
        
        // For combined filter search (nativeQuery=true), use Sort.unsorted() to avoid mapping issues
        Pageable pageableForFilter = PageRequest.of(page, size, Sort.unsorted());
        
        // For default search (non-native queries), apply sorting normally
        Pageable pageableDefault = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Check if querystring is not empty for any of the filter parameters
        boolean hasFilter = (fullName != null && !fullName.trim().isEmpty()) ||
                            (staffCode != null && !staffCode.trim().isEmpty()) ||
                            (keyword != null && !keyword.trim().isEmpty());
        
        if (hasFilter) {
            // Search with multiple filters (intersection logic) - native query, use unsorted pageable
            Page<LecturerResponse> lecturers = lecturerService.searchLecturersWithMultipleFilters(
                    fullName, staffCode, keyword, pageableForFilter);
            return ResponseEntity.ok(BaseResponse.success("Lecturers retrieved successfully (combined filters)", lecturers));
        }
        
        // Default: Get all lecturers with pagination - normal JPA query (sorting works)
        Page<LecturerResponse> lecturers = lecturerService.getAllLecturers(pageableDefault);
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

    /**
     * Helper method to map Java field names to database column names (for native queries)
     */
    private String mapLecturerSortByToColumn(String sortBy) {
        return switch (sortBy) {
            case "lecturerId" -> "lecturer_id";
            case "staffCode" -> "staff_code";
            case "fullName" -> "full_name";
            case "createdAt" -> "created_at";
            default -> sortBy;
        };
    }
}
