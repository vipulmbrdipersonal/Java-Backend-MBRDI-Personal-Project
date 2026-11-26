
package com.mercedes.telemetry.event;

public final class PasswordEmailTopics {

    private PasswordEmailTopics() {
    }

    public static final String PASSWORD_CHANGE_OTP =
            "auth.password.otp.v1";

    public static final String PASSWORD_CHANGED =
            "auth.password.changed.v1";

    public static final String PASSWORD_RESET_OTP =
            "auth.password.reset.otp.v1";
}
