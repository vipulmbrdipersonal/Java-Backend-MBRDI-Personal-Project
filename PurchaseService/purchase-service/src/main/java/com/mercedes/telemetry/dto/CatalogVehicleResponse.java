
package com.mercedes.telemetry.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CatalogVehicleResponse(
        UUID id,
        String modelName,
        Integer modelYear,
        BigDecimal price
) {}
