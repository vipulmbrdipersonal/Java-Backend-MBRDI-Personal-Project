
package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.PasswordChangedEmailEvent;
import com.mercedes.telemetry.dto.PasswordResetOtpEmailEvent;
import com.mercedes.telemetry.entity.PasswordResetOtp;
import com.mercedes.telemetry.entity.PasswordResetToken;
import com.mercedes.telemetry.entity.User;
import com.mercedes.telemetry.event.PasswordEmailEventPublisher;
import com.mercedes.telemetry.repository.PasswordResetOtpRepository;
import com.mercedes.telemetry.repository.PasswordResetTokenRepository;
import com.mercedes.telemetry.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class PasswordResetService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int MAX_REQUESTS = 3;
    private static final int REQUEST_WINDOW_MINUTES = 15;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 10;

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordEmailEventPublisher emailEventPublisher;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetOtpRepository otpRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            PasswordEmailEventPublisher emailEventPublisher
    ) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailEventPublisher = emailEventPublisher;
    }

    /**
     * STEP 1:
     * Request an OTP for the registered email address.
     */
    @Transactional
    public void requestOtp(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email address is required."
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElse(null);

        /*
         * Do not reveal whether an account exists.
         * The controller returns the same response in either case.
         */
        if (user == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        List<PasswordResetOtp> activeOtps =
                otpRepository.findActiveOtpsForUpdate(
                        user.getId()
                );

        /*
         * Check the latest OTP, including consumed OTPs,
         * to enforce the resend cooldown.
         */
        PasswordResetOtp latestOtp =
                otpRepository.findTopByUserIdOrderByCreatedAtDesc(
                        user.getId()
                ).orElse(null);

        if (latestOtp != null) {

            LocalDateTime resendAvailableAt =
                    latestOtp.getCreatedAt()
                            .plusSeconds(RESEND_COOLDOWN_SECONDS);

            if (now.isBefore(resendAvailableAt)) {
                throw new IllegalStateException(
                        "Please wait before requesting another OTP."
                );
            }
        }

        long requestCount =
                otpRepository.countByUserIdAndCreatedAtAfter(
                        user.getId(),
                        now.minusMinutes(REQUEST_WINDOW_MINUTES)
                );

        if (requestCount >= MAX_REQUESTS) {
            throw new IllegalStateException(
                    "Too many OTP requests. Please try again later."
            );
        }

        /*
         * Invalidate previously issued OTPs.
         */
        for (PasswordResetOtp activeOtp : activeOtps) {
            activeOtp.setConsumedAt(now);
        }

        otpRepository.saveAll(activeOtps);

        String otp = generateOtp();

        LocalDateTime expiresAt =
                now.plusMinutes(OTP_EXPIRY_MINUTES);

        PasswordResetOtp resetOtp =
                new PasswordResetOtp();

        resetOtp.setUserId(user.getId());
        resetOtp.setEmail(user.getEmail());
        resetOtp.setOtpHash(passwordEncoder.encode(otp));
        resetOtp.setExpiresAt(expiresAt);
        resetOtp.setAttempts(0);

        otpRepository.save(resetOtp);

        PasswordResetOtpEmailEvent event =
                new PasswordResetOtpEmailEvent(
                        user.getEmail(),
                        otp,
                        expiresAt.toString()
                );

        /*
         * Email event is published only after the OTP transaction commits.
         */
        emailEventPublisher.publishPasswordResetOtpAfterCommit(event);
    }

    /**
     * STEP 2:
     * Verify OTP and issue a temporary reset token.
     *
     * Returns null for an invalid, expired or exhausted OTP.
     * The caller should send the HTTP error after this transaction returns.
     */
    @Transactional
    public String verifyOtp(
            String email,
            String submittedOtp
    ) {

        if (
                email == null
                        || email.isBlank()
                        || submittedOtp == null
                        || !submittedOtp.matches("\\d{6}")
        ) {
            return null;
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElse(null);

        if (user == null) {
            return null;
        }

        List<PasswordResetOtp> activeOtps =
                otpRepository.findActiveOtpsForUpdate(
                        user.getId()
                );

        if (activeOtps.isEmpty()) {
            return null;
        }

        PasswordResetOtp otpRecord = activeOtps.get(0);

        LocalDateTime now = LocalDateTime.now();

        if (
                otpRecord.getExpiresAt() == null
                        || !otpRecord.getExpiresAt().isAfter(now)
        ) {
            otpRecord.setConsumedAt(now);
            otpRepository.save(otpRecord);
            return null;
        }

        if (otpRecord.getAttempts() >= MAX_OTP_ATTEMPTS) {
            otpRecord.setConsumedAt(now);
            otpRepository.save(otpRecord);
            return null;
        }

        boolean matches = passwordEncoder.matches(
                submittedOtp,
                otpRecord.getOtpHash()
        );

        if (!matches) {

            otpRecord.setAttempts(
                    otpRecord.getAttempts() + 1
            );

            if (otpRecord.getAttempts() >= MAX_OTP_ATTEMPTS) {
                otpRecord.setConsumedAt(now);
            }

            otpRepository.save(otpRecord);

            return null;
        }

        /*
         * OTP can only be used once.
         */
        otpRecord.setConsumedAt(now);
        otpRepository.save(otpRecord);

        /*
         * Invalidate any previously issued reset tokens.
         */
        List<PasswordResetToken> activeTokens =
                tokenRepository.findActiveTokensForUpdate(
                        user.getId()
                );

        for (PasswordResetToken activeToken : activeTokens) {
            activeToken.setConsumedAt(now);
        }

        tokenRepository.saveAll(activeTokens);

        /*
         * Generate a cryptographically random temporary token.
         * Only its SHA-256 hash is persisted.
         */
        String rawResetToken = generateResetToken();

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setUserId(user.getId());
        resetToken.setTokenHash(hashToken(rawResetToken));
        resetToken.setExpiresAt(
                now.plusMinutes(RESET_TOKEN_EXPIRY_MINUTES)
        );

        tokenRepository.save(resetToken);

        /*
         * The raw token is returned only once to the verified user.
         */
        return rawResetToken;
    }

    /**
     * STEP 3:
     * Validate the temporary reset token and update the password.
     *
     * Returns false when the token has expired.
     */
    @Transactional
    public boolean resetPassword(
            String rawResetToken,
            String newPassword
    ) {

        if (rawResetToken == null || rawResetToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Password reset authorization is required."
            );
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "New password is required."
            );
        }

        if (newPassword.length() < 8 || newPassword.length() > 72) {
            throw new IllegalArgumentException(
                    "Password must contain between 8 and 72 characters."
            );
        }

        String tokenHash = hashToken(rawResetToken);

        PasswordResetToken resetToken =
                tokenRepository.findActiveTokenForUpdate(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid or expired password reset session."
                                )
                        );

        LocalDateTime now = LocalDateTime.now();

        if (
                resetToken.getExpiresAt() == null
                        || !resetToken.getExpiresAt().isAfter(now)
        ) {
            resetToken.setConsumedAt(now);
            tokenRepository.save(resetToken);

            /*
             * Return normally so consumedAt is committed.
             */
            return false;
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User account could not be found."
                        )
                );

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException(
                    "New password must be different from the current password."
            );
        }

        /*
         * Save using the same BCrypt PasswordEncoder as normal
         * authenticated password changes.
         */
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        /*
         * Consume this token and invalidate any other active
         * reset tokens belonging to this user.
         */
        resetToken.setConsumedAt(now);
        tokenRepository.save(resetToken);

        List<PasswordResetToken> otherTokens =
                tokenRepository.findActiveTokensForUpdate(
                        user.getId()
                );

        for (PasswordResetToken otherToken : otherTokens) {
            otherToken.setConsumedAt(now);
        }

        tokenRepository.saveAll(otherTokens);

        /*
         * Reuse the existing password-changed confirmation email.
         * It is published only after the password update commits.
         */
        PasswordChangedEmailEvent confirmationEvent =
                new PasswordChangedEmailEvent(
                        user.getEmail(),
                        now.toString()
                );

        emailEventPublisher.publishPasswordChangedAfterCommit(
                confirmationEvent
        );

        return true;
    }

    private String generateOtp() {

        int number = SECURE_RANDOM.nextInt(
                (int) Math.pow(10, OTP_LENGTH)
        );

        return String.format("%06d", number);
    }

    private String generateResetToken() {

        byte[] bytes = new byte[32];

        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Unable to process password reset token.",
                    exception
            );
        }
    }
}
