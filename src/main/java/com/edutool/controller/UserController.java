package com.edutool.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edutool.dto.request.UpdateRoleRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.UserResponse;
import com.edutool.model.User;
import com.edutool.repository.UserRepository;
import com.edutool.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserResponse>> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Debug: Print authorities
        System.out.println("Username: " + username);
        System.out.println("Authorities: " + authentication.getAuthorities());
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        UserResponse userResponse = new UserResponse();
        userResponse.setUserId(user.getUserId());
        userResponse.setUsername(user.getUsername());
        userResponse.setRole(user.getRole().toString());
        userResponse.setFullName(user.getFullName());
        userResponse.setEmail(user.getEmail());
        userResponse.setStatus(user.getStatus().toString());
        
        return ResponseEntity.ok(BaseResponse.success("User information retrieved successfully", userResponse));
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
        
        UserResponse userResponse = new UserResponse();
        userResponse.setUserId(updatedUser.getUserId());
        userResponse.setUsername(updatedUser.getUsername());
        userResponse.setRole(updatedUser.getRole().toString());
        userResponse.setFullName(updatedUser.getFullName());
        userResponse.setEmail(updatedUser.getEmail());
        userResponse.setStatus(updatedUser.getStatus().toString());
        
        return ResponseEntity.ok(BaseResponse.success("User role updated successfully", userResponse));
    }
}
