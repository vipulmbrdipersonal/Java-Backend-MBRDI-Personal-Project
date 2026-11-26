
package com.mercedes.telemetry.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /*
     * PASSWORD CHANGE OTP EMAIL
     */
    public void sendPasswordChangeOtp(
            String recipient,
            String otp,
            String expiresAt
    ) {

        String subject =
                "Mercedes-Benz - Password Change Verification";

        String plainText = """
                Mercedes-Benz Account Security

                Your password change verification code is: %s

                If you did not request this change, please secure your account.
                """.formatted(otp);

        String html = """
                <html>
                <body style="font-family:Arial;background:#f1f3f5;padding:30px;">
                  <div style="max-width:550px;margin:auto;background:#ffffff;
                              padding:35px;border-radius:10px;">
                    <h2 style="color:#111820;">Mercedes-Benz</h2>
                    <p>The Best or Nothing.</p>
                    <h3>Password Change Verification</h3>
                    <p>Your verification code is:</p>
                    <h1 style="letter-spacing:8px;color:#111820;">%s</h1>
                    <p>If you did not request this change, secure your account.</p>
                  </div>
                </body>
                </html>
                """.formatted(otp);

        sendEmail(recipient, subject, plainText, html);
    }

    /*
     * PASSWORD CHANGED CONFIRMATION EMAIL
     */
    public void sendPasswordChangedConfirmation(String recipient) {

        String subject =
                "Mercedes-Benz - Password Changed Successfully";

        String plainText = """
                Your Mercedes-Benz account password has been changed successfully.

                If you did not perform this action, contact support immediately.
                """;

        String html = """
                <html>
                <body style="font-family:Arial;background:#f1f3f5;padding:30px;">
                  <div style="max-width:550px;margin:auto;background:#ffffff;
                              padding:35px;border-radius:10px;">
                    <h2 style="color:#111820;">Mercedes-Benz</h2>
                    <p>The Best or Nothing.</p>
                    <h3>Password Changed Successfully</h3>
                    <p>Your account password has been updated successfully.</p>
                    <p>If this was not you, contact support immediately.</p>
                  </div>
                </body>
                </html>
                """;

        sendEmail(recipient, subject, plainText, html);
    }

    /*
     * PASSWORD RESET OTP EMAIL
     */
    public void sendPasswordResetOtp(
            String recipient,
            String otp,
            String expiresAt
    ) {

        String subject =
                "Mercedes-Benz - Password Reset Verification";

        String plainText = """
                Mercedes-Benz Account Security

                Your password reset verification code is: %s

                If you did not request a password reset, ignore this email.
                """.formatted(otp);

        String html = """
                <html>
                <body style="font-family:Arial;background:#f1f3f5;padding:30px;">
                  <div style="max-width:550px;margin:auto;background:#ffffff;
                              padding:35px;border-radius:10px;">
                    <h2 style="color:#111820;">Mercedes-Benz</h2>
                    <p>The Best or Nothing.</p>
                    <h3>Password Reset Verification</h3>
                    <p>Your verification code is:</p>
                    <h1 style="letter-spacing:8px;color:#111820;">%s</h1>
                    <p>If you did not request a reset, ignore this email.</p>
                  </div>
                </body>
                </html>
                """.formatted(otp);

        sendEmail(recipient, subject, plainText, html);
    }

    /*
     * VEHICLE HEALTH REPORT EMAIL
     *
     * Embeds:
     * - mercedes-logo.png
     * - g-wagon.jpg
     *
     * Attaches:
     * - Generated PDF report
     */
    public void sendVehicleHealthReport(
            String recipient,
            String subject,
            String plainText,
            String html,
            byte[] pdfBytes,
            String pdfFileName
    ) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(plainText, html);

            // Embed Mercedes-Benz logo in the email.
            helper.addInline(
                    "mercedesLogo",
                    new ClassPathResource(
                            "static/images/mercedes-logo.png"
                    ),
                    "image/png"
            );

            // Embed G-Wagon image in the email.
            helper.addInline(
                    "gWagon",
                    new ClassPathResource(
                            "static/images/g-wagon.jpg"
                    ),
                    "image/jpeg"
            );

            // Attach the generated PDF report.
            helper.addAttachment(
                    pdfFileName,
                    new ByteArrayResource(pdfBytes),
                    "application/pdf"
            );

            mailSender.send(message);

        } catch (MessagingException exception) {

            throw new IllegalStateException(
                    "Unable to send vehicle health report email",
                    exception
            );
        }
    }

    /*
     * COMMON EMAIL SENDER
     *
     * Used by password-related emails.
     */
    private void sendEmail(
            String recipient,
            String subject,
            String plainText,
            String html
    ) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(plainText, html);

            mailSender.send(message);

        } catch (MessagingException exception) {

            throw new IllegalStateException(
                    "Unable to send email",
                    exception
            );
        }
    }
}
