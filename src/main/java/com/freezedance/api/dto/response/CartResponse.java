package com.freezedance.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data @AllArgsConstructor @Builder
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private Integer totalItems;

    @Data @AllArgsConstructor @Builder
    public static class CartItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productSlug;
        private String imageUrl;
        private Long variantId;
        private Integer weightGrams;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}
