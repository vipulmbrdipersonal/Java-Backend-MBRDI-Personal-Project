
package com.mercedes.telemetry.event;

public final class Topics {

    private Topics() {
    }

    public static final String TELEMETRY_RAW = "telemetry.raw";
    public static final String HEALTH_UPDATED = "vehicle.health.updated";
    public static final String HEALTH_ALERT = "vehicle.health.alert";
    public static final String TOKEN_REVOKED = "auth.token.revoked";
}
