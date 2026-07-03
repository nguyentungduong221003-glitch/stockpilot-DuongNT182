package com.stockpilot.model;

import com.stockpilot.service.DiscountPolicy;
import com.stockpilot.service.NoDiscount;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    private String id;
    private Customer customer;
    private LocalDateTime orderDate;
    private List<OrderItem> orderItems = new ArrayList<>();
    private DiscountPolicy discountPolicy = new NoDiscount();

    public Order() {
        this.orderDate = LocalDateTime.now();
    }

    public Order(String id, Customer customer, LocalDateTime orderDate) {
        this.id = id;
        this.customer = customer;
        this.orderDate = orderDate != null ? orderDate : LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
    }

    public DiscountPolicy getDiscountPolicy() {
        return discountPolicy;
    }

    public void setDiscountPolicy(DiscountPolicy discountPolicy) {
        if (discountPolicy != null) {
            this.discountPolicy = discountPolicy;
        }
    }

    public BigDecimal getOriginalTotal() {
        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDiscountAmount() {
        return discountPolicy.calculateDiscount(this);
    }

    public BigDecimal getFinalTotal() {
        BigDecimal total = getOriginalTotal().subtract(getDiscountAmount());
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Order[ID='%s', Customer='%s', Date=%s, Items=%d, OriginalTotal=$%.2f, Discount=$%.2f, FinalTotal=$%.2f, Policy=%s]",
                id, customer != null ? customer.getName() : "null", orderDate, orderItems.size(),
                getOriginalTotal(), getDiscountAmount(), getFinalTotal(), discountPolicy.getPolicyName());
    }
}
