package com.example.demo.dto;

import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  PaymentResponse DTO                                          ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Returns payment details to the client. Includes both the     ║
 * ║  transaction status and the associated order information.     ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: DTO                                          ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Data
@Builder
public class PaymentResponse {

    private Long id;
    private String transactionId;
    private Long orderId;
    private Double amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public static PaymentResponse fromEntity(com.example.demo.entity.Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .transactionId(payment.getTransactionId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .method(payment.getMethod())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
