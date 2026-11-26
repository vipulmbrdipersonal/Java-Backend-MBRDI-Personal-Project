
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, String> {

    boolean existsByTokenId(String tokenId);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
