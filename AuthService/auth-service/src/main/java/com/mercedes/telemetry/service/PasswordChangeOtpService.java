
package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.PasswordChangeOtpEmailEvent;
import com.mercedes.telemetry.entity.PasswordChangeOtp;
import com.mercedes.telemetry.entity.User;
import com.mercedes.telemetry.event.PasswordEmailEventPublisher;
import com.mercedes.telemetry.repository.PasswordChangeOtpRepository;
import com.mercedes.telemetry.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PasswordChangeOtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_REQUESTS = 3;
    private static final int REQUEST_WINDOW_MINUTES = 15;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PasswordChangeOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordEmailEventPublisher emailEventPublisher;

    public PasswordChangeOtpService(
            PasswordChangeOtpRepository otpRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordEmailEventPublisher emailEventPublisher) {

        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailEventPublisher = emailEventPublisher;
    }

    /**
     * Generates and stores a password-change OTP.
     *
     * The OTP is stored as a BCrypt hash.
     * The plaintext OTP is published only for email delivery.
     */
    @Transactional
    public IssuedOtp requestOtp(
            String email,
            String currentPassword) {

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unable to verify current credentials."
                        )
                );

        if (currentPassword == null
                || !passwordEncoder.matches(
                currentPassword,
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "Unable to verify current credentials."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        // Resend cooldown
        otpRepository
                .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .ifPresent(previousOtp -> {

                    LocalDateTime cooldownEnd =
                            previousOtp.getCreatedAt()
                                    .plusSeconds(RESEND_COOLDOWN_SECONDS);

                    if (now.isBefore(cooldownEnd)) {
                        throw new IllegalStateException(
                                "Please wait before requesting another OTP."
                        );
                    }
                });

        // Maximum three requests within fifteen minutes
        long recentRequests =
                otpRepository.countByUserIdAndCreatedAtAfter(
                        user.getId(),
                        now.minusMinutes(REQUEST_WINDOW_MINUTES)
                );

        if (recentRequests >= MAX_REQUESTS) {
            throw new IllegalStateException(
                    "Too many OTP requests. Please try again later."
            );
        }

        // Invalidate previous active OTP records
        List<PasswordChangeOtp> activeOtps =
                otpRepository.findAllByUserIdAndConsumedAtIsNull(
                        user.getId()
                );

        for (PasswordChangeOtp previousOtp : activeOtps) {
            previousOtp.setConsumedAt(now);
        }

        if (!activeOtps.isEmpty()) {
            otpRepository.saveAll(activeOtps);
        }

        // Generate six-digit OTP, including possible leading zeroes
        String otp = String.format(
                "%0" + OTP_LENGTH + "d",
                SECURE_RANDOM.nextInt(1_000_000)
        );

        PasswordChangeOtp otpEntity = new PasswordChangeOtp();

        otpEntity.setUserId(user.getId());
        otpEntity.setEmail(user.getEmail());

        // Store only the BCrypt hash
        otpEntity.setOtpHash(
                passwordEncoder.encode(otp)
        );

        LocalDateTime expiresAt =
                now.plusMinutes(OTP_EXPIRY_MINUTES);

        otpEntity.setExpiresAt(expiresAt);
        otpEntity.setAttempts(0);
        otpEntity.setConsumedAt(null);
        otpEntity.setCreatedAt(now);

        // Save OTP before scheduling the email event
        otpRepository.save(otpEntity);

        // Publish OTP email event after successful transaction commit
        PasswordChangeOtpEmailEvent emailEvent =
                new PasswordChangeOtpEmailEvent(
                        user.getEmail(),
                        otp,
                        expiresAt.toString()
                );

        emailEventPublisher.publishOtpAfterCommit(emailEvent);

        return new IssuedOtp(
                otp,
                user.getEmail(),
                expiresAt
        );
    }

    /**
     * Verifies and consumes the OTP.
     *
     * When called from an outer AuthService transaction,
     * OTP consumption participates in that transaction.
     */
    @Transactional
    public boolean verifyOtp(
            String email,
            String submittedOtp) {

        if (email == null
                || submittedOtp == null
                || !submittedOtp.matches("\\d{6}")) {

            return false;
        }

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElse(null);

        if (user == null) {
            return false;
        }

        List<PasswordChangeOtp> activeOtps =
                otpRepository.findActiveOtpsForUpdate(
                        user.getId()
                );

        if (activeOtps.isEmpty()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        PasswordChangeOtp otpEntity = activeOtps.get(0);

        // Invalidate any unexpected older active records
        for (int i = 1; i < activeOtps.size(); i++) {
            activeOtps.get(i).setConsumedAt(now);
        }

        if (activeOtps.size() > 1) {
            otpRepository.saveAll(
                    activeOtps.subList(1, activeOtps.size())
            );
        }

        // Expired OTP
        if (!now.isBefore(otpEntity.getExpiresAt())) {
            otpEntity.setConsumedAt(now);
            otpRepository.save(otpEntity);
            return false;
        }

        // Maximum verification attempts
        if (otpEntity.getAttempts() >= MAX_ATTEMPTS) {
            otpEntity.setConsumedAt(now);
            otpRepository.save(otpEntity);
            return false;
        }

        // Invalid OTP
        if (!passwordEncoder.matches(
                submittedOtp,
                otpEntity.getOtpHash())) {

            int attempts = otpEntity.getAttempts() + 1;
            otpEntity.setAttempts(attempts);

            if (attempts >= MAX_ATTEMPTS) {
                otpEntity.setConsumedAt(now);
            }

            otpRepository.save(otpEntity);
            return false;
        }

        // Successful verification: consume OTP
        otpEntity.setConsumedAt(now);
        otpRepository.save(otpEntity);

        return true;
    }

    /**
     * Internal result.
     * Never return the plaintext OTP to the frontend.
     */
    public static class IssuedOtp {

        private final String otp;
        private final String email;
        private final LocalDateTime expiresAt;

        public IssuedOtp(
                String otp,
                String email,
                LocalDateTime expiresAt) {

            this.otp = otp;
            this.email = email;
            this.expiresAt = expiresAt;
        }

        public String getOtp() {
            return otp;
        }

        public String getEmail() {
            return email;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }
    }
}
