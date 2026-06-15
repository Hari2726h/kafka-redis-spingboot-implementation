package com.example.demo.controller;

import com.example.demo.dto.InventoryRequest;
import com.example.demo.dto.InventoryResponse;
import com.example.demo.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  InventoryController                                          ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  REST endpoints for stock management. Typically used by       ║
 * ║  admin/warehouse staff to manage inventory levels.            ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: MVC Controller (thin controller)             ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
@Tag(name = "Inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Initialize inventory for a product")
    public ResponseEntity<InventoryResponse> initializeInventory(
            @Valid @RequestBody InventoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.initializeInventory(request));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory for a product")
    public ResponseEntity<InventoryResponse> getByProductId(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(inventoryService.getByProductId(productId));
    }

    @PutMapping("/product/{productId}/add")
    @Operation(summary = "Add stock to product inventory")
    public ResponseEntity<InventoryResponse> addStock(
            @PathVariable Long productId,
            @RequestParam int quantity
    ) {
        return ResponseEntity.ok(inventoryService.addStock(productId, quantity));
    }

    @PutMapping("/product/{productId}/deduct")
    @Operation(summary = "Deduct stock from product inventory")
    public ResponseEntity<InventoryResponse> deductStock(
            @PathVariable Long productId,
            @RequestParam int quantity
    ) {
        return ResponseEntity.ok(inventoryService.deductStock(productId, quantity));
    }
}
