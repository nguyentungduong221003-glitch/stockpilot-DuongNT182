package com.stockpilot.util;

import com.stockpilot.exception.DataAccessException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.sql.*;

public class DBConnectionHelper {

    private static final String JDBC_URL = "jdbc:h2:./data/stockpilot;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static boolean schemaInitialized = false;

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("CRITICAL ERROR: H2 JDBC Driver not found on the classpath. " +
                    "Make sure you are running the application via Maven or that h2-2.2.224.jar is included.", e);
        }
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
            if (!schemaInitialized) {
                initializeSchema(conn);
            }
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException("Could not acquire database connection", e);
        }
    }

    private static synchronized void initializeSchema(Connection conn) throws DataAccessException {
        if (schemaInitialized) return;
        try (Statement stmt = conn.createStatement()) {
            ResultSet tables = conn.getMetaData().getTables(null, null, "PRODUCTS", null);
            if (tables.next()) {
                System.out.println("Schema already exists. Skipping initialization.");
                schemaInitialized = true;
                return;
            }

            System.out.println("Creating schema for the first time...");
            InputStream in = DBConnectionHelper.class.getResourceAsStream("/schema.sql");
            if (in == null) {
                System.out.println("Warning: schema.sql resource not found. Creating table structures manually.");
                stmt.execute("CREATE TABLE products (" +
                        "id VARCHAR(50) PRIMARY KEY, sku VARCHAR(10) NOT NULL UNIQUE, name VARCHAR(255) NOT NULL, " +
                        "category VARCHAR(100) NOT NULL, price DECIMAL(10,2) NOT NULL, stock_quantity INT NOT NULL DEFAULT 0)");
                stmt.execute("CREATE TABLE customers (" +
                        "id VARCHAR(50) PRIMARY KEY, name VARCHAR(255) NOT NULL, email VARCHAR(255) NOT NULL UNIQUE, phone VARCHAR(50) NOT NULL)");
                stmt.execute("CREATE TABLE orders (" +
                        "id VARCHAR(50) PRIMARY KEY, customer_id VARCHAR(50) NOT NULL, order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "discount_policy_type VARCHAR(100) NOT NULL, total_original DECIMAL(10,2) NOT NULL, total_discount DECIMAL(10,2) NOT NULL, total_final DECIMAL(10,2) NOT NULL)");
                stmt.execute("CREATE TABLE order_items (" +
                        "id VARCHAR(50) PRIMARY KEY, order_id VARCHAR(50) NOT NULL, product_id VARCHAR(50) NOT NULL, " +
                        "quantity INT NOT NULL, unit_price DECIMAL(10,2) NOT NULL)");
            } else {
                BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));
                String sql = reader.lines().reduce("", (accumulator, line) -> accumulator + "\n" + line);
                stmt.execute(sql);
            }
            schemaInitialized = true;
            System.out.println("Schema created successfully!");
        } catch (Exception e) {
            throw new DataAccessException("Failed to initialize database schema", e);
        }
    }
}
