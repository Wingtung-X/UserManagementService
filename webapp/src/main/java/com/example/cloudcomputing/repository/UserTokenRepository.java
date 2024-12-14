package com.example.cloudcomputing.repository;

import com.example.cloudcomputing.model.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {
    Optional<UserToken> findByToken(String token);
    Optional<UserToken> findByUserId(String userId);
    Optional<UserToken> findByUserIdAndToken(String userId, String token);
}

