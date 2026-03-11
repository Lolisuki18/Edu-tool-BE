package com.edutool.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.edutool.dto.request.ChangeEmailRequest;
import com.edutool.dto.request.ChangePasswordRequest;
import com.edutool.dto.request.CreateUserRequest;
import com.edutool.dto.request.UpdateUserRequest;
import com.edutool.model.Lecturer;
import com.edutool.model.Role;
import com.edutool.model.Student;
import com.edutool.model.User;
import com.edutool.model.UserStatus;
import com.edutool.repository.LecturerRepository;
import com.edutool.repository.StudentRepository;
import com.edutool.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Create a new user
     * @param request - User creation data
     * @return Created user
     */
    @Transactional
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

        User savedUser = userRepository.save(user);

        // Auto-create profile for STUDENT or LECTURER
        if (savedUser.getRole() == Role.STUDENT) {
            String studentCode = (request.getStudentCode() != null && !request.getStudentCode().isBlank())
                    ? request.getStudentCode()
                    : String.format("SE%06d", savedUser.getUserId());
            Student student = new Student();
            student.setUser(savedUser);
            student.setStudentCode(studentCode);
            student.setCreatedAt(LocalDateTime.now());
            studentRepository.save(student);
        } else if (savedUser.getRole() == Role.LECTURER) {
            String staffCode = (request.getStaffCode() != null && !request.getStaffCode().isBlank())
                    ? request.getStaffCode()
                    : String.format("GV%03d", savedUser.getUserId());
            Lecturer lecturer = new Lecturer();
            lecturer.setUser(savedUser);
            lecturer.setStaffCode(staffCode);
            lecturer.setCreatedAt(LocalDateTime.now());
            lecturerRepository.save(lecturer);
        }

        return savedUser;
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
    @Transactional
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

        User savedUser = userRepository.save(user);
        if (request.getRole() != null) {
            createRoleProfileIfNotExists(savedUser, savedUser.getRole());
        }
        return savedUser;
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
    @Transactional
    public User updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        try {
            Role newRole = Role.valueOf(role.toUpperCase());
            user.setRole(newRole);
            User savedUser = userRepository.save(user);
            createRoleProfileIfNotExists(savedUser, newRole);
            return savedUser;
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



    /**
     * Search users with multiple filters (intersection of all conditions)
     * Case 1: Combine username, email, fullName, role, status
     * Case 2: Combine keyword, role, status
     * - If a parameter is null/empty, it's ignored (acts as OR 1=1)
     * - Results are intersection of all applied conditions
     * 
     * @param username - Fuzzy search on username (null = ignored)
     * @param email - Fuzzy search on email (null = ignored)
     * @param fullName - Fuzzy search on fullName (null = ignored)
     * @param keyword - Fuzzy search on username/email/fullName (null = ignored)
     * @param role - Exact match on role (null = ignored)
     * @param status - Exact match on status (null = ignored)
     * @param pageable - Pagination parameters
     * @return Page of users matching all applied filters (intersection)
     */
    public Page<User> searchUsersWithMultipleFilters(
            String username, String email, String fullName, String keyword, String role, String status, Pageable pageable) {
        
        // Normalize empty strings to null
        username = (username != null && username.trim().isEmpty()) ? null : username;
        email = (email != null && email.trim().isEmpty()) ? null : email;
        fullName = (fullName != null && fullName.trim().isEmpty()) ? null : fullName;
        keyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;
        role = (role != null && role.trim().isEmpty()) ? null : role;
        status = (status != null && status.trim().isEmpty()) ? null : status;
        
        // Validate role if provided
        if (role != null) {
            try {
                Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role. Allowed values: ADMIN, LECTURER, STUDENT");
            }
        }
        
        // Validate status if provided
        if (status != null) {
            try {
                UserStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status. Allowed values: ACTIVE, INACTIVE, SUSPENDED");
            }
        }
        
        // Call repository method that implements intersection logic
        return userRepository.searchUsersWithMultipleFilters(username, email, fullName, keyword, role, status, pageable);
    }

    // -----------------------------------------------------------------------
    // Private helpers: auto-create Student/Lecturer profile on role assignment
    // -----------------------------------------------------------------------

    private void createRoleProfileIfNotExists(User user, Role role) {
        if (role == Role.STUDENT) {
            createStudentProfileIfNotExists(user);
        } else if (role == Role.LECTURER) {
            createLecturerProfileIfNotExists(user);
        }
    }

    private void createStudentProfileIfNotExists(User user) {
        if (studentRepository.findByUserUserId(user.getUserId()).isEmpty()) {
            Student student = new Student();
            student.setUser(user);
            student.setStudentCode("SV" + user.getUserId());
            studentRepository.save(student);
        }
    }

    private void createLecturerProfileIfNotExists(User user) {
        if (lecturerRepository.findByUserUserId(user.getUserId()).isEmpty()) {
            Lecturer lecturer = new Lecturer();
            lecturer.setUser(user);
            lecturer.setStaffCode("GV" + user.getUserId());
            lecturerRepository.save(lecturer);
        }
    }
}
