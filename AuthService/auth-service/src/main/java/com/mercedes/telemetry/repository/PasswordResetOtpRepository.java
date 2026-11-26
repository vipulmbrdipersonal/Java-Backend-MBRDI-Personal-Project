
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.PasswordResetOtp;

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
public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT otp
        FROM PasswordResetOtp otp
        WHERE otp.userId = :userId
          AND otp.consumedAt IS NULL
        ORDER BY otp.createdAt DESC
    """)
    List<PasswordResetOtp> findActiveOtpsForUpdate(
            @Param("userId") UUID userId
    );

    Optional<PasswordResetOtp>
    findTopByUserIdAndConsumedAtIsNullOrderByCreatedAtDesc(
            UUID userId
    );

    // Includes consumed OTPs so the resend cooldown
    // cannot be bypassed by verifying or consuming an OTP.
    Optional<PasswordResetOtp> findTopByUserIdOrderByCreatedAtDesc(
            UUID userId
    );

    long countByUserIdAndCreatedAtAfter(
            UUID userId,
            LocalDateTime createdAt
    );
}
