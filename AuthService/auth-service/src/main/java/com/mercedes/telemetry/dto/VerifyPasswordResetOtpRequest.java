
package com.mercedes.telemetry.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyPasswordResetOtpRequest {

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please provide a valid email address.")
    private String email;

    @NotBlank(message = "OTP is required.")
    @Pattern(
            regexp = "\\d{6}",
            message = "OTP must contain exactly 6 digits."
    )
    private String otp;

    public VerifyPasswordResetOtpRequest() {
    }

    public VerifyPasswordResetOtpRequest(
            String email,
            String otp
    ) {
        this.email = email;
        this.otp = otp;
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
}
