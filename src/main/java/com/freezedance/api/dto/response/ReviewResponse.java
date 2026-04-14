package com.freezedance.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean approved;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private LocalDateTime createdAt;
}
