package com.freezedance.api.service;

import com.freezedance.api.dto.request.ProductRequest;
import com.freezedance.api.dto.response.ProductResponse;
import com.freezedance.api.exception.ResourceNotFoundException;
import com.freezedance.api.model.Category;
import com.freezedance.api.model.Product;
import com.freezedance.api.model.enums.ProductType;
import com.freezedance.api.repository.CategoryRepository;
import com.freezedance.api.repository.ProductRepository;
import com.freezedance.api.repository.ReviewRepository;
import com.freezedance.api.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable).map(this::mapToResponse);
    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return getAll(pageable);
    }

    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return mapToResponse(product);
    }

    public ProductResponse getProductBySlug(String slug) {
        return getBySlug(slug);
    }

    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<ProductResponse> getByCategory(String categorySlug, Pageable pageable) {
        return productRepository.findByCategorySlugAndIsActiveTrue(categorySlug, pageable)
                .map(this::mapToResponse);
    }

    public Page<ProductResponse> getProductsByCategory(String categorySlug, Pageable pageable) {
        return getByCategory(categorySlug, pageable);
    }

    public Page<ProductResponse> getByType(String type, Pageable pageable) {
        ProductType productType;
        try {
            productType = ProductType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Unknown product type: " + type);
        }
        return productRepository.findByProductTypeAndIsActiveTrue(productType, pageable)
                .map(this::mapToResponse);
    }

    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        return productRepository.search(query, pageable).map(this::mapToResponse);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(SlugGenerator.toSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setShortDesc(request.getShortDesc());
        product.setBasePrice(request.getBasePrice());
        product.setDiscountPct(request.getDiscountPct() != null ? request.getDiscountPct() : BigDecimal.ZERO);
        product.setProductType(request.getProductType());
        product.setCategory(category);
        product.setIsFeatured(request.getIsFeatured() != null && request.getIsFeatured());
        product.setIsActive(true);
        product.setMetaTitle(request.getMetaTitle());
        product.setMetaDesc(request.getMetaDesc());

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setSlug(SlugGenerator.toSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setShortDesc(request.getShortDesc());
        product.setBasePrice(request.getBasePrice());
        if (request.getDiscountPct() != null) product.setDiscountPct(request.getDiscountPct());
        product.setProductType(request.getProductType());
        product.setCategory(category);
        if (request.getIsFeatured() != null) product.setIsFeatured(request.getIsFeatured());
        product.setMetaTitle(request.getMetaTitle());
        product.setMetaDesc(request.getMetaDesc());

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        Double avgRating = reviewRepository.averageRatingByProductId(product.getId());
        long reviewCount = reviewRepository.countByProductIdAndIsApprovedTrue(product.getId());

        List<ProductResponse.VariantResponse> variantResponses = null;
        if (product.getVariants() != null) {
            variantResponses = product.getVariants().stream().map(v ->
                    ProductResponse.VariantResponse.builder()
                            .id(v.getId())
                            .weightGrams(v.getWeightGrams())
                            .price(v.getPrice())
                            .stockQty(v.getStockQty())
                            .sku(v.getSku())
                            .build()
            ).collect(Collectors.toList());
        }

        List<ProductResponse.ImageResponse> imageResponses = null;
        if (product.getImages() != null) {
            imageResponses = product.getImages().stream().map(img ->
                    ProductResponse.ImageResponse.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .altText(img.getAltText())
                            .isPrimary(img.getIsPrimary())
                            .build()
            ).collect(Collectors.toList());
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .shortDesc(product.getShortDesc())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .productType(product.getProductType())
                .basePrice(product.getBasePrice())
                .discountPct(product.getDiscountPct())
                .isFeatured(product.getIsFeatured())
                .variants(variantResponses)
                .images(imageResponses)
                .avgRating(avgRating != null ? avgRating : 0.0)
                .reviewCount(reviewCount)
                .build();
    }
}
