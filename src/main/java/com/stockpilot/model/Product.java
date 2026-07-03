package com.stockpilot.model;

import com.stockpilot.exception.InvalidInputException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

public class Product {
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z]{3}-\\d{4}$");

    private String id;
    private String sku;
    private String name;
    private String category;
    private BigDecimal price;
    private int stockQuantity;

    public Product() {}

    public Product(String id, String sku, String name, String category, BigDecimal price, int stockQuantity) {
        this.id = id;
        setSku(sku);
        this.name = name;
        this.category = category;
        setPrice(price);
        setStockQuantity(stockQuantity);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        if (sku == null || !SKU_PATTERN.matcher(sku).matches()) {
            throw new InvalidInputException("Invalid SKU format. Expected format: AAA-1234 (e.g., LAP-4521)");
        }
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("Price cannot be negative");
        }
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new InvalidInputException("Stock quantity cannot be negative");
        }
        this.stockQuantity = stockQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) || Objects.equals(sku, product.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sku);
    }

    @Override
    public String toString() {
        return String.format("Product[ID='%s', SKU='%s', Name='%s', Category='%s', Price=$%.2f, Stock=%d]",
                id, sku, name, category, price, stockQuantity);
    }
}
