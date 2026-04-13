package com.freezedance.api.controller;

import com.freezedance.api.dto.response.ApiResponse;
import com.freezedance.api.dto.response.ProductResponse;
import com.freezedance.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        Page<ProductResponse> products;

        if (search != null && !search.isBlank()) {
            products = productService.searchProducts(search, pageable);
        } else if (category != null && !category.isBlank()) {
            products = productService.getByCategory(category, pageable);
        } else if (type != null && !type.isBlank()) {
            products = productService.getByType(type, pageable);
        } else {
            products = productService.getAll(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable String slug) {
        ProductResponse product = productService.getBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts() {
        List<ProductResponse> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}
