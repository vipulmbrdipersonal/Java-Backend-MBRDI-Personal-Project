package com.mercedes.vehicle.dto;
import jakarta.validation.constraints.*;
public record CreateVehicleRequest(@NotBlank @Size(max=120) String model, @Size(max=80) String modelId, @Min(1990) @Max(2100) Integer modelYear) {}
