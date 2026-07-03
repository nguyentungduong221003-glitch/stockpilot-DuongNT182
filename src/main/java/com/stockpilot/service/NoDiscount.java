package com.stockpilot.service;

import com.stockpilot.model.Order;
import java.math.BigDecimal;


public class NoDiscount implements DiscountPolicy {
    @Override
    public BigDecimal calculateDiscount(Order order) {
        return BigDecimal.ZERO;
    }

    @Override
    public String getPolicyName() {
        return "NO_DISCOUNT";
    }
}
