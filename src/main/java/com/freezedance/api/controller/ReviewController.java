package com.freezedance.api.controller;

import com.freezedance.api.dto.request.ReviewRequest;
import com.freezedance.api.dto.response.ApiResponse;
import com.freezedance.api.dto.response.ReviewResponse;
import com.freezedance.api.model.User;
import com.freezedance.api.repository.UserRepository;
import com.freezedance.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{slug}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @PathVariable String slug,
            Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getProductReviews(slug, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @PathVariable String slug,
            @RequestBody @Valid ReviewRequest request) {
        Long userId = getCurrentUserId();
        ReviewResponse review = reviewService.createReview(userId, slug, request);
        return ResponseEntity.ok(ApiResponse.success("Review submitted for approval", review));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
