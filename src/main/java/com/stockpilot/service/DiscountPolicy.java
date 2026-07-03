package com.stockpilot.service;

import com.stockpilot.model.Order;
import java.math.BigDecimal;

public interface DiscountPolicy {

    BigDecimal calculateDiscount(Order order);

    String getPolicyName();
}
