package com.mercedes.telemetry.event;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.mercedes.telemetry.dto.PasswordChangeOtpEvent;
import com.mercedes.telemetry.dto.PasswordChangedEvent;
import com.mercedes.telemetry.dto.PasswordResetOtpEmailEvent;
import com.mercedes.telemetry.service.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PasswordEmailConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(PasswordEmailConsumer.class);

    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    public PasswordEmailConsumer(
            ObjectMapper objectMapper,
            EmailService emailService
    ) {
        this.objectMapper = objectMapper;
        this.emailService = emailService;
    }

    /**
     * Existing password-change OTP email.
     */
    @KafkaListener(
            topics = Topics.PASSWORD_CHANGE_OTP,
            groupId = "notification-password-otp-email-group"
    )
    public void consumePasswordChangeOtp(String message) {

        try {
            PasswordChangeOtpEvent event =
                    objectMapper.readValue(
                            message,
                            PasswordChangeOtpEvent.class
                    );

            if (
                    event.getEmail() == null
                            || event.getEmail().isBlank()
                            || event.getOtp() == null
                            || event.getOtp().isBlank()
            ) {
                logger.warn("Invalid password-change OTP event received.");
                return;
            }

            emailService.sendPasswordChangeOtp(
                    event.getEmail(),
                    event.getOtp(),
                    event.getExpiresAt()
            );

            logger.info(
                    "Password-change OTP email sent to {}",
                    event.getEmail()
            );

        } catch (Exception exception) {
            logger.error(
                    "Failed to process password-change OTP email event.",
                    exception
            );
        }
    }

    /**
     * Existing password-changed confirmation email.
     */
    @KafkaListener(
            topics = Topics.PASSWORD_CHANGED,
            groupId = "notification-password-confirmation-email-group"
    )
    public void consumePasswordChanged(String message) {

        try {
            PasswordChangedEvent event =
                    objectMapper.readValue(
                            message,
                            PasswordChangedEvent.class
                    );

            if (
                    event.getEmail() == null
                            || event.getEmail().isBlank()
            ) {
                logger.warn("Invalid password-changed event received.");
                return;
            }

            emailService.sendPasswordChangedConfirmation(
                    event.getEmail()
            );

            logger.info(
                    "Password-changed confirmation email sent to {}",
                    event.getEmail()
            );

        } catch (Exception exception) {
            logger.error(
                    "Failed to process password-changed confirmation event.",
                    exception
            );
        }
    }

    /**
     * New password-reset OTP email.
     */
    @KafkaListener(
            topics = Topics.PASSWORD_RESET_OTP,
            groupId = "notification-password-reset-otp-email-group"
    )
    public void consumePasswordResetOtp(String message) {

        try {
            PasswordResetOtpEmailEvent event =
                    objectMapper.readValue(
                            message,
                            PasswordResetOtpEmailEvent.class
                    );

            if (
                    event.getEmail() == null
                            || event.getEmail().isBlank()
                            || event.getOtp() == null
                            || !event.getOtp().matches("\\d{6}")
            ) {
                logger.warn("Invalid password-reset OTP event received.");
                return;
            }

            emailService.sendPasswordResetOtp(
                    event.getEmail(),
                    event.getOtp(),
                    event.getExpiresAt()
            );

            logger.info(
                    "Password-reset OTP email sent to {}",
                    event.getEmail()
            );

        } catch (Exception exception) {
            logger.error(
                    "Failed to process password-reset OTP email event.",
                    exception
            );
        }
    }
}
