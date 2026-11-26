
package com.mercedes.telemetry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PurchaseRequest(

        @NotBlank
        @Size(max = 36)
        String vehicleCatalogId,

        String paymentMethod,

        @NotBlank
        @Size(max = 40)
        String selectedColor

) {}
