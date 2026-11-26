
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByMobileNumber(String mobileNumber);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            UUID userId
    );

    boolean existsByMobileNumberAndIdNot(
            String mobileNumber,
            UUID userId
    );
}
