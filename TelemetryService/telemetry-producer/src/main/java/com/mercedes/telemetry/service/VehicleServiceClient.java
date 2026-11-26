package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.VehicleDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

@Component
public class VehicleServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.vehicle-service.url:http://localhost:8086}")
    private String vehicleServiceUrl;

    public VehicleServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public VehicleDetails getOwnedVehicle(
            String vehicleCode,
            String authorizationHeader
    ) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication token is required"
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            VehicleDetails vehicle = restTemplate.exchange(
                    vehicleServiceUrl + "/api/vehicles/{vehicleCode}",
                    HttpMethod.GET,
                    request,
                    VehicleDetails.class,
                    vehicleCode
            ).getBody();

            if (vehicle == null
                    || vehicle.id() == null
                    || vehicle.vin() == null
                    || vehicle.ownerEmail() == null
                    || vehicle.ownerEmail().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Vehicle Service returned incomplete vehicle details"
                );
            }

            return vehicle;

        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "Vehicle ownership verification failed"
            );
        }
    }
}