
package com.mercedes.telemetry.dto;

public class PasswordChangeOtpEmailEvent {

    private String email;
    private String otp;
    private String expiresAt;

    public PasswordChangeOtpEmailEvent() {
    }

    public PasswordChangeOtpEmailEvent(
            String email,
            String otp,
            String expiresAt) {
        this.email = email;
        this.otp = otp;
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}
