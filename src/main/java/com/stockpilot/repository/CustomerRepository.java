package com.stockpilot.repository;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.model.Customer;
import com.stockpilot.util.DBConnectionHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class CustomerRepository implements Repository<Customer, String> {

    @Override
    public void save(Customer customer) throws DataAccessException {
        String sql = "INSERT INTO customers (id, name, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getId());
            pstmt.setString(2, customer.getName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getPhone());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving customer: " + customer.getEmail(), e);
        }
    }

    @Override
    public Optional<Customer> findById(String id) throws DataAccessException {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding customer by ID: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Customer> findByEmail(String email) throws DataAccessException {
        String sql = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding customer by Email: " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() throws DataAccessException {
        String sql = "SELECT * FROM customers";
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DBConnectionHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing all customers", e);
        }
        return customers;
    }

    @Override
    public void update(Customer customer) throws DataAccessException {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhone());
            pstmt.setString(4, customer.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating customer: " + customer.getEmail(), e);
        }
    }

    @Override
    public void deleteById(String id) throws DataAccessException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DBConnectionHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting customer ID: " + id, e);
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getString("id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        return c;
    }
}
