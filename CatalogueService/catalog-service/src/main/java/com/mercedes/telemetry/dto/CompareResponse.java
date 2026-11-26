
package com.mercedes.telemetry.dto;

import java.util.List;

public class CompareResponse {

    private final int comparedModels;
    private final List<VehicleModelResponse> models;

    public CompareResponse(List<VehicleModelResponse> models) {
        this.models = List.copyOf(models);
        this.comparedModels = models.size();
    }

    public int getComparedModels() {
        return comparedModels;
    }

    public List<VehicleModelResponse> getModels() {
        return models;
    }
}
