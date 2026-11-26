
package com.mercedes.telemetry.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class ModelFeature {

    private String featureName;

    public ModelFeature() {
    }

    public ModelFeature(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }
}
