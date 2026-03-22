package com.freezedance.api.controller;

import com.freezedance.api.dto.response.AnalyticsResponse;
import com.freezedance.api.dto.response.ApiResponse;
import com.freezedance.api.dto.response.OrderResponse;
import com.freezedance.api.dto.response.ReviewResponse;
import com.freezedance.api.model.OrderStatus;
import com.freezedance.api.model.User;
import com.freezedance.api.repository.UserRepository;
import com.freezedance.api.service.AnalyticsService;
import com.freezedance.api.service.OrderService;
import com.freezedance.api.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AnalyticsService analyticsService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping("/analytics/dashboard")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getDashboard() {
        AnalyticsResponse analytics = analyticsService.getDashboardAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        Page<OrderResponse> orders;
        if (status != null) {
            orders = orderService.getOrdersByStatus(status, pageable);
        } else {
            orders = orderService.getAllOrders(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam(required = false) String trackingUrl) {
        OrderResponse order = orderService.updateOrderStatus(orderId, status, trackingNumber, trackingUrl);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<Page<User>>> getCustomers(Pageable pageable) {
        Page<User> customers = userRepository.findByRole("CUSTOMER", pageable);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @PutMapping("/reviews/{reviewId}/approve")
    public ResponseEntity<ApiResponse<ReviewResponse>> approveReview(@PathVariable Long reviewId) {
        ReviewResponse review = reviewService.approveReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review approved", review));
    }
}
