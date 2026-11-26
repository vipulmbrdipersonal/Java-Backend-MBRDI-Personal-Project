
package com.mercedes.dashboard.repository;

import com.mercedes.dashboard.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, String> {
}
