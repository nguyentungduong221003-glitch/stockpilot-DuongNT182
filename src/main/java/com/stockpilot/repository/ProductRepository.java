package com.stockpilot.repository;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.model.Product;
import com.stockpilot.util.DBConnectionHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository implements Repository<Product, String> {

    @Override
    public void save(Product product) throws DataAccessException {
        String sql = "INSERT INTO products (id, sku, name, category, price, stock_quantity) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getId());
            pstmt.setString(2, product.getSku());
            pstmt.setString(3, product.getName());
            pstmt.setString(4, product.getCategory());
            pstmt.setBigDecimal(5, product.getPrice());
            pstmt.setInt(6, product.getStockQuantity());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving product: " + product.getSku(), e);
        }
    }

    @Override
    public Optional<Product> findById(String id) throws DataAccessException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding product by ID: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Product> findBySku(String sku) throws DataAccessException {
        String sql = "SELECT * FROM products WHERE sku = ?";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sku);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding product by SKU: " + sku, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() throws DataAccessException {
        String sql = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DBConnectionHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing all products", e);
        }
        return products;
    }

    @Override
    public void update(Product product) throws DataAccessException {
        String sql = "UPDATE products SET sku = ?, name = ?, category = ?, price = ?, stock_quantity = ? WHERE id = ?";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getSku());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getCategory());
            pstmt.setBigDecimal(4, product.getPrice());
            pstmt.setInt(5, product.getStockQuantity());
            pstmt.setString(6, product.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating product: " + product.getSku(), e);
        }
    }

    @Override
    public void deleteById(String id) throws DataAccessException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting product ID: " + id, e);
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getString("id"));
        p.setSku(rs.getString("sku"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        return p;
    }
}
