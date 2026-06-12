package com.example.demo.service;

import com.example.demo.cache.ProductCacheService;
import com.example.demo.dto.ProductRequest;
import com.example.demo.entity.Product;
import com.example.demo.enums.ActionType;
import com.example.demo.enums.ProductStatus;
import com.example.demo.kafka.HistoryEvent;
import com.example.demo.kafka.HistoryProducer;
import com.example.demo.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductCacheService productCacheService;

	private final HistoryProducer historyProducer;

	@Transactional
	public Product createProduct(
			ProductRequest request
	) {
		Product product = Product.builder()
				.name(request.getName())
				.price(request.getPrice())
				.status(ProductStatus.ACTIVE)
				.build();

		Product savedProduct = productRepository.save(product);
		productCacheService.cacheProduct(savedProduct);
		productCacheService.evictCollectionCaches();
		productCacheService.clearSearchCache();
		publishHistory(savedProduct, ActionType.CREATE);
		log.info("Created product {}", savedProduct.getId());
		return savedProduct;
	}

	public List<Product> getAllProducts() {
		return productCacheService.getAllProducts()
				.orElseGet(() -> {
					List<Product> products = productRepository.findAll();
					productCacheService.cacheAllProducts(products);
					return products;
				});
	}

	public Product getProductById(Long id) {
		return productCacheService.getProduct(id)
				.orElseGet(() -> {
					Product product = findProductOrThrow(id);
					productCacheService.cacheProduct(product);
					return product;
				});
	}

	public List<Product> searchProducts(String name) {
		return productCacheService.getSearchResults(name)
				.orElseGet(() -> {
					List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
					productCacheService.cacheSearchResults(name, products);
					return products;
				});
	}

	@Transactional
	public Product updateProduct(
			Long id,
			ProductRequest request
	) {
		Product product = findProductOrThrow(id);
		product.setName(request.getName());
		product.setPrice(request.getPrice());

		Product savedProduct = productRepository.save(product);
		productCacheService.cacheProduct(savedProduct);
		productCacheService.evictCollectionCaches();
		productCacheService.clearSearchCache();
		publishHistory(savedProduct, ActionType.UPDATE);
		log.info("Updated product {}", savedProduct.getId());
		return savedProduct;
	}

	@Transactional
	public void deleteProduct(Long id) {
		Product product = findProductOrThrow(id);
		productRepository.delete(product);
		productCacheService.evictProduct(id);
		productCacheService.evictCollectionCaches();
		productCacheService.clearSearchCache();
		publishHistory(product, ActionType.DELETE);
		log.info("Deleted product {}", id);
	}

	private Product findProductOrThrow(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Product not found with id " + id
				));
	}

	private void publishHistory(Product product, ActionType actionType) {
		historyProducer.sendEvent(new HistoryEvent(
				Product.class.getSimpleName(),
				product.getId(),
				actionType.name()
		));
	}
}
