package com.freezedance.api.service;

import com.freezedance.api.dto.response.PaymentResponse;
import com.freezedance.api.exception.BadRequestException;
import com.freezedance.api.exception.ResourceNotFoundException;
import com.freezedance.api.model.Order;
import com.freezedance.api.model.Payment;
import com.freezedance.api.model.enums.OrderStatus;
import com.freezedance.api.model.enums.PaymentStatus;
import com.freezedance.api.repository.OrderRepository;
import com.freezedance.api.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    public PaymentService(
            @Value("${razorpay.key-id}") String razorpayKeyId,
            @Value("${razorpay.key-secret}") String razorpayKeySecret,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.razorpayKeySecret = razorpayKeySecret;
        try {
            this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to initialize Razorpay client", e);
        }
    }

    @Transactional
    public PaymentResponse createRazorpayOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));

        try {
            int amountInPaise = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", order.getOrderNumber());

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setRazorpayOrderId(razorpayOrder.get("id"));
            payment.setAmount(order.getTotalAmount());
            payment.setCurrency("INR");
            payment.setStatus(PaymentStatus.CREATED);
            paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .razorpayOrderId(razorpayOrder.get("id"))
                    .amount(amountInPaise)
                    .currency("INR")
                    .orderNumber(order.getOrderNumber())
                    .build();
        } catch (RazorpayException e) {
            throw new BadRequestException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    @Transactional
    public void verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

            if (isValid) {
                Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment not found for Razorpay order: " + razorpayOrderId));

                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setRazorpaySignature(razorpaySignature);
                payment.setStatus(PaymentStatus.CAPTURED);
                paymentRepository.save(payment);

                Order order = payment.getOrder();
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
            }
        } catch (RazorpayException e) {
            throw new BadRequestException("Payment verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        try {
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, razorpayKeySecret);

            if (!isValid) {
                throw new BadRequestException("Invalid webhook signature");
            }

            JSONObject webhookPayload = new JSONObject(payload);
            String event = webhookPayload.getString("event");

            JSONObject paymentEntity = webhookPayload
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String razorpayOrderId = paymentEntity.getString("order_id");
            String razorpayPaymentId = paymentEntity.getString("id");

            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElse(null);

            if (payment == null) {
                return;
            }

            switch (event) {
                case "payment.captured":
                    payment.setRazorpayPaymentId(razorpayPaymentId);
                    payment.setStatus(PaymentStatus.CAPTURED);
                    paymentRepository.save(payment);

                    Order order = payment.getOrder();
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                    break;

                case "payment.failed":
                    payment.setRazorpayPaymentId(razorpayPaymentId);
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);

                    Order failedOrder = payment.getOrder();
                    failedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
                    orderRepository.save(failedOrder);
                    break;

                default:
                    break;
            }
        } catch (RazorpayException e) {
            throw new BadRequestException("Webhook processing failed: " + e.getMessage());
        }
    }
}
