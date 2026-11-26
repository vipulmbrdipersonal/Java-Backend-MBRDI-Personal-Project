package com.mercedes.telemetry.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedes.telemetry.dto.VehicleHealthEvent;
import com.mercedes.telemetry.service.VehicleHealthReportService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VehicleHealthReportConsumer {

    private final ObjectMapper objectMapper;
    private final VehicleHealthReportService reportService;

    public VehicleHealthReportConsumer(
            ObjectMapper objectMapper,
            VehicleHealthReportService reportService
    ) {
        this.objectMapper = objectMapper;
        this.reportService = reportService;
    }

    @KafkaListener(
            topics = Topics.HEALTH_UPDATED,
            groupId = "vehicle-health-report-email-group"
    )
    public void consume(String message) throws Exception {

        VehicleHealthEvent event =
                objectMapper.readValue(
                        message,
                        VehicleHealthEvent.class
                );

        reportService.sendReport(event);
    }
}