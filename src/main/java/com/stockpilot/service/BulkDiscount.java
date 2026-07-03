package com.stockpilot.service;

import com.stockpilot.model.Order;
import com.stockpilot.model.OrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BulkDiscount implements DiscountPolicy {
    private final int thresholdQuantity;
    private final BigDecimal discountPercentage;

    public BulkDiscount(int thresholdQuantity, BigDecimal discountPercentage) {
        this.thresholdQuantity = thresholdQuantity;
        this.discountPercentage = discountPercentage;
    }

    @Override
    public BigDecimal calculateDiscount(Order order) {
        if (order == null || order.getOrderItems() == null) {
            return BigDecimal.ZERO;
        }

        int totalQuantity = order.getOrderItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        if (totalQuantity >= thresholdQuantity) {
            return order.getOriginalTotal()
                    .multiply(discountPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    @Override
    public String getPolicyName() {
        return String.format("BULK_DISCOUNT(>=%d items, %s%% off)", thresholdQuantity, discountPercentage);
    }
}
