
package com.mercedes.telemetry.controller;

import com.mercedes.telemetry.dto.PurchaseRequest;
import com.mercedes.telemetry.dto.PurchaseResponse;
import com.mercedes.telemetry.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse createPurchase(
            @Valid @RequestBody PurchaseRequest request,
            Authentication authentication) {

        String userId = getUserId(authentication);

        return purchaseService.createPurchase(
                authentication.getName(),
                userId,
                request
        );
    }

    @GetMapping("/my")
    public List<PurchaseResponse> getMyPurchases(
            Authentication authentication) {

        return purchaseService.getMyPurchases(
                authentication.getName()
        );
    }

    @GetMapping("/{purchaseNumber}")
    public PurchaseResponse getPurchase(
            @PathVariable String purchaseNumber,
            Authentication authentication) {

        boolean admin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        return purchaseService.getPurchase(
                purchaseNumber,
                authentication.getName(),
                admin
        );
    }

    private String getUserId(Authentication authentication) {

        Object details = authentication.getDetails();

        if (details instanceof Map<?, ?> detailsMap) {
            Object userId = detailsMap.get("userId");

            if (userId != null && !userId.toString().isBlank()) {
                return userId.toString();
            }
        }

        throw new IllegalStateException(
                "Authenticated JWT does not contain a valid userId"
        );
    }
}
