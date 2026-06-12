package com.example.demo.controller;

import com.example.demo.dto.ProductRequest;
import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	@PostMapping
	public ResponseEntity<Product> createProduct(
			@Valid @RequestBody ProductRequest request
	) {
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(productService.createProduct(request));
	}

	@GetMapping
	public ResponseEntity<List<Product>> getAllProducts() {
		return ResponseEntity.ok(productService.getAllProducts());
	}

	@GetMapping("/search")
	public ResponseEntity<List<Product>> searchProducts(
			@RequestParam String name
	) {
		return ResponseEntity.ok(productService.searchProducts(name));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Product> getProductById(
			@PathVariable Long id
	) {
		return ResponseEntity.ok(productService.getProductById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Product> updateProduct(
			@PathVariable Long id,
			@Valid @RequestBody ProductRequest request
	) {
		return ResponseEntity.ok(productService.updateProduct(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(
			@PathVariable Long id
	) {
		productService.deleteProduct(id);
		return ResponseEntity.noContent().build();
	}
}
