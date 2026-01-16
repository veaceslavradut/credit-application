package com.creditapp.shared.repository;

import com.creditapp.shared.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserId(UUID userId);
    List<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);
    Optional<RefreshToken> findByUserIdAndToken(UUID userId, String token);
}