package com.freezedance.api.dto.request;

import com.freezedance.api.model.enums.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank
    private String name;

    private String description;
    private String shortDesc;

    @NotNull
    private Long categoryId;

    @NotNull
    private ProductType productType;

    @NotNull
    private BigDecimal basePrice;

    private BigDecimal discountPct;
    private Boolean isFeatured;
    private String metaTitle;
    private String metaDesc;
}
