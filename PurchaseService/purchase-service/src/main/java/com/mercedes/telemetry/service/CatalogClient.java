
package com.mercedes.telemetry.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedes.telemetry.dto.CatalogVehicleResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class CatalogClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String catalogBaseUrl;

    public CatalogClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${services.catalog.base-url}") String catalogBaseUrl) {

        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.catalogBaseUrl = catalogBaseUrl.replaceAll("/+$", "");
    }

    public CatalogVehicleResponse getVehicle(String id) {

        if (id == null || id.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A catalog vehicle ID is required"
            );
        }

        UUID catalogId;

        try {
            catalogId = UUID.fromString(id.trim());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid catalog vehicle ID"
            );
        }

        try {
            String url = catalogBaseUrl
                    + "/api/catalog/models/"
                    + catalogId;

            JsonNode response = restTemplate.getForObject(
                    url,
                    JsonNode.class
            );

            JsonNode data = response == null
                    ? null
                    : response.get("data");

            if (data == null || data.isNull()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Catalog Service returned an empty response"
                );
            }

            CatalogVehicleResponse vehicle =
                    objectMapper.convertValue(
                            data,
                            CatalogVehicleResponse.class
                    );

            if (vehicle.id() == null
                    || vehicle.modelName() == null
                    || vehicle.modelName().isBlank()
                    || vehicle.price() == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Catalog Service returned incomplete vehicle details"
                );
            }

            if (vehicle.price().signum() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Catalog Service returned an invalid vehicle price"
                );
            }

            return vehicle;

        } catch (HttpClientErrorException.NotFound ex) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Vehicle model not found in catalog"
            );

        } catch (ResponseStatusException ex) {

            throw ex;

        } catch (RestClientException ex) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Catalog Service is unavailable"
            );
        }
    }
}
