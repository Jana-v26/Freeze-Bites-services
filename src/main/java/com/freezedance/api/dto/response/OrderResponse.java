package com.freezedance.api.dto.response;

import com.freezedance.api.model.enums.OrderStatus;
import com.freezedance.api.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String trackingNumber;
    private String trackingUrl;
    private String notes;
    private AddressInfo address;
    private PaymentInfo payment;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;

    @Data @AllArgsConstructor @Builder
    public static class AddressInfo {
        private String fullName;
        private String phone;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
    }

    @Data @AllArgsConstructor @Builder
    public static class PaymentInfo {
        private String razorpayPaymentId;
        private PaymentStatus status;
        private String method;
    }

    @Data @AllArgsConstructor @Builder
    public static class OrderItemResponse {
        private String productName;
        private Integer weightGrams;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
