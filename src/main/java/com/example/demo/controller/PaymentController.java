package com.example.demo.controller;

import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.PaymentResponse;
import com.example.demo.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  PaymentController                                            ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  REST endpoints for payment processing. In this project,     ║
 * ║  payments are simulated — no real gateway integration.        ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: MVC Controller (thin controller)             ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Process payment for an order (simulated)")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.processPayment(request));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment status for an order")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }
}
