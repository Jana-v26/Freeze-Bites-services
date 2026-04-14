package com.freezedance.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String razorpayOrderId;
    private Integer amount;
    private String currency;
    private String orderNumber;
}
