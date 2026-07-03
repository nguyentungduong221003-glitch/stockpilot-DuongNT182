package com.stockpilot.repository;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.model.Customer;
import com.stockpilot.model.Order;
import com.stockpilot.model.OrderItem;
import com.stockpilot.model.Product;
import com.stockpilot.service.DiscountPolicy;
import com.stockpilot.service.NoDiscount;
import com.stockpilot.service.PercentageDiscount;
import com.stockpilot.service.BulkDiscount;
import com.stockpilot.util.DBConnectionHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public class OrderRepository implements Repository<Order, String> {
    private final CustomerRepository customerRepo = new CustomerRepository();
    private final ProductRepository productRepo = new ProductRepository();

    @Override
    public void save(Order order) throws DataAccessException {
        String insertOrderSql = "INSERT INTO orders (id, customer_id, order_date, discount_policy_type, total_original, total_discount, total_final) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO order_items (id, order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionHelper.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction
            try {
                try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSql)) {
                    orderStmt.setString(1, order.getId());
                    orderStmt.setString(2, order.getCustomer().getId());
                    orderStmt.setTimestamp(3, Timestamp.valueOf(order.getOrderDate()));
                    orderStmt.setString(4, order.getDiscountPolicy().getPolicyName());
                    orderStmt.setBigDecimal(5, order.getOriginalTotal());
                    orderStmt.setBigDecimal(6, order.getDiscountAmount());
                    orderStmt.setBigDecimal(7, order.getFinalTotal());
                    orderStmt.executeUpdate();
                }

                try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                    for (OrderItem item : order.getOrderItems()) {
                        itemStmt.setString(1, item.getId());
                        itemStmt.setString(2, order.getId());
                        itemStmt.setString(3, item.getProduct().getId());
                        itemStmt.setInt(4, item.getQuantity());
                        itemStmt.setBigDecimal(5, item.getUnitPrice());
                        itemStmt.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Transaction failed while saving order: Rollback performed.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database access error during order save", e);
        }
    }

    @Override
    public Optional<Order> findById(String id) throws DataAccessException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrder(rs, conn));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding order ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findAll() throws DataAccessException {
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DBConnectionHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs, conn));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing all orders", e);
        }
        return orders;
    }

    @Override
    public void update(Order order) throws DataAccessException {
        String sql = "UPDATE orders SET total_original = ?, total_discount = ?, total_final = ? WHERE id = ?";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, order.getOriginalTotal());
            pstmt.setBigDecimal(2, order.getDiscountAmount());
            pstmt.setBigDecimal(3, order.getFinalTotal());
            pstmt.setString(4, order.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating order ID: " + order.getId(), e);
        }
    }

    @Override
    public void deleteById(String id) throws DataAccessException {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting order ID: " + id, e);
        }
    }

    private Order mapResultSetToOrder(ResultSet rs, Connection conn) throws SQLException, DataAccessException {
        Order o = new Order();
        o.setId(rs.getString("id"));
        o.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());

        String customerId = rs.getString("customer_id");
        Optional<Customer> customer = customerRepo.findById(customerId);
        customer.ifPresent(o::setCustomer);

        String policyName = rs.getString("discount_policy_type");
        o.setDiscountPolicy(reconstructPolicy(policyName));

        String itemsSql = "SELECT * FROM order_items WHERE order_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(itemsSql)) {
            pstmt.setString(1, o.getId());
            try (ResultSet itemsRs = pstmt.executeQuery()) {
                while (itemsRs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(itemsRs.getString("id"));
                    item.setQuantity(itemsRs.getInt("quantity"));
                    item.setUnitPrice(itemsRs.getBigDecimal("unit_price"));

                    String productId = itemsRs.getString("product_id");
                    Optional<Product> p = productRepo.findById(productId);
                    p.ifPresent(item::setProduct);

                    o.addOrderItem(item);
                }
            }
        }

        return o;
    }

    private DiscountPolicy reconstructPolicy(String name) {
        if (name == null || name.startsWith("NO_DISCOUNT")) {
            return new NoDiscount();
        } else if (name.contains("PERCENTAGE_DISCOUNT")) {
            try {
                String valStr = name.substring(name.indexOf('(') + 1, name.indexOf('%'));
                return new PercentageDiscount(new BigDecimal(valStr));
            } catch (Exception e) {
                return new PercentageDiscount(BigDecimal.TEN);
            }
        } else if (name.contains("BULK_DISCOUNT")) {
            return new BulkDiscount(5, BigDecimal.valueOf(15));
        }
        return new NoDiscount();
    }
}
