package com.edutool.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.edutool.dto.RegisterRequest;
import com.edutool.model.Role;
import com.edutool.model.User;
import com.edutool.model.UserStatus;
import com.edutool.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        user.setStatus(UserStatus.VERIFICATION_PENDING);
        user.setRole(Role.STUDENT);
        user.setCreatedAt(LocalDateTime.now());
        user.setEmailVerificationToken(UUID.randomUUID().toString());

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken());
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerificationToken(null); // Clear the token after verification
        userRepository.save(user);
    }
}