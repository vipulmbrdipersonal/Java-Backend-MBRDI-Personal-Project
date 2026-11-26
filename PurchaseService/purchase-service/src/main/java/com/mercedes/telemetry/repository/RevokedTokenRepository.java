
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, String> {
}
