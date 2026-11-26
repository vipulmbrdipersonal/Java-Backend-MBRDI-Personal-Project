
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.PasswordChangeOtp;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordChangeOtpRepository
        extends JpaRepository<PasswordChangeOtp, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT otp
        FROM PasswordChangeOtp otp
        WHERE otp.userId = :userId
          AND otp.consumedAt IS NULL
        ORDER BY otp.createdAt DESC
    """)
    List<PasswordChangeOtp> findActiveOtpsForUpdate(
            @Param("userId") UUID userId
    );

    Optional<PasswordChangeOtp>
    findTopByUserIdAndConsumedAtIsNullOrderByCreatedAtDesc(
            UUID userId
    );

    Optional<PasswordChangeOtp>
    findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    List<PasswordChangeOtp> findAllByUserIdAndConsumedAtIsNull(
            UUID userId
    );

    long countByUserIdAndCreatedAtAfter(
            UUID userId,
            LocalDateTime createdAt
    );
}
