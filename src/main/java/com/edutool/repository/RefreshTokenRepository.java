package com.edutool.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edutool.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);
}