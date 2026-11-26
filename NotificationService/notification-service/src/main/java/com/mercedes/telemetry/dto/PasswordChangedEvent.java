
package com.mercedes.telemetry.dto;

public class PasswordChangedEvent {

    private String email;
    private String changedAt;

    public PasswordChangedEvent() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(String changedAt) {
        this.changedAt = changedAt;
    }
}
