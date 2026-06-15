package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  PaymentRequest DTO                                           ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Input payload for processing a payment. Takes the payment    ║
 * ║  method — the amount comes from the order automatically.      ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: DTO                                          ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Data
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Payment method is required (CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING)")
    private String paymentMethod;
}
