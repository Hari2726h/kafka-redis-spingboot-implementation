package com.example.demo.cache;

import com.example.demo.entity.Inventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  InventoryCacheService                                        ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Caches inventory data in Redis to reduce DB load. Inventory  ║
 * ║  lookups are frequent (every "add to cart" checks stock) and  ║
 * ║  caching prevents hammering the database.                     ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  Uses the "Cache-Aside" pattern (same as ProductCacheService):║
 * ║  1. Check Redis first                                         ║
 * ║  2. If cache miss → load from database                       ║
 * ║  3. Store in Redis for future requests                        ║
 * ║  4. On update → evict/refresh the cache                      ║
 * ║                                                               ║
 * ║  All Redis operations are wrapped in try/catch so the app     ║
 * ║  continues working even if Redis is down (fail-open).         ║
 * ║                                                               ║
 * ║  DESIGN PATTERN: Cache-Aside + Fail-Open                     ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCacheService {

    private static final String INVENTORY_KEY_PREFIX = "inventory:product:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Store inventory in Redis.
     * Called after DB save to keep cache fresh.
     */
    public void cacheInventory(Long productId, Inventory inventory) {
        try {
            redisTemplate.opsForValue().set(INVENTORY_KEY_PREFIX + productId, inventory);
            log.debug("Cached inventory for product {}", productId);
        } catch (Exception ex) {
            log.warn("Failed to cache inventory for product {}: {}", productId, ex.getMessage());
        }
    }

    /**
     * Retrieve inventory from Redis.
     * Returns Optional.empty() on cache miss or Redis failure.
     */
    public Optional<Inventory> getInventory(Long productId) {
        try {
            Object value = redisTemplate.opsForValue().get(INVENTORY_KEY_PREFIX + productId);
            if (value instanceof Inventory inventory) {
                log.debug("Cache HIT for inventory product {}", productId);
                return Optional.of(inventory);
            }
        } catch (Exception ex) {
            log.warn("Failed to read inventory cache for product {}: {}", productId, ex.getMessage());
        }
        log.debug("Cache MISS for inventory product {}", productId);
        return Optional.empty();
    }

    /**
     * Remove inventory from Redis.
     * Called when inventory is updated to force a fresh load.
     */
    public void evictInventory(Long productId) {
        try {
            redisTemplate.delete(INVENTORY_KEY_PREFIX + productId);
            log.debug("Evicted inventory cache for product {}", productId);
        } catch (Exception ex) {
            log.warn("Failed to evict inventory cache for product {}: {}", productId, ex.getMessage());
        }
    }
}
