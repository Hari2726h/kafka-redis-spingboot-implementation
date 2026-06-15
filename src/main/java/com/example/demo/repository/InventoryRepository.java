package com.example.demo.repository;

import com.example.demo.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  InventoryRepository                                          ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Data access for Inventory entities. The key query is         ║
 * ║  findByProductId — since inventory is 1:1 with product, we    ║
 * ║  look up stock by product ID, not by inventory ID.            ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Repository Pattern                           ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory record by product ID.
     * This is the primary lookup method — we always query stock by product.
     */
    Optional<Inventory> findByProductId(Long productId);

    /**
     * Check if inventory already exists for a product.
     * Used to prevent duplicate inventory creation.
     */
    boolean existsByProductId(Long productId);
}
