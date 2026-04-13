package com.freezedance.api.dto.response;

import com.freezedance.api.model.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data @AllArgsConstructor @Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String shortDesc;
    private String categoryName;
    private String categorySlug;
    private ProductType productType;
    private BigDecimal basePrice;
    private BigDecimal discountPct;
    private Boolean isFeatured;
    private Double avgRating;
    private Long reviewCount;
    private List<VariantResponse> variants;
    private List<ImageResponse> images;

    @Data @AllArgsConstructor @Builder
    public static class VariantResponse {
        private Long id;
        private Integer weightGrams;
        private BigDecimal price;
        private Integer stockQty;
        private String sku;
    }

    @Data @AllArgsConstructor @Builder
    public static class ImageResponse {
        private Long id;
        private String imageUrl;
        private String altText;
        private Boolean isPrimary;
    }
}
