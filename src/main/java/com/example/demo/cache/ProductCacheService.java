package com.example.demo.cache;

import com.example.demo.entity.Product;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductCacheService {
	private static final String PRODUCT_KEY_PREFIX = "product:";
	private static final String PRODUCTS_ALL_KEY = "products:all";
	private static final String PRODUCTS_SEARCH_KEY_PREFIX = "products:search:";

	private final RedisTemplate<String, Object> redisTemplate;
	private final ProductLruCache<String, Object> productLruCache;
	private final Cache<String, Object> caffeineCache;

	public void cacheProduct(Product product) {
		try {
			redisTemplate.opsForValue().set(PRODUCT_KEY_PREFIX + product.getId(), product);
		} catch (Exception ex) {
			// Local embedded Redis can be unavailable during startup or manual shutdown.
		}
	}

	public void evictProduct(Long id) {
		try {
			redisTemplate.delete(PRODUCT_KEY_PREFIX + id);
		} catch (Exception ex) {
			// Fall back to DAO when Redis is not reachable.
		}
	}

	public Optional<Product> getProduct(Long id) {
		try {
			Object value = redisTemplate.opsForValue().get(PRODUCT_KEY_PREFIX + id);
			if (value instanceof Product product) {
				return Optional.of(product);
			}
		} catch (Exception ex) {
			// Fall back to DAO when Redis is not reachable.
		}
		return Optional.empty();
	}

	public void cacheAllProducts(List<Product> products) {
		try {
			productLruCache.put(PRODUCTS_ALL_KEY, products);
		} catch (Exception ex) {
			// LRU cache is in-memory and should not fail, but keep this fail-open.
		}
	}

	@SuppressWarnings("unchecked")
	public Optional<List<Product>> getAllProducts() {
		try {
			Object value = productLruCache.get(PRODUCTS_ALL_KEY);
			if (value instanceof List<?> list) {
				return Optional.of((List<Product>) list);
			}
		} catch (Exception ex) {
			// Fall back to DAO when the LRU cache cannot be read.
		}
		return Optional.empty();
	}

	public void cacheSearchResults(String term, List<Product> products) {
		try {
			caffeineCache.put(PRODUCTS_SEARCH_KEY_PREFIX + term.toLowerCase(), products);
		} catch (Exception ex) {
			// Fall back to DAO when Caffeine is unavailable.
		}
	}

	@SuppressWarnings("unchecked")
	public Optional<List<Product>> getSearchResults(String term) {
		try {
			Object value = caffeineCache.getIfPresent(PRODUCTS_SEARCH_KEY_PREFIX + term.toLowerCase());
			if (value instanceof List<?> list) {
				return Optional.of((List<Product>) list);
			}
		} catch (Exception ex) {
			// Fall back to DAO when Caffeine is unavailable.
		}
		return Optional.empty();
	}

	public void evictCollectionCaches() {
		productLruCache.remove(PRODUCTS_ALL_KEY);
	}

	public void clearSearchCache() {
		caffeineCache.invalidateAll();
	}
}