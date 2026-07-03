package com.stockpilot.service;

import com.stockpilot.model.Order;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageDiscount implements DiscountPolicy {
    private final BigDecimal percentage;

    public PercentageDiscount(BigDecimal percentage) {
        this.percentage = percentage;
    }

    @Override
    public BigDecimal calculateDiscount(Order order) {
        if (order == null || order.getOriginalTotal() == null) {
            return BigDecimal.ZERO;
        }
        return order.getOriginalTotal()
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String getPolicyName() {
        return "PERCENTAGE_DISCOUNT(" + percentage + "%)";
    }
}
