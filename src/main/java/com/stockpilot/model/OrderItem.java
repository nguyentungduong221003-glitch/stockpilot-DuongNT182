package com.stockpilot.model;

import com.stockpilot.exception.InvalidInputException;
import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {
    private String id;
    private Product product;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderItem() {}

    public OrderItem(String id, Product product, int quantity, BigDecimal unitPrice) {
        this.id = id;
        this.product = product;
        setQuantity(quantity);
        setUnitPrice(unitPrice);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than zero");
        }
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("Unit price cannot be negative");
        }
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(id, orderItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("OrderItem[ID='%s', Product='%s', Qty=%d, UnitPrice=$%.2f, Total=$%.2f]",
                id, product != null ? product.getName() : "null", quantity, unitPrice, getTotalPrice());
    }
}
