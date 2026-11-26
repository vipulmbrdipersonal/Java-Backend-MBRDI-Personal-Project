
package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.TelemetryEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HealthAssessmentService {

    public Assessment assess(TelemetryEvent telemetry) {

        List<String> issues = new ArrayList<>();
        boolean critical = false;
        int score = 100;

        Double temperature = telemetry.getEngineTemperature();

        if (temperature != null && temperature >= 120) {
            issues.add("CRITICAL: Engine temperature is extremely high");
            score -= 25;
            critical = true;
        } else if (temperature != null && temperature >= 105) {
            issues.add("WARNING: Engine temperature is above normal");
            score -= 10;
        }

        Double voltage = telemetry.getBatteryVoltage();

        if (voltage != null && (voltage < 10.5 || voltage > 16.0)) {
            issues.add("CRITICAL: Battery voltage is outside the critical range");
            score -= 25;
            critical = true;
        } else if (voltage != null &&
                (voltage < 11.8 || voltage > 15.0)) {
            issues.add("WARNING: Battery voltage requires inspection");
            score -= 10;
        }

        Double fuel = telemetry.getFuelLevel();

        if (fuel != null && fuel <= 10) {
            issues.add("WARNING: Fuel level is low");
            score -= 10;
        }

        critical |= checkTyre(
                "Front-left", telemetry.getTyrePressureFrontLeft(), issues);
        critical |= checkTyre(
                "Front-right", telemetry.getTyrePressureFrontRight(), issues);
        critical |= checkTyre(
                "Rear-left", telemetry.getTyrePressureRearLeft(), issues);
        critical |= checkTyre(
                "Rear-right", telemetry.getTyrePressureRearRight(), issues);

        Integer rpm = telemetry.getRpm();

        if (rpm != null && rpm > 7000) {
            issues.add("CRITICAL: Engine RPM exceeds the configured limit");
            score -= 25;
            critical = true;
        } else if (rpm != null && rpm > 6000) {
            issues.add("WARNING: Engine RPM is unusually high");
            score -= 10;
        }

        score = Math.max(0, score);

        String status;
        if (critical) {
            status = "CRITICAL";
        } else if (!issues.isEmpty()) {
            status = "WARNING";
        } else {
            status = "HEALTHY";
        }

        return new Assessment(score, status, issues);
    }

    private boolean checkTyre(
            String position, Double pressure, List<String> issues) {

        if (pressure == null) {
            return false;
        }

        if (pressure < 20 || pressure > 45) {
            issues.add("CRITICAL: " + position +
                    " tyre pressure is dangerously abnormal");
            return true;
        }

        if (pressure < 25 || pressure > 40) {
            issues.add("WARNING: " + position +
                    " tyre pressure requires attention");
        }

        return false;
    }

    public record Assessment(
            int score,
            String status,
            List<String> issues) {
    }
}
