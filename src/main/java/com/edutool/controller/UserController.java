package com.edutool.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edutool.dto.request.ChangeEmailRequest;
import com.edutool.dto.request.ChangePasswordRequest;
import com.edutool.dto.request.CreateUserRequest;
import com.edutool.dto.request.UpdateRoleRequest;
import com.edutool.dto.request.UpdateUserRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.ImportResponse;
import com.edutool.dto.response.UserResponse;
import com.edutool.model.User;
import com.edutool.repository.UserRepository;
import com.edutool.service.CsvExportService;
import com.edutool.service.CsvImportService;
import com.edutool.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final CsvExportService csvExportService;
    private final CsvImportService csvImportService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserResponse>> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Debug: Print authorities
        System.out.println("Username: " + username);
        System.out.println("Authorities: " + authentication.getAuthorities());
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        UserResponse userResponse = convertToUserResponse(user);
        
        return ResponseEntity.ok(BaseResponse.success("User information retrieved successfully", userResponse));
    }

    /**
     * Create a new user (Admin only)
     * @param request - User creation data
     * @return Created user information
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        User createdUser = userService.createUser(request);
        UserResponse userResponse = convertToUserResponse(createdUser);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("User created successfully", userResponse));
    }

    /**
     * Get user by ID (Admin only)
     * @param userId - User ID
     * @return User information
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        UserResponse userResponse = convertToUserResponse(user);
        
        return ResponseEntity.ok(BaseResponse.success("User retrieved successfully", userResponse));
    }

    /**
     * Get all users (Admin only)
     * @param page - Page number (default: 0)
     * @param size - Page size (default: 10)
     * @param sortBy - Sort field (default: userId)
     * @param sortDirection - Sort direction (default: ASC)
     * @return List of users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<User> usersPage = userService.getUsersWithPagination(pageable);
        Page<UserResponse> userResponsePage = usersPage.map(this::convertToUserResponse);
        
        return ResponseEntity.ok(BaseResponse.success("Users retrieved successfully", userResponsePage));
    }

    /**
     * Update user information (Admin only)
     * @param userId - User ID to update
     * @param request - Update data
     * @return Updated user information
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        
        User updatedUser = userService.updateUser(userId, request);
        UserResponse userResponse = convertToUserResponse(updatedUser);
        
        return ResponseEntity.ok(BaseResponse.success("User updated successfully", userResponse));
    }

    /**
     * Delete user by ID (Admin only)
     * @param userId - User ID to delete
     * @return Success message
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        
        return ResponseEntity.ok(BaseResponse.success("User deleted successfully", null));
    }

    /**
     * Admin API to update user role
     * @param request - Contains userId and new role
     * @return Updated user information
     */
    @PutMapping("/admin/update-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserResponse>> updateUserRole(@Valid @RequestBody UpdateRoleRequest request) {
        User updatedUser = userService.updateUserRole(request.getUserId(), request.getRole());
        UserResponse userResponse = convertToUserResponse(updatedUser);
        
        return ResponseEntity.ok(BaseResponse.success("User role updated successfully", userResponse));
    }

    /**
     * Change own password
     * @param request - Password change request
     * @return Success message
     */
    @PutMapping("/me/password")
    public ResponseEntity<BaseResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        userService.changePassword(username, request);

        return ResponseEntity.ok(BaseResponse.success("Password changed successfully", null));
    }

    /**
     * Change own email
     * @param request - Email change request
     * @return Updated user information
     */
    @PutMapping("/me/email")
    public ResponseEntity<BaseResponse<UserResponse>> changeEmail(@Valid @RequestBody ChangeEmailRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User updatedUser = userService.changeEmail(username, request);
        UserResponse userResponse = convertToUserResponse(updatedUser);

        return ResponseEntity.ok(BaseResponse.success("Email changed successfully", userResponse));
    }

    /**
     * Search users by keyword (Admin only)
     * @param keyword - Search keyword for username, email, or full name
     * @return List of matching users
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<UserResponse>>> searchUsers(
            @RequestParam(required = false) String keyword) {
        
        List<User> users = userService.searchUsers(keyword);
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(BaseResponse.success("Users retrieved successfully", userResponses));
    }

    /**
     * Get users by role (Admin only)
     * @param role - User role (ADMIN, LECTURER, STUDENT)
     * @return List of users with the specified role
     */
    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<UserResponse>>> getUsersByRole(@PathVariable String role) {
        List<User> users = userService.getUsersByRole(role);
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(BaseResponse.success(
                "Users with role " + role.toUpperCase() + " retrieved successfully", userResponses));
    }

    /**
     * Get users by status (Admin only)
     * @param status - User status (ACTIVE, INACTIVE, VERIFICATION_PENDING)
     * @return List of users with the specified status
     */
    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<UserResponse>>> getUsersByStatus(@PathVariable String status) {
        List<User> users = userService.getUsersByStatus(status);
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(BaseResponse.success(
                "Users with status " + status.toUpperCase() + " retrieved successfully", userResponses));
    }

    /**
     * Get user by username (Admin only)
     * @param username - Username to search for
     * @return User information
     */
    @GetMapping("/by-username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        UserResponse userResponse = convertToUserResponse(user);

        return ResponseEntity.ok(BaseResponse.success("User retrieved successfully", userResponse));
    }

    /**
     * Export all users to CSV file (Admin only)
     * @return CSV file download
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportUsers() {
        List<User> users = userService.getAllUsers();
        ByteArrayInputStream csvData = csvExportService.exportUsersToCsv(users);

        String filename = "users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        InputStreamResource resource = new InputStreamResource(csvData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    /**
     * Import users from CSV file (Admin only)
     * @param file - CSV file containing user data
     * @return Import result with success and error counts
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<ImportResponse>> importUsers(
            @RequestParam("file") MultipartFile file) {
        
        CsvImportService.ImportResult result = csvImportService.importUsersFromCsv(file);
        
        ImportResponse response = new ImportResponse(
                result.getSuccessCount(),
                result.getErrorCount(),
                result.getErrors()
        );

        if (result.getErrorCount() > 0) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body(BaseResponse.success("Import completed with errors", response));
        }

        return ResponseEntity.ok(BaseResponse.success("All users imported successfully", response));
    }

    /**
     * Helper method to convert User entity to UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setUserId(user.getUserId());
        userResponse.setUsername(user.getUsername());
        userResponse.setRole(user.getRole().toString());
        userResponse.setFullName(user.getFullName());
        userResponse.setEmail(user.getEmail());
        userResponse.setStatus(user.getStatus().toString());
        return userResponse;
    }
}
