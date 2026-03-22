package com.freezedance.api.controller;

import com.freezedance.api.dto.request.OrderRequest;
import com.freezedance.api.dto.response.ApiResponse;
import com.freezedance.api.dto.response.OrderResponse;
import com.freezedance.api.model.User;
import com.freezedance.api.repository.UserRepository;
import com.freezedance.api.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@RequestBody @Valid OrderRequest request) {
        Long userId = getCurrentUserId();
        OrderResponse order = orderService.placeOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<OrderResponse> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String orderNumber) {
        Long userId = getCurrentUserId();
        OrderResponse order = orderService.getOrder(userId, orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable String orderNumber) {
        Long userId = getCurrentUserId();
        OrderResponse order = orderService.cancelOrder(userId, orderNumber);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
