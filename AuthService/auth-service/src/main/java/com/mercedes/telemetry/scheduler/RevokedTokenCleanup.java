
package com.mercedes.telemetry.scheduler;

import com.mercedes.telemetry.repository.RevokedTokenRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class RevokedTokenCleanup {

    private final RevokedTokenRepository repository;

    public RevokedTokenCleanup(RevokedTokenRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
