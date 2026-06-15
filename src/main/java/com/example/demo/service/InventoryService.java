package com.example.demo.service;

import com.example.demo.cache.InventoryCacheService;
import com.example.demo.dto.InventoryRequest;
import com.example.demo.dto.InventoryResponse;
import com.example.demo.entity.Inventory;
import com.example.demo.entity.Product;
import com.example.demo.enums.ActionType;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.kafka.DomainEventProducer;
import com.example.demo.kafka.HistoryEvent;
import com.example.demo.kafka.HistoryProducer;
import com.example.demo.repository.InventoryRepository;
import com.example.demo.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  InventoryService                                             ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Manages stock quantities for products. This is a CRITICAL    ║
 * ║  service — incorrect stock management leads to overselling    ║
 * ║  or negative inventory.                                       ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - initializeInventory: creates stock record for a product    ║
 * ║  - addStock: increases quantity (restock from warehouse)      ║
 * ║  - deductStock: decreases quantity (order placed)             ║
 * ║  - reserveStock: marks stock as reserved for pending orders   ║
 * ║  - releaseStock: releases reserved stock (order cancelled)    ║
 * ║  - All changes are cached in Redis and audited via Kafka      ║
 * ║                                                               ║
 * ║  BUSINESS RULES:                                              ║
 * ║  1. Cannot deduct more stock than available                   ║
 * ║  2. Cannot reserve more stock than available                  ║
 * ║  3. Available = quantity - reservedQuantity                   ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Service Layer Pattern                                      ║
 * ║  - Cache-Aside (via InventoryCacheService)                    ║
 * ║  - Observer (Kafka domain events + audit events)              ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryCacheService inventoryCacheService;
    private final HistoryProducer historyProducer;
    private final DomainEventProducer domainEventProducer;

    // ─── INITIALIZE ──────────────────────────────────────────────

    /**
     * Creates an inventory record for a product.
     * Must be called once per product before stock operations.
     */
    @Transactional
    public InventoryResponse initializeInventory(InventoryRequest request) {
        Long productId = request.getProductId();

        // Verify product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Prevent duplicate inventory
        if (inventoryRepository.existsByProductId(productId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inventory already exists for product " + productId);
        }

        Inventory inventory = Inventory.builder()
                .product(product)
                .quantity(request.getQuantity())
                .reservedQuantity(0)
                .build();

        Inventory saved = inventoryRepository.save(inventory);
        inventoryCacheService.cacheInventory(productId, saved);
        publishHistory(saved, ActionType.CREATE);
        publishDomainEvent(saved, "INVENTORY_INITIALIZED");

        log.info("Initialized inventory for product {}: quantity={}", productId, request.getQuantity());
        return InventoryResponse.fromEntity(saved);
    }

    // ─── READ ────────────────────────────────────────────────────

    /**
     * Gets inventory for a product. Checks Redis cache first.
     */
    public InventoryResponse getByProductId(Long productId) {
        Inventory inventory = findInventoryOrThrow(productId);
        return InventoryResponse.fromEntity(inventory);
    }

    // ─── ADD STOCK ───────────────────────────────────────────────

    /**
     * Adds stock to inventory (e.g., warehouse restock).
     * Always succeeds — you can always add more stock.
     */
    @Transactional
    public InventoryResponse addStock(Long productId, int quantityToAdd) {
        Inventory inventory = findInventoryOrThrow(productId);
        int before = inventory.getQuantity();

        inventory.setQuantity(before + quantityToAdd);
        Inventory saved = inventoryRepository.save(inventory);

        inventoryCacheService.evictInventory(productId);
        publishHistory(saved, ActionType.UPDATE);
        publishDomainEvent(saved, "STOCK_ADDED");

        log.info("Added {} stock to product {}: {} → {}", quantityToAdd, productId, before, saved.getQuantity());
        return InventoryResponse.fromEntity(saved);
    }

    // ─── DEDUCT STOCK ────────────────────────────────────────────

    /**
     * Deducts stock from inventory (e.g., order shipped).
     * Throws InsufficientStockException if not enough stock.
     */
    @Transactional
    public InventoryResponse deductStock(Long productId, int quantityToDeduct) {
        Inventory inventory = findInventoryOrThrow(productId);
        int available = inventory.getAvailableQuantity();

        if (available < quantityToDeduct) {
            throw new InsufficientStockException(productId, quantityToDeduct, available);
        }

        int before = inventory.getQuantity();
        inventory.setQuantity(before - quantityToDeduct);
        Inventory saved = inventoryRepository.save(inventory);

        inventoryCacheService.evictInventory(productId);
        publishHistory(saved, ActionType.UPDATE);
        publishDomainEvent(saved, "STOCK_DEDUCTED");

        log.info("Deducted {} stock from product {}: {} → {}",
                quantityToDeduct, productId, before, saved.getQuantity());
        return InventoryResponse.fromEntity(saved);
    }

    // ─── RESERVE STOCK ───────────────────────────────────────────

    /**
     * Reserves stock for a pending order. Reserved stock cannot be
     * sold to other customers.
     *
     * Called by OrderService when an order is created.
     */
    @Transactional
    public void reserveStock(Long productId, int quantityToReserve) {
        Inventory inventory = findInventoryOrThrow(productId);
        int available = inventory.getAvailableQuantity();

        if (available < quantityToReserve) {
            throw new InsufficientStockException(productId, quantityToReserve, available);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantityToReserve);
        inventoryRepository.save(inventory);
        inventoryCacheService.evictInventory(productId);

        log.info("Reserved {} stock for product {}", quantityToReserve, productId);
    }

    /**
     * Releases reserved stock (e.g., order cancelled).
     * Also reduces the reserved quantity.
     */
    @Transactional
    public void releaseStock(Long productId, int quantityToRelease) {
        Inventory inventory = findInventoryOrThrow(productId);

        int newReserved = Math.max(0, inventory.getReservedQuantity() - quantityToRelease);
        inventory.setReservedQuantity(newReserved);
        inventoryRepository.save(inventory);
        inventoryCacheService.evictInventory(productId);

        log.info("Released {} reserved stock for product {}", quantityToRelease, productId);
    }

    /**
     * Confirms reserved stock (order shipped) — reduces both
     * reserved and total quantity.
     */
    @Transactional
    public void confirmReservedStock(Long productId, int quantity) {
        Inventory inventory = findInventoryOrThrow(productId);

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        inventoryRepository.save(inventory);
        inventoryCacheService.evictInventory(productId);

        log.info("Confirmed {} stock for product {} (shipped)", quantity, productId);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private Inventory findInventoryOrThrow(Long productId) {
        // Try cache first
        return inventoryCacheService.getInventory(productId)
                .orElseGet(() -> {
                    Inventory inv = inventoryRepository.findByProductId(productId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Inventory", "productId", productId));
                    inventoryCacheService.cacheInventory(productId, inv);
                    return inv;
                });
    }

    private void publishHistory(Inventory inventory, ActionType actionType) {
        historyProducer.sendEvent(new HistoryEvent(
                Inventory.class.getSimpleName(),
                inventory.getId(),
                actionType.name()
        ));
    }

    private void publishDomainEvent(Inventory inventory, String eventType) {
        String json = String.format(
                "{\"type\":\"%s\",\"productId\":%d,\"quantity\":%d,\"reservedQuantity\":%d,\"availableQuantity\":%d,\"timestamp\":\"%s\"}",
                eventType,
                inventory.getProduct().getId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(),
                LocalDateTime.now()
        );
        domainEventProducer.publish("inventory-events", json);
    }
}
