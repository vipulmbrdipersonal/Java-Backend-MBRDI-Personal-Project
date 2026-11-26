
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT token
        FROM PasswordResetToken token
        WHERE token.tokenHash = :tokenHash
          AND token.consumedAt IS NULL
    """)
    Optional<PasswordResetToken> findActiveTokenForUpdate(
            @Param("tokenHash") String tokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT token
        FROM PasswordResetToken token
        WHERE token.userId = :userId
          AND token.consumedAt IS NULL
    """)
    List<PasswordResetToken> findActiveTokensForUpdate(
            @Param("userId") UUID userId
    );
}
