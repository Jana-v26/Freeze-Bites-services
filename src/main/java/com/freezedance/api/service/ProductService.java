package com.freezedance.api.service;

import com.freezedance.api.dto.request.ProductRequest;
import com.freezedance.api.dto.response.ProductResponse;
import com.freezedance.api.exception.ResourceNotFoundException;
import com.freezedance.api.model.Category;
import com.freezedance.api.model.Product;
import com.freezedance.api.model.ProductImage;
import com.freezedance.api.model.ProductVariant;
import com.freezedance.api.repository.CategoryRepository;
import com.freezedance.api.repository.ProductRepository;
import com.freezedance.api.repository.ReviewRepository;
import com.freezedance.api.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return mapToResponse(product);
    }

    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findByFeaturedTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<ProductResponse> getProductsByCategory(String categorySlug, Pageable pageable) {
        Category category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + categorySlug));
        return productRepository.findByCategoryId(category.getId(), pageable).map(this::mapToResponse);
    }

    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(SlugGenerator.toSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCompareAtPrice(request.getCompareAtPrice());
        product.setCategory(category);
        product.setFeatured(request.isFeatured());
        product.setActive(true);

        if (request.getVariants() != null) {
            List<ProductVariant> variants = request.getVariants().stream().map(v -> {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setSize(v.getSize());
                variant.setColor(v.getColor());
                variant.setSku(v.getSku());
                variant.setPrice(v.getPrice());
                variant.setStockQuantity(v.getStockQuantity());
                return variant;
            }).collect(Collectors.toList());
            product.setVariants(variants);
        }

        if (request.getImages() != null) {
            List<ProductImage> images = request.getImages().stream().map(img -> {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setUrl(img.getUrl());
                image.setAltText(img.getAltText());
                image.setSortOrder(img.getSortOrder());
                return image;
            }).collect(Collectors.toList());
            product.setImages(images);
        }

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
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
        product.setPrice(request.getPrice());
        product.setCompareAtPrice(request.getCompareAtPrice());
        product.setCategory(category);
        product.setFeatured(request.isFeatured());

        if (request.getVariants() != null) {
            product.getVariants().clear();
            request.getVariants().forEach(v -> {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setSize(v.getSize());
                variant.setColor(v.getColor());
                variant.setSku(v.getSku());
                variant.setPrice(v.getPrice());
                variant.setStockQuantity(v.getStockQuantity());
                product.getVariants().add(variant);
            });
        }

        if (request.getImages() != null) {
            product.getImages().clear();
            request.getImages().forEach(img -> {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setUrl(img.getUrl());
                image.setAltText(img.getAltText());
                image.setSortOrder(img.getSortOrder());
                product.getImages().add(image);
            });
        }

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId());
        Long reviewCount = reviewRepository.countByProductIdAndApprovedTrue(product.getId());

        List<ProductResponse.VariantResponse> variantResponses = null;
        if (product.getVariants() != null) {
            variantResponses = product.getVariants().stream().map(v ->
                    ProductResponse.VariantResponse.builder()
                            .id(v.getId())
                            .size(v.getSize())
                            .color(v.getColor())
                            .sku(v.getSku())
                            .price(v.getPrice())
                            .stockQuantity(v.getStockQuantity())
                            .build()
            ).collect(Collectors.toList());
        }

        List<ProductResponse.ImageResponse> imageResponses = null;
        if (product.getImages() != null) {
            imageResponses = product.getImages().stream().map(img ->
                    ProductResponse.ImageResponse.builder()
                            .id(img.getId())
                            .url(img.getUrl())
                            .altText(img.getAltText())
                            .sortOrder(img.getSortOrder())
                            .build()
            ).collect(Collectors.toList());
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .featured(product.isFeatured())
                .active(product.isActive())
                .variants(variantResponses)
                .images(imageResponses)
                .avgRating(avgRating != null ? avgRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
