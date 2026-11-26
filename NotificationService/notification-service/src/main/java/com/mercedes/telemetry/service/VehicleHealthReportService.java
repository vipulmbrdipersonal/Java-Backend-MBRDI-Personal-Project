package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.VehicleHealthEvent;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class VehicleHealthReportService {

    private static final Logger log =
            LoggerFactory.getLogger(VehicleHealthReportService.class);

    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a",
                    Locale.ENGLISH
            ).withZone(ZoneId.systemDefault());

    private static final String LOGO_PATH =
            "static/images/mercedes-logo.png";

    public VehicleHealthReportService(
            TemplateEngine templateEngine,
            EmailService emailService
    ) {
        this.templateEngine = templateEngine;
        this.emailService = emailService;
    }

    /*
     * MAIN REPORT PROCESSING METHOD
     */
    public void sendReport(VehicleHealthEvent event) {

        validateEvent(event);

        if (!StringUtils.hasText(event.getOwnerEmail())) {

            log.warn(
                    "Health report skipped because owner email is missing. Vehicle ID: {}",
                    event.getVehicleId()
            );

            return;
        }

        try {

            Map<String, Object> report = buildReport(event);

            // Load the actual PNG logo and embed it as Base64.
            String logoDataUri = loadLogoDataUri();

            report.put("logoDataUri", logoDataUri);

            Context context = new Context(Locale.ENGLISH);
            context.setVariable("report", report);

            // Render email HTML.
            String emailHtml = templateEngine.process(
                    "vehicle-health-email",
                    context
            );

            // Render PDF HTML.
            String pdfHtml = templateEngine.process(
                    "vehicle-health-report",
                    context
            );

            // Generate and validate the one-page PDF.
            byte[] pdfBytes = generatePdf(pdfHtml);

            String pdfFileName =
                    "Mercedes-Benz-Health-Report-"
                            + event.getVehicleId()
                            + ".pdf";

            String subject =
                    "Mercedes-Benz | Vehicle Health Report | "
                            + event.getStatus();

            String plainText = buildPlainText(report);

            // Send email with embedded email images and PDF attachment.
            emailService.sendVehicleHealthReport(
                    event.getOwnerEmail(),
                    subject,
                    plainText,
                    emailHtml,
                    pdfBytes,
                    pdfFileName
            );

            log.info(
                    "Vehicle health report email sent successfully. Vehicle ID: {}, Status: {}",
                    event.getVehicleId(),
                    event.getStatus()
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to generate or send health report for vehicle ID: {}",
                    event.getVehicleId(),
                    exception
            );

            throw new IllegalStateException(
                    "Vehicle health report processing failed",
                    exception
            );
        }
    }

    /*
     * LOAD MERCEDES LOGO
     *
     * Reads the PNG from the application classpath and converts it
     * into a Base64 data URI so the PDF does not depend on a file URL.
     */
    private String loadLogoDataUri() throws Exception {

        ClassPathResource logoResource =
                new ClassPathResource(LOGO_PATH);

        if (!logoResource.exists()) {

            throw new IllegalStateException(
                    "Mercedes logo not found at classpath location: "
                            + LOGO_PATH
            );
        }

        try (InputStream inputStream = logoResource.getInputStream()) {

            byte[] logoBytes = inputStream.readAllBytes();

            if (logoBytes.length == 0) {

                throw new IllegalStateException(
                        "Mercedes logo file is empty: " + LOGO_PATH
                );
            }

            String base64Logo =
                    Base64.getEncoder().encodeToString(logoBytes);

            log.info("Mercedes logo loaded successfully for PDF generation.");

            return "data:image/png;base64," + base64Logo;
        }
    }

    /*
     * VALIDATE EVENT
     */
    private void validateEvent(VehicleHealthEvent event) {

        if (event == null) {

            throw new IllegalArgumentException(
                    "Vehicle health event cannot be null"
            );
        }

        if (event.getEventId() == null
                || event.getVehicleId() == null
                || event.getVin() == null
                || event.getStatus() == null) {

            throw new IllegalArgumentException(
                    "Vehicle health event is missing required information"
            );
        }
    }

    /*
     * BUILD REPORT DATA
     */
    private Map<String, Object> buildReport(
            VehicleHealthEvent event
    ) {

        Map<String, Object> report = new LinkedHashMap<>();

        report.put("eventId", event.getEventId());
        report.put("telemetryEventId", event.getTelemetryEventId());

        report.put("vehicleId", event.getVehicleId());
        report.put("vin", event.getVin());

        report.put("healthScore", event.getHealthScore());

        String normalizedStatus =
                normalizeStatus(event.getStatus());

        report.put("status", normalizedStatus);

        report.put(
                "assessedAtText",
                event.getAssessedAt() != null
                        ? DATE_FORMATTER.format(event.getAssessedAt())
                        : "Not available"
        );

        report.put("speed", event.getSpeed());
        report.put("engineTemperature", event.getEngineTemperature());
        report.put("fuelLevel", event.getFuelLevel());
        report.put("batteryVoltage", event.getBatteryVoltage());
        report.put("odometer", event.getOdometer());
        report.put("rpm", event.getRpm());

        // Build tyre pressure information.
        List<Map<String, Object>> tyres = new ArrayList<>();

        tyres.add(createTyre(
                "Front Left",
                event.getTyrePressureFrontLeft()
        ));

        tyres.add(createTyre(
                "Front Right",
                event.getTyrePressureFrontRight()
        ));

        tyres.add(createTyre(
                "Rear Left",
                event.getTyrePressureRearLeft()
        ));

        tyres.add(createTyre(
                "Rear Right",
                event.getTyrePressureRearRight()
        ));

        report.put("tyres", tyres);

        // Health issues.
        List<String> issues = event.getIssues() == null
                ? List.of()
                : event.getIssues();

        report.put("issues", issues);

        // Recommended action.
        report.put(
                "recommendation",
                getRecommendation(
                        normalizedStatus,
                        issues
                )
        );

        // Status color.
        report.put(
                "statusColor",
                getStatusColor(normalizedStatus)
        );

        return report;
    }

    /*
     * CREATE TYRE DATA
     */
    private Map<String, Object> createTyre(
            String position,
            Double pressure
    ) {

        Map<String, Object> tyre = new LinkedHashMap<>();

        tyre.put("position", position);
        tyre.put("pressure", pressure);

        return tyre;
    }

    /*
     * NORMALIZE HEALTH STATUS
     */
    private String normalizeStatus(String status) {

        if (status == null) {
            return "WARNING";
        }

        String normalized =
                status.trim().toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "HEALTHY", "WARNING", "CRITICAL" -> normalized;
            default -> "WARNING";
        };
    }

    /*
     * STATUS COLOR
     */
    private String getStatusColor(String status) {

        return switch (status) {
            case "HEALTHY" -> "#27D17F";
            case "CRITICAL" -> "#FF5757";
            default -> "#F4B942";
        };
    }

    /*
     * RECOMMENDATION
     */
    private String getRecommendation(
            String status,
            List<String> issues
    ) {

        if ("HEALTHY".equals(status) && issues.isEmpty()) {

            return "Your vehicle is operating within the configured health assessment thresholds. Continue regular maintenance and periodic health monitoring.";
        }

        if ("CRITICAL".equals(status)) {

            return "Critical vehicle conditions have been detected. Arrange an inspection at an authorized service centre as soon as possible.";
        }

        return "Warning conditions have been detected. Review the reported issues and arrange the recommended maintenance or inspection.";
    }

    /*
     * PLAIN TEXT EMAIL CONTENT
     */
    private String buildPlainText(Map<String, Object> report) {

        return """
                MERCEDES-BENZ
                The Best or Nothing.

                VEHICLE HEALTH REPORT

                Status: %s
                Health Score: %s/100

                Vehicle ID: %s
                VIN: %s
                Assessment Date: %s

                A detailed vehicle health report is attached as a PDF.

                Mercedes-Benz Vehicle Intelligence Platform
                """.formatted(
                report.get("status"),
                report.get("healthScore"),
                report.get("vehicleId"),
                report.get("vin"),
                report.get("assessedAtText")
        );
    }

    /*
     * GENERATE AND VALIDATE ONE-PAGE PDF
     */
    private byte[] generatePdf(String html) throws Exception {

        byte[] pdfBytes;

        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            PdfRendererBuilder builder =
                    new PdfRendererBuilder();

            builder.useFastMode();

            builder.withHtmlContent(
                    html,
                    null
            );

            builder.toStream(outputStream);

            builder.run();

            pdfBytes = outputStream.toByteArray();
        }

        // Verify the actual generated PDF page count.
        try (PDDocument document = PDDocument.load(pdfBytes)) {

            int pageCount = document.getNumberOfPages();

            log.info(
                    "Generated vehicle health PDF. Page count: {}",
                    pageCount
            );

            if (pageCount != 1) {

                throw new IllegalStateException(
                        "Vehicle health PDF must contain exactly one A4 page, "
                                + "but generated PDF contains "
                                + pageCount
                                + " pages. Reduce report content or layout spacing."
                );
            }
        }

        return pdfBytes;
    }
}
