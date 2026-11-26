
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPurchaseId(Long purchaseId);
}
