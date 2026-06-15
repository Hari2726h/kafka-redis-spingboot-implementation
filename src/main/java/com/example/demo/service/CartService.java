package com.example.demo.service;

import com.example.demo.dto.CartItemRequest;
import com.example.demo.dto.CartResponse;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.enums.ActionType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.kafka.HistoryEvent;
import com.example.demo.kafka.HistoryProducer;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  CartService                                                  ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  WHY IT EXISTS:                                               ║
 * ║  Manages shopping cart operations. The cart is persisted in    ║
 * ║  the database (not in session), so it survives logouts and    ║
 * ║  device switches.                                             ║
 * ║                                                               ║
 * ║  HOW IT WORKS:                                                ║
 * ║  - Cart is auto-created when a user first adds an item        ║
 * ║  - Adding an existing product updates quantity (no duplicates)║
 * ║  - Removing the last item doesn't delete the cart             ║
 * ║  - "Clear cart" removes all items but keeps the cart          ║
 * ║                                                               ║
 * ║  DESIGN PATTERNS:                                             ║
 * ║  - Service Layer Pattern                                      ║
 * ║  - Lazy Initialization (cart created on first use)            ║
 * ║  - Observer (audit via Kafka)                                 ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final HistoryProducer historyProducer;

    // ─── VIEW CART ───────────────────────────────────────────────

    /**
     * Returns the cart for a user. If no cart exists, returns
     * an empty cart response (not an error).
     */
    public CartResponse getCart(Long userId) {
        User user = findUserOrThrow(userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Create an empty cart if none exists
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });

        return CartResponse.fromEntity(cart);
    }

    // ─── ADD ITEM ────────────────────────────────────────────────

    /**
     * Adds a product to the user's cart. If the product is already
     * in the cart, the quantities are ADDED (not replaced).
     *
     * Example: Cart has 2x iPhone. User adds 3x iPhone → Cart has 5x iPhone.
     */
    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        User user = findUserOrThrow(userId);
        Product product = findProductOrThrow(request.getProductId());

        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            // Update quantity (add to existing)
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
            log.info("Updated cart item: product={}, new quantity={}", product.getId(), item.getQuantity());
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
            cartRepository.save(cart);
            log.info("Added product {} to cart for user {}", product.getId(), userId);
        }

        publishHistory(cart, ActionType.UPDATE);

        // Reload to get fresh data
        Cart updatedCart = cartRepository.findByUserId(userId).orElseThrow();
        return CartResponse.fromEntity(updatedCart);
    }

    // ─── UPDATE ITEM QUANTITY ────────────────────────────────────

    /**
     * Sets the quantity of a specific product in the cart.
     * If quantity is set to 0, the item is removed.
     */
    @Transactional
    public CartResponse updateItemQuantity(Long userId, Long productId, int newQuantity) {
        Cart cart = findCartOrThrow(userId);

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", productId));

        if (newQuantity <= 0) {
            // Remove item if quantity is 0 or negative
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            log.info("Removed product {} from cart for user {}", productId, userId);
        } else {
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
            log.info("Updated product {} quantity to {} for user {}", productId, newQuantity, userId);
        }

        publishHistory(cart, ActionType.UPDATE);

        Cart updatedCart = cartRepository.findByUserId(userId).orElseThrow();
        return CartResponse.fromEntity(updatedCart);
    }

    // ─── REMOVE ITEM ─────────────────────────────────────────────

    /**
     * Removes a specific product from the cart entirely.
     */
    @Transactional
    public CartResponse removeItem(Long userId, Long productId) {
        Cart cart = findCartOrThrow(userId);

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", productId));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        publishHistory(cart, ActionType.UPDATE);
        log.info("Removed product {} from cart for user {}", productId, userId);

        Cart updatedCart = cartRepository.findByUserId(userId).orElseThrow();
        return CartResponse.fromEntity(updatedCart);
    }

    // ─── CLEAR CART ──────────────────────────────────────────────

    /**
     * Removes all items from the cart but keeps the cart itself.
     */
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = findCartOrThrow(userId);
        cart.getItems().clear();
        cartRepository.save(cart);

        publishHistory(cart, ActionType.UPDATE);
        log.info("Cleared cart for user {}", userId);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    private Cart findCartOrThrow(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
    }

    private void publishHistory(Cart cart, ActionType actionType) {
        historyProducer.sendEvent(new HistoryEvent(
                Cart.class.getSimpleName(),
                cart.getId(),
                actionType.name()
        ));
    }
}
