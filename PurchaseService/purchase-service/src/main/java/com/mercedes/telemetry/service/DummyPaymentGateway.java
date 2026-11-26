
package com.mercedes.telemetry.service;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class DummyPaymentGateway {

    public PaymentResult process(BigDecimal amount, String paymentMethod) {
        String transactionId = "TXN-" +
                UUID.randomUUID().toString().replace("-", "").toUpperCase();

        if (amount == null || amount.signum() <= 0) {
            return new PaymentResult(false, transactionId,
                    "Invalid payment amount");
        }

        if ("FAIL".equalsIgnoreCase(paymentMethod)
                || "DECLINED".equalsIgnoreCase(paymentMethod)) {
            return new PaymentResult(false, transactionId,
                    "Payment declined by simulated payment gateway");
        }

        return new PaymentResult(true, transactionId,
                "Payment approved by simulated payment gateway");
    }

    public record PaymentResult(
            boolean successful,
            String transactionId,
            String message
    ) {}
}
