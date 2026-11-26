
package com.mercedes.telemetry.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;

@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        try {
            byte[] decoded = Base64.getDecoder().decode(secret);
            if (decoded.length < 32) {
                throw new IllegalArgumentException(
                        "JWT secret must contain at least 256 bits");
            }
            this.signingKey = Keys.hmacShaKeyFor(decoded);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "security.jwt.secret must be a Base64-encoded key of at least 32 bytes",
                    ex);
        }
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            Claims claims = parse(token);
            return claims.getSubject() != null
                    && claims.getId() != null
                    && claims.get("role", String.class) != null;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
