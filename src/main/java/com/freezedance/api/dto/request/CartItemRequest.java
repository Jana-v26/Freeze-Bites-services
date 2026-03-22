package com.freezedance.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull
    private Long productId;

    @NotNull
    private Long variantId;

    @NotNull @Min(1)
    private Integer quantity;
}
