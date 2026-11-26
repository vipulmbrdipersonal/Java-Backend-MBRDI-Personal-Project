
package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByCustomerEmailOrderByCreatedAtDesc(String email);
    Optional<Purchase> findByPurchaseNumber(String purchaseNumber);
}
