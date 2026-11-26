package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.TelemetryEvent;
import com.mercedes.telemetry.service.HealthAssessmentService.Assessment;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class HealthEmailService {

    private static final Logger log =
            LoggerFactory.getLogger(HealthEmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@mercedes.local}")
    private String fromAddress;

    @Value("${app.mail.from-name:Mercedes-Benz Vehicle Health Monitoring}")
    private String fromName;

    public HealthEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendHealthReport(
            TelemetryEvent telemetry,
            Assessment assessment
    ) {
        String recipient = telemetry.getOwnerEmail();

        if (recipient == null || recipient.isBlank()) {
            throw new IllegalStateException(
                    "Cannot send health email: vehicle owner email is missing"
            );
        }

        String status = assessment.status();

        String subject = switch (status) {
            case "CRITICAL" ->
                    "URGENT: Mercedes-Benz Vehicle Health Alert";
            case "WARNING" ->
                    "Attention Required: Mercedes-Benz Vehicle Health";
            default ->
                    "Mercedes-Benz Vehicle Health Report: Healthy";
        };

        String message = buildMessage(telemetry, assessment);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(message, false);

            mailSender.send(mimeMessage);

            log.info(
                    "Health email sent. vehicleId={}, status={}, recipient={}",
                    telemetry.getVehicleId(),
                    status,
                    recipient
            );

        } catch (Exception ex) {
            log.error(
                    "Failed to send health email. vehicleId={}, status={}",
                    telemetry.getVehicleId(),
                    status,
                    ex
            );

            throw new IllegalStateException(
                    "Health email delivery failed",
                    ex
            );
        }
    }

    private String buildMessage(
            TelemetryEvent telemetry,
            Assessment assessment
    ) {
        StringBuilder body = new StringBuilder();

        body.append("Mercedes-Benz Vehicle Health Monitoring\n");
        body.append("========================================\n\n");

        body.append("Dear Mercedes-Benz Customer,\n\n");

        body.append("Your vehicle's latest health assessment is complete.\n\n");

        body.append("VEHICLE DETAILS\n");
        body.append("------------------------------\n");
        body.append("Vehicle ID: ")
                .append(telemetry.getVehicleId()).append("\n");
        body.append("VIN: ")
                .append(telemetry.getVin()).append("\n\n");

        body.append("HEALTH ASSESSMENT\n");
        body.append("------------------------------\n");
        body.append("Status: ")
                .append(assessment.status()).append("\n");
        body.append("Health Score: ")
                .append(assessment.score()).append("/100\n\n");

        body.append("SENSOR READINGS\n");
        body.append("------------------------------\n");
        body.append("Engine Temperature: ")
                .append(telemetry.getEngineTemperature()).append(" °C\n");
        body.append("Battery Voltage: ")
                .append(telemetry.getBatteryVoltage()).append(" V\n");
        body.append("Fuel Level: ")
                .append(telemetry.getFuelLevel()).append(" %\n");
        body.append("RPM: ")
                .append(telemetry.getRpm()).append("\n");
        body.append("Front Left Tyre: ")
                .append(telemetry.getTyrePressureFrontLeft()).append(" PSI\n");
        body.append("Front Right Tyre: ")
                .append(telemetry.getTyrePressureFrontRight()).append(" PSI\n");
        body.append("Rear Left Tyre: ")
                .append(telemetry.getTyrePressureRearLeft()).append(" PSI\n");
        body.append("Rear Right Tyre: ")
                .append(telemetry.getTyrePressureRearRight()).append(" PSI\n\n");

        body.append("ASSESSMENT DETAILS\n");
        body.append("------------------------------\n");

        if (assessment.issues().isEmpty()) {
            body.append("No issues detected. Your vehicle is operating normally.\n");
        } else {
            for (String issue : assessment.issues()) {
                body.append("- ").append(issue).append("\n");
            }
        }

        body.append("\nRECOMMENDATION\n");
        body.append("------------------------------\n");

        switch (assessment.status()) {
            case "CRITICAL" -> body.append(
                    "Your vehicle has a critical health condition. "
                            + "Please arrange an inspection as soon as possible.\n"
            );
            case "WARNING" -> body.append(
                    "Some vehicle readings require attention. "
                            + "Please arrange an inspection when convenient.\n"
            );
            default -> body.append(
                    "Your vehicle is healthy based on the latest simulated "
                            + "telemetry readings.\n"
            );
        }

        body.append("\nThis is an automatically generated development email "
                + "from Mercedes-Benz Vehicle Health Monitoring.\n");

        return body.toString();
    }
}