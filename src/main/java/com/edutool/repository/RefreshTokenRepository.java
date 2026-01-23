package com.edutool.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edutool.model.RefreshToken;
import com.edutool.model.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);
    
    List<RefreshToken> findByUserAndRevokedFalse(User user);
}