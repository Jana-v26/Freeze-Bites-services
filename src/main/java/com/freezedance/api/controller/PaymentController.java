package com.freezedance.api.controller;

import com.freezedance.api.dto.response.ApiResponse;
import com.freezedance.api.dto.response.PaymentResponse;
import com.freezedance.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentResponse>> createOrder(@RequestParam String orderNumber) {
        PaymentResponse response = paymentService.createRazorpayOrder(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyPayment(
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpaySignature) {
        paymentService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", null));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok(ApiResponse.success("Webhook processed", null));
    }
}
