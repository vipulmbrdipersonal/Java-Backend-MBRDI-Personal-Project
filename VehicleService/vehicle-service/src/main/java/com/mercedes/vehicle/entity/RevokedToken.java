package com.mercedes.vehicle.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="revoked_tokens")
public class RevokedToken {
 @Id @Column(name="token_id",length=100) private String tokenId;
 @Column(name="expires_at") private Instant expiresAt;
 @Column(name="revoked_at",nullable=false) private Instant revokedAt;
 protected RevokedToken(){}
 public RevokedToken(String tokenId,Instant expiresAt,Instant revokedAt){this.tokenId=tokenId;this.expiresAt=expiresAt;this.revokedAt=revokedAt;}
 public String getTokenId(){return tokenId;} public Instant getExpiresAt(){return expiresAt;}
}
