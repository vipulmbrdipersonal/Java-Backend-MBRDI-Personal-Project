
package com.mercedes.telemetry.dto;

import com.mercedes.telemetry.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private String mobileNumber;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private boolean hasProfilePhoto;

    public UserResponse() {
    }

    // Backward-compatible mapping.
    // Use this when the photo existence has not been checked.
    public static UserResponse from(User user) {
        return from(user, false);
    }

    // Mapping that includes profile photo availability.
    public static UserResponse from(
            User user,
            boolean hasProfilePhoto) {

        UserResponse response = new UserResponse();

        response.id = user.getId();
        response.name = user.getName();
        response.email = user.getEmail();
        response.mobileNumber = user.getMobileNumber();
        response.role = user.getRole().name();
        response.createdAt = user.getCreatedAt();
        response.lastLoginAt = user.getLastLoginAt();
        response.hasProfilePhoto = hasProfilePhoto;

        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public boolean isHasProfilePhoto() {
        return hasProfilePhoto;
    }

    public void setHasProfilePhoto(boolean hasProfilePhoto) {
        this.hasProfilePhoto = hasProfilePhoto;
    }
}
