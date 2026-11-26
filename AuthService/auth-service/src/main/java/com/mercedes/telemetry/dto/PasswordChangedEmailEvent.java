
package com.mercedes.telemetry.dto;

public class PasswordChangedEmailEvent {

    private String email;
    private String changedAt;

    public PasswordChangedEmailEvent() {
    }

    public PasswordChangedEmailEvent(String email, String changedAt) {
        this.email = email;
        this.changedAt = changedAt;
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
