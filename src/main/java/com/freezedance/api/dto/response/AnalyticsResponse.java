package com.freezedance.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data @AllArgsConstructor @Builder
public class AnalyticsResponse {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalCustomers;
    private Long totalProducts;
    private BigDecimal revenueThisWeek;
    private Long ordersThisWeek;
    private List<TopProduct> topProducts;

    @Data @AllArgsConstructor @Builder
    public static class TopProduct {
        private String name;
        private Long totalSold;
        private BigDecimal revenue;
    }
}
