package com.freezedance.api.service;

import com.freezedance.api.dto.response.AnalyticsResponse;
import com.freezedance.api.repository.OrderRepository;
import com.freezedance.api.repository.ProductRepository;
import com.freezedance.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponse getDashboardAnalytics() {
        LocalDateTime epoch = LocalDateTime.of(2000, 1, 1, 0, 0);
        BigDecimal totalRevenue = orderRepository.totalRevenueSince(epoch);
        Long totalOrders = orderRepository.count();
        Long totalCustomers = userRepository.count();
        Long totalProducts = productRepository.count();

        LocalDateTime startOfWeek = LocalDateTime.now()
                .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        BigDecimal revenueThisWeek = orderRepository.totalRevenueSince(startOfWeek);
        long ordersThisWeek = orderRepository.countOrdersSince(startOfWeek);

        return AnalyticsResponse.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalOrders(totalOrders)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .revenueThisWeek(revenueThisWeek != null ? revenueThisWeek : BigDecimal.ZERO)
                .ordersThisWeek(ordersThisWeek)
                .build();
    }
}
