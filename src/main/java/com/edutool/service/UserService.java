package com.edutool.service;

import org.springframework.stereotype.Service;

import com.edutool.model.Role;
import com.edutool.model.User;
import com.edutool.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
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
}
