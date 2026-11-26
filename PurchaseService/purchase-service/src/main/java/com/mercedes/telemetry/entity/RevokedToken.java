
package com.mercedes.telemetry.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

    @Id
    @Column(length = 150)
    private String tokenId;

    @Column(nullable = false)
    private LocalDateTime revokedAt;

    public RevokedToken() {}

    public RevokedToken(String tokenId) {
        this.tokenId = tokenId;
        this.revokedAt = LocalDateTime.now();
    }

    public String getTokenId() { return tokenId; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
}
