package com.freezedance.api.service;

import com.freezedance.api.dto.request.ReviewRequest;
import com.freezedance.api.dto.response.ReviewResponse;
import com.freezedance.api.exception.BadRequestException;
import com.freezedance.api.exception.ResourceNotFoundException;
import com.freezedance.api.model.Product;
import com.freezedance.api.model.Review;
import com.freezedance.api.model.User;
import com.freezedance.api.repository.ProductRepository;
import com.freezedance.api.repository.ReviewRepository;
import com.freezedance.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(String slug, Pageable pageable) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));

        return reviewRepository.findByProductIdAndIsApprovedTrue(product.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public ReviewResponse createReview(Long userId, String slug, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));

        boolean alreadyReviewed = reviewRepository.existsByProductIdAndUserId(product.getId(), userId);
        if (alreadyReviewed) {
            throw new BadRequestException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setIsApproved(false);

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    @Transactional
    public ReviewResponse approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        review.setIsApproved(true);
        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .approved(review.getIsApproved())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
