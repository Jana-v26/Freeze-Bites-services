package com.freezedance.api.service;

import com.freezedance.api.dto.request.CartItemRequest;
import com.freezedance.api.dto.response.CartResponse;
import com.freezedance.api.exception.BadRequestException;
import com.freezedance.api.exception.ResourceNotFoundException;
import com.freezedance.api.model.Cart;
import com.freezedance.api.model.CartItem;
import com.freezedance.api.model.Product;
import com.freezedance.api.model.ProductVariant;
import com.freezedance.api.repository.CartItemRepository;
import com.freezedance.api.repository.CartRepository;
import com.freezedance.api.repository.ProductRepository;
import com.freezedance.api.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });
        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + request.getVariantId()));

            if (variant.getStockQuantity() < request.getQuantity()) {
                throw new BadRequestException("Insufficient stock for the selected variant");
            }
        }

        // Check if item already exists in cart
        ProductVariant finalVariant = variant;
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId())
                        && (finalVariant == null
                            ? item.getVariant() == null
                            : finalVariant.getId().equals(item.getVariant() != null ? item.getVariant().getId() : null)))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setVariant(variant);
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(variant != null ? variant.getPrice() : product.getPrice());
            cart.getItems().add(newItem);
        }

        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Transactional
    public CartResponse updateItemQuantity(Long userId, Long itemId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }

        if (item.getVariant() != null && item.getVariant().getStockQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock for the selected variant");
        }

        item.setQuantity(quantity);
        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartResponse.CartItemResponse> itemResponses = cart.getItems().stream().map(item ->
                CartResponse.CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productSlug(item.getProduct().getSlug())
                        .productImage(item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()
                                ? item.getProduct().getImages().get(0).getUrl() : null)
                        .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                        .variantSize(item.getVariant() != null ? item.getVariant().getSize() : null)
                        .variantColor(item.getVariant() != null ? item.getVariant().getColor() : null)
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build()
        ).collect(Collectors.toList());

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartResponse.CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .totalItems(cart.getItems().size())
                .build();
    }
}
