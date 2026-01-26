package com.edutool.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.edutool.dto.request.ChangeEmailRequest;
import com.edutool.dto.request.ChangePasswordRequest;
import com.edutool.dto.request.CreateUserRequest;
import com.edutool.dto.request.UpdateUserRequest;
import com.edutool.model.Role;
import com.edutool.model.User;
import com.edutool.model.UserStatus;
import com.edutool.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.io.ObjectInputFilter.Status;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Create a new user
     * @param request - User creation data
     * @return Created user
     */
    public User createUser(CreateUserRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setStatus(UserStatus.valueOf(request.getStatus().toUpperCase()));
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Get user by ID
     * @param userId - User ID
     * @return User if found
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get users with pagination
     * @param pageable - Pagination parameters
     * @return Page of users
     */
    public Page<User> getUsersWithPagination(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Update user information
     * @param userId - ID of the user to update
     * @param request - Update data
     * @return Updated user
     */
    public User updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Update email if provided and different
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Update full name if provided
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }

        // Update role if provided
        if (request.getRole() != null) {
            try {
                Role newRole = Role.valueOf(request.getRole().toUpperCase());
                user.setRole(newRole);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role. Allowed values: ADMIN, LECTURER, STUDENT");
            }
        }

        // Update status if provided
        if (request.getStatus() != null) {
            try {
                UserStatus newStatus = UserStatus.valueOf(request.getStatus().toUpperCase());
                user.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status. Allowed values: ACTIVE, INACTIVE, SUSPENDED");
            }
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * Delete user by ID
     * @param userId - ID of the user to delete
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    /**
     * Update user role (Admin only)
     * @param userId - ID of the user to update
     * @param role - New role to assign (ADMIN, LECTURER, STUDENT)
     * @return Updated user
     */
    public User updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        try {
            Role newRole = Role.valueOf(role.toUpperCase());
            user.setRole(newRole);
            return userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Allowed values: ADMIN, LECTURER, STUDENT");
        }
    }

    /**
     * Change user password
     * @param username - Username of the user
     * @param request - Password change request
     * @return Updated user
     */
    public User changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        return userRepository.save(user);
    }

    /**
     * Change user email
     * @param username - Username of the user
     * @param request - Email change request
     * @return Updated user
     */
    public User changeEmail(String username, ChangeEmailRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Password is incorrect");
        }

        // Check if new email already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getNewEmail());
        if (existingUser.isPresent() && !existingUser.get().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Email already exists: " + request.getNewEmail());
        }

        // Update email
        user.setEmail(request.getNewEmail());
        return userRepository.save(user);
    }

    /**
     * Search users by keyword
     * @param keyword - Search keyword
     * @return List of matching users
     */
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.searchUsers(keyword.trim());
    }

    /**
     * Get users by role
     * @param role - User role
     * @return List of users with the specified role
     */
    public List<User> getUsersByRole(String role) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            return userRepository.findByRole(userRole);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Allowed values: ADMIN, LECTURER, STUDENT");
        }
    }

    /**
     * Get users by status
     * @param status - User status
     * @return List of users with the specified status
     */
    public List<User> getUsersByStatus(String status) {
        try {
            UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
            return userRepository.findByStatus(userStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Allowed values: ACTIVE, INACTIVE, SUSPENDED");
        }
    }

    /**
     * Get user by username
     * @param username - Username
     * @return User if found
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public Long getUserIdFromUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null ? user.getUserId() : null;
    }
}
