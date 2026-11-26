
package com.mercedes.telemetry.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

    @Id
    private String tokenId;

    private LocalDateTime revokedAt;

    protected RevokedToken() {}

    public RevokedToken(String tokenId) {
        this.tokenId = tokenId;
        this.revokedAt = LocalDateTime.now();
    }

    public String getTokenId() {
        return tokenId;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
}
