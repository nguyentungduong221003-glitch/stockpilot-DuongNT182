package com.stockpilot.service;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.exception.InsufficientStockException;
import com.stockpilot.exception.ProductNotFoundException;
import com.stockpilot.model.Customer;
import com.stockpilot.model.Order;
import com.stockpilot.model.OrderItem;
import com.stockpilot.model.Product;
import com.stockpilot.repository.CustomerRepository;
import com.stockpilot.repository.OrderRepository;
import com.stockpilot.repository.ProductRepository;
import com.stockpilot.util.DBConnectionHelper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class StockPilotService {
    private final ProductRepository productRepo = new ProductRepository();
    private final CustomerRepository customerRepo = new CustomerRepository();
    private final OrderRepository orderRepo = new OrderRepository();

    public void addProduct(Product product) throws DataAccessException {
        productRepo.save(product);
    }

    public List<Product> getAllProducts() throws DataAccessException {
        return productRepo.findAll();
    }

    public Optional<Product> getProductById(String id) throws DataAccessException {
        return productRepo.findById(id);
    }

    public Optional<Product> getProductBySku(String sku) throws DataAccessException {
        return productRepo.findBySku(sku);
    }

    public void updateProduct(Product product) throws DataAccessException {
        productRepo.update(product);
    }

    public void adjustStock(String sku, int newQty) throws DataAccessException {
        Product p = productRepo.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found for SKU: " + sku));
        p.setStockQuantity(newQty);
        productRepo.update(p);
    }

    public void addCustomer(Customer customer) throws DataAccessException {
        customerRepo.save(customer);
    }

    public List<Customer> getAllCustomers() throws DataAccessException {
        return customerRepo.findAll();
    }

    public Optional<Customer> getCustomerByEmail(String email) throws DataAccessException {
        return customerRepo.findByEmail(email);
    }

    public void placeOrder(Order order) throws DataAccessException {
        try (Connection conn = DBConnectionHelper.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (OrderItem item : order.getOrderItems()) {
                    Product dbProduct = productRepo.findById(item.getProduct().getId())
                            .orElseThrow(() -> new ProductNotFoundException("Product ID not found: " + item.getProduct().getId()));

                    if (dbProduct.getStockQuantity() < item.getQuantity()) {
                        throw new InsufficientStockException(String.format(
                                "Insufficient stock for product '%s' (SKU: %s). Requested: %d, Available: %d",
                                dbProduct.getName(), dbProduct.getSku(), item.getQuantity(), dbProduct.getStockQuantity()
                        ));
                    }

                    dbProduct.setStockQuantity(dbProduct.getStockQuantity() - item.getQuantity());
                    productRepo.update(dbProduct);
                }

                orderRepo.save(order);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new DataAccessException("Order transaction rolled back due to error: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database transactional error occurred", e);
        }
    }

    public BigDecimal calculateTotalRevenue() throws DataAccessException {
        return orderRepo.findAll().stream()
                .map(Order::getFinalTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getOrderCount() throws DataAccessException {
        return orderRepo.findAll().size();
    }

    public List<Map.Entry<Product, Integer>> getTopNProducts(int n) throws DataAccessException {
        List<Order> orders = orderRepo.findAll();
        Map<Product, Integer> productQuantities = orders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        OrderItem::getProduct,
                        Collectors.summingInt(OrderItem::getQuantity)
                ));

        return productQuantities.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public Map<String, BigDecimal> getRevenueByCategory() throws DataAccessException {
        return orderRepo.findAll().stream()
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getCategory(),
                        Collectors.mapping(
                                OrderItem::getTotalPrice,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }

    public List<Product> getLowStockProducts(int threshold) throws DataAccessException {
        return productRepo.findAll().stream()
                .filter(p -> p.getStockQuantity() < threshold)
                .sorted(Comparator.comparingInt(Product::getStockQuantity))
                .collect(Collectors.toList());
    }
}
