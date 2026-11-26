
package com.mercedes.telemetry.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

    @Id
    @Column(length = 100)
    private String jti;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    protected RevokedToken() {
    }

    public RevokedToken(String jti, Instant revokedAt) {
        this.jti = jti;
        this.revokedAt = revokedAt;
    }

    public String getJti() { return jti; }
}
