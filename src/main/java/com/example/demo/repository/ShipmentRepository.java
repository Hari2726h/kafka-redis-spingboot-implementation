package com.example.demo.repository;

import com.example.demo.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  ShipmentRepository                                           ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Data access for Shipment entities. Supports lookup by order  ║
 * ║  ID and tracking number (for customer tracking page).         ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Repository Pattern                           ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Find the shipment for a specific order (1:1).
     */
    Optional<Shipment> findByOrderId(Long orderId);

    /**
     * Find a shipment by its tracking number.
     * This is the primary lookup for customer-facing tracking.
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
