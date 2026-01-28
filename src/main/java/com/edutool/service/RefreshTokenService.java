package com.edutool.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.edutool.model.RefreshToken;
import com.edutool.model.User;
import com.edutool.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final PasswordEncoder encoder;

    public String createRefreshToken(User user) {

        String rawToken = UUID.randomUUID() + UUID.randomUUID().toString();
        String hash = encoder.encode(rawToken);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash);
        token.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));

        repo.save(token);
        return rawToken;
    }
    
    public void revokeToken(String rawToken, User user) {
        // Find all non-revoked tokens for this user
        List<RefreshToken> tokens = repo.findByUserAndRevokedFalse(user);
        
        // Check each token using matches() since BCrypt hashes are different each time
        for (RefreshToken token : tokens) {
            if (encoder.matches(rawToken, token.getTokenHash())) {
                token.setRevoked(true);
                repo.save(token);
                return;
            }
        }
    }
    
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> tokens = repo.findByUserAndRevokedFalse(user);
        tokens.forEach(token -> {
            token.setRevoked(true);
            repo.save(token);
        });
    }
    
    public User validateAndGetUser(String rawToken) {
        // Find all non-revoked tokens
        List<RefreshToken> allTokens = repo.findAll();
        
        for (RefreshToken token : allTokens) {
            // Check if token is valid: not revoked, not expired, and matches the raw token
            if (!token.isRevoked() && 
                token.getExpiresAt().isAfter(Instant.now()) &&
                encoder.matches(rawToken, token.getTokenHash())) {
                return token.getUser();
            }
        }
        
        throw new IllegalArgumentException("Invalid or expired refresh token");
    }
}
