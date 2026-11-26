
package com.mercedes.telemetry.seed;

import com.mercedes.telemetry.entity.ModelFeature;
import com.mercedes.telemetry.entity.VehicleModel;
import com.mercedes.telemetry.repository.VehicleModelRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class CatalogDataLoader implements CommandLineRunner {

    private final VehicleModelRepository repository;

    @Value("${app.catalog.seed-enabled:true}")
    private boolean seedEnabled;

    public CatalogDataLoader(VehicleModelRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (!seedEnabled) {
            return;
        }

        addIfMissing(
                "MB-C-2026",
                "C-Class",
                2026,
                "SEDAN",
                "PETROL",
                "2.0L Turbocharged",
                "2.0 L",
                "204 hp",
                "300 Nm",
                "9G-TRONIC Automatic",
                "Rear Wheel Drive",
                "14 km/l",
                "246 km/h",
                "7.3 seconds",
                5,
                "455 L",
                "6500000",
                "Luxury executive sedan with advanced driving assistance and connected technology.",
                List.of(
                        "MBUX Infotainment",
                        "Digital Instrument Cluster",
                        "Active Brake Assist",
                        "LED Headlamps",
                        "Panoramic Sunroof"
                )
        );

        addIfMissing(
                "MB-E-2026",
                "E-Class",
                2026,
                "SEDAN",
                "PETROL",
                "2.0L Turbocharged",
                "2.0 L",
                "204 hp",
                "320 Nm",
                "9G-TRONIC Automatic",
                "Rear Wheel Drive",
                "15 km/l",
                "240 km/h",
                "7.5 seconds",
                5,
                "540 L",
                "8000000",
                "Premium executive sedan with comfort-focused technology.",
                List.of(
                        "MBUX Superscreen",
                        "Burmester Sound System",
                        "Multicontour Seats",
                        "Driver Assistance Package",
                        "Ambient Lighting"
                )
        );

        addIfMissing(
                "MB-GLC-2026",
                "GLC",
                2026,
                "SUV",
                "PETROL",
                "2.0L Turbocharged",
                "2.0 L",
                "258 hp",
                "400 Nm",
                "9G-TRONIC Automatic",
                "4MATIC All Wheel Drive",
                "12 km/l",
                "240 km/h",
                "6.2 seconds",
                5,
                "620 L",
                "9500000",
                "Premium SUV combining performance, utility and Mercedes-Benz comfort.",
                List.of(
                        "4MATIC All Wheel Drive",
                        "MBUX Navigation",
                        "360 Degree Camera",
                        "Panoramic Sunroof",
                        "Active Parking Assist"
                )
        );

        addIfMissing(
                "MB-EQS-2026",
                "EQS",
                2026,
                "SEDAN",
                "ELECTRIC",
                "Electric Motor",
                "Electric",
                "360 hp",
                "568 Nm",
                "Single Speed Automatic",
                "Rear Wheel Drive",
                "Up to 800 km claimed range (variant dependent)",
                "210 km/h",
                "6.2 seconds",
                5,
                "610 L",
                "15000000",
                "All-electric luxury sedan with advanced electric mobility technology.",
                List.of(
                        "MBUX Hyperscreen",
                        "Electric Powertrain",
                        "Regenerative Braking",
                        "Advanced Driver Assistance",
                        "Premium Acoustic Comfort"
                )
        );
    }

    private void addIfMissing(
            String code,
            String name,
            Integer year,
            String body,
            String fuel,
            String engine,
            String capacity,
            String power,
            String torque,
            String transmission,
            String drivetrain,
            String mileage,
            String speed,
            String acceleration,
            Integer seats,
            String boot,
            String price,
            String description,
            List<String> features) {

        // Update an existing model if its image URL is missing.
        Optional<VehicleModel> existingModel =
                repository.findByModelCodeIgnoreCase(code);

        if (existingModel.isPresent()) {
            VehicleModel existing = existingModel.get();

            if (existing.getImageUrl() == null
                    || existing.getImageUrl().isBlank()) {

                existing.setImageUrl(getImageUrl(code));
                repository.save(existing);
            }

            return;
        }

        // Create a new catalogue model.
        VehicleModel model = new VehicleModel();

        model.setModelCode(code);
        model.setBrand("Mercedes-Benz");
        model.setModelName(name);
        model.setModelYear(year);
        model.setBodyType(body);
        model.setFuelType(fuel);
        model.setEngine(engine);
        model.setEngineCapacity(capacity);
        model.setPower(power);
        model.setTorque(torque);
        model.setTransmission(transmission);
        model.setDrivetrain(drivetrain);
        model.setMileage(mileage);
        model.setTopSpeed(speed);
        model.setAcceleration(acceleration);
        model.setSeatingCapacity(seats);
        model.setBootSpace(boot);
        model.setPrice(new BigDecimal(price));
        model.setCurrency("INR");

        // Save the frontend image path in the catalogue database.
        model.setImageUrl(getImageUrl(code));

        model.setDescription(description);
        model.setActive(true);

        model.setFeatures(
                features.stream()
                        .map(ModelFeature::new)
                        .toList()
        );

        repository.save(model);
    }

    private String getImageUrl(String code) {

        return switch (code) {
            case "MB-C-2026" ->
                    "/assets/images/c-class.jpg";

            case "MB-E-2026" ->
                    "/assets/images/e-class.jpg";

            case "MB-GLC-2026" ->
                    "/assets/images/glc.jpg";

            case "MB-EQS-2026" ->
                    "/assets/images/eqs.jpg";

            default -> null;
        };
    }
}
