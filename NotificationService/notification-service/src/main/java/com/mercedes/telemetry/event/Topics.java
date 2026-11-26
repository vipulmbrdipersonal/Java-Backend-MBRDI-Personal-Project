package com.mercedes.telemetry.event;

public final class Topics {

    private Topics() {
    }

    public static final String HEALTH_ALERT =
            "vehicle.health.alert";

    public static final String HEALTH_UPDATED =
            "vehicle.health.updated";

    public static final String NOTIFICATION_CREATED =
            "notification.created";

    public static final String TOKEN_REVOKED =
            "auth.token.revoked";

    public static final String PASSWORD_CHANGE_OTP =
            "auth.password.otp.v1";

    public static final String PASSWORD_CHANGED =
            "auth.password.changed.v1";

    public static final String PASSWORD_RESET_OTP =
            "auth.password.reset.otp.v1";
}