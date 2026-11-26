
package com.mercedes.telemetry.dto;

public class AuthResponse {

    private String message;
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UserResponse user;

    public AuthResponse(
            String message,
            String accessToken,
            long expiresIn,
            UserResponse user) {

        this.message = message;
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public UserResponse getUser() {
        return user;
    }
}
