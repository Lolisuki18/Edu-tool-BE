package com.edutool.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
}
