package com.edutool.controller;

import com.edutool.dto.request.CreateStudentRequest;
import com.edutool.dto.request.UpdateStudentRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.StudentResponse;
import com.edutool.service.StudentService;
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
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<BaseResponse<StudentResponse>> createStudent(
            @Valid @RequestBody CreateStudentRequest request) {
        StudentResponse student = studentService.createStudent(request);
        BaseResponse<StudentResponse> response = BaseResponse.success(
                HttpStatus.CREATED.value(),
                "Student created successfully",
                student
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<BaseResponse<StudentResponse>> getStudentById(@PathVariable Integer id) {
        StudentResponse student = studentService.getStudentById(id);
        return ResponseEntity.ok(BaseResponse.success("Student retrieved successfully", student));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<?> searchStudents(
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String githubUsername,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "studentId") String sortBy,
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
        final String[] ALLOWED_SORT_FIELDS = {"studentId", "studentCode", "fullName", "githubUsername", "createdAt"};
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
        boolean hasFilter = (studentCode != null && !studentCode.trim().isEmpty()) ||
                            (githubUsername != null && !githubUsername.trim().isEmpty()) ||
                            (fullName != null && !fullName.trim().isEmpty()) ||
                            (keyword != null && !keyword.trim().isEmpty());
        
        if (hasFilter) {
            // Search with multiple filters (intersection logic) - native query, use unsorted pageable
            Page<StudentResponse> students = studentService.searchStudentsWithMultipleFilters(
                    fullName, studentCode, githubUsername, keyword, pageableForFilter);
            return ResponseEntity.ok(BaseResponse.success("Students retrieved successfully (combined filters)", students));
        }
        
        // Default: Get all students with pagination - normal JPA query (sorting works)
        Page<StudentResponse> students = studentService.getAllStudents(pageableDefault);
        return ResponseEntity.ok(BaseResponse.success("Students retrieved successfully", students));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<BaseResponse<StudentResponse>> updateStudent(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateStudentRequest request) {
        StudentResponse student = studentService.updateStudent(id, request);
        return ResponseEntity.ok(BaseResponse.success("Student updated successfully", student));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteStudent(@PathVariable Integer id) {
        studentService.deleteStudent(id);
        BaseResponse<Void> response = BaseResponse.success(
                HttpStatus.NO_CONTENT.value(),
                "Student deleted successfully",
                null
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    /**
     * Helper method to map Java field names to database column names (for native queries)
     */
    private String mapStudentSortByToColumn(String sortBy) {
        return switch (sortBy) {
            case "studentId" -> "student_id";
            case "studentCode" -> "student_code";
            case "fullName" -> "full_name";
            case "githubUsername" -> "github_username";
            case "createdAt" -> "created_at";
            case "updatedAt" -> "updated_at";
            default -> sortBy;
        };
    }
}
