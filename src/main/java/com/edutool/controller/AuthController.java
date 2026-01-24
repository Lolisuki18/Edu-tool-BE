package com.edutool.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edutool.dto.request.LoginRequest;
import com.edutool.dto.request.RegisterRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.LoginResponse;
import com.edutool.model.User;
import com.edutool.model.UserStatus;
import com.edutool.repository.UserRepository;
import com.edutool.service.AuthService;
import com.edutool.service.RefreshTokenService;
import com.edutool.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<String>> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);
        return ResponseEntity.ok(BaseResponse.success(
                "Registration successful. Please check your email to verify your account.",
                null));
    }

    @GetMapping("/verify")
    public ResponseEntity<BaseResponse<String>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(BaseResponse.success(
                "Email verified successfully. You can now log in.",
                null));
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        //Load user first to check status
        User user = userRepository.findByEmailOrUsername(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        //Check email is verified or not
        if (user.getStatus() == UserStatus.VERIFICATION_PENDING) { 
            if(request.getUsername().equals(user.getEmail()) && !request.getUsername().equals(user.getUsername())) {
                throw new IllegalArgumentException("Please verify your email before logging in by email.");
            }
        }

        //Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword())
        );

        //Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        //Set refresh token cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)           // HTTPS only
                .sameSite("Strict")
                .path("/auth/refresh")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // return login response
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(accessToken);
        loginResponse.setFullName(user.getFullName());
        loginResponse.setRole(user.getRole().toString());
        loginResponse.setEmail(user.getEmail());
        loginResponse.setStatus(user.getStatus().toString());

        return ResponseEntity.ok(BaseResponse.success("Login successful", loginResponse));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        // Get current user from security context
        org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            
            // Get refresh token from cookie
            Cookie[] cookies = request.getCookies();
            String refreshToken = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }
            
            // Revoke refresh token if exists and user is found
            if (refreshToken != null && user != null) {
                refreshTokenService.revokeToken(refreshToken, user);
            }
        }
        
        // Clear refresh token cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/auth/refresh")
                .maxAge(0)
                .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        
        return ResponseEntity.ok(BaseResponse.success("Logout successful", null));
    }
}