
package com.mercedes.dashboard.event;

public final class Topics {

    private Topics() {
    }

    public static final String HEALTH_UPDATED = "vehicle.health.updated";
    public static final String NOTIFICATION_CREATED = "notification.created";
    public static final String TOKEN_REVOKED = "auth.token.revoked";
}
