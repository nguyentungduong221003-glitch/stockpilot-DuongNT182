package com.stockpilot;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.exception.InsufficientStockException;
import com.stockpilot.exception.InvalidInputException;
import com.stockpilot.model.Customer;
import com.stockpilot.model.Order;
import com.stockpilot.model.OrderItem;
import com.stockpilot.model.Product;
import com.stockpilot.service.BulkDiscount;
import com.stockpilot.service.PercentageDiscount;
import com.stockpilot.service.StockPilotService;
import com.stockpilot.util.DBConnectionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class StockPilotTest {
    private final StockPilotService service = new StockPilotService();

    @BeforeEach
    public void setUp() throws Exception {
        // Clear all tables to get a clean testing state
        try (Connection conn = DBConnectionHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM order_items");
            stmt.execute("DELETE FROM orders");
            stmt.execute("DELETE FROM customers");
            stmt.execute("DELETE FROM products");
        }
    }

    @Test
    public void testProductSkuValidation() {
        Product p = new Product();
        assertDoesNotThrow(() -> p.setSku("LAP-1234"));
        assertThrows(InvalidInputException.class, () -> p.setSku("lap-1234"));
        assertThrows(InvalidInputException.class, () -> p.setSku("LAPP-123"));
        assertThrows(InvalidInputException.class, () -> p.setSku("LAP-123a"));
    }

    // Test 2: Product Price and Stock Bounds
    @Test
    public void testProductBounds() {
        Product p = new Product();
        assertThrows(InvalidInputException.class, () -> p.setPrice(BigDecimal.valueOf(-10)));
        assertThrows(InvalidInputException.class, () -> p.setStockQuantity(-5));
    }

    // Test 3: Customer Input format Validation
    @Test
    public void testCustomerValidation() {
        Customer c = new Customer();
        c.setName("John Doe");
        assertDoesNotThrow(() -> c.setEmail("john@fpt.com"));
        assertDoesNotThrow(() -> c.setPhone("+84-12345678"));
        assertThrows(InvalidInputException.class, () -> c.setEmail("johnfpt.com"));
        assertThrows(InvalidInputException.class, () -> c.setPhone("abcde1234"));
    }

    // Test 4: Product CRUD and List operations
    @Test
    public void testProductServiceCrud() throws DataAccessException {
        Product p = new Product(UUID.randomUUID().toString(), "LAP-4522", "Elitebook Laptop", "Electronics", BigDecimal.valueOf(1200), 10);
        service.addProduct(p);
        Optional<Product> found = service.getProductBySku("LAP-4522");
        assertTrue(found.isPresent());
        assertEquals("Elitebook Laptop", found.get().getName());
        List<Product> list = service.getAllProducts();
        assertEquals(1, list.size());
    }

    // Test 5: Standard Place-order with Stock check & decrement
    @Test
    public void testPlaceOrderAndStockDecrement() throws DataAccessException {
        Product p = new Product(UUID.randomUUID().toString(), "PHN-3321", "Samsung S23", "Electronics", BigDecimal.valueOf(800), 5);
        Customer c = new Customer(UUID.randomUUID().toString(), "Alex", "alex@fpt.com", "0901234567");
        service.addProduct(p);
        service.addCustomer(c);
        Order order = new Order(UUID.randomUUID().toString(), c, null);
        order.addOrderItem(new OrderItem(UUID.randomUUID().toString(), p, 2, p.getPrice()));
        service.placeOrder(order);
        Product dbProduct = service.getProductById(p.getId()).orElseThrow();
        assertEquals(3, dbProduct.getStockQuantity());
    }

    // Test 6: Throws InsufficientStockException
    @Test
    public void testPlaceOrderInsufficientStock() throws DataAccessException {
        Product p = new Product(UUID.randomUUID().toString(), "TAB-9912", "iPad Pro", "Electronics", BigDecimal.valueOf(600), 2);
        Customer c = new Customer(UUID.randomUUID().toString(), "Alex", "alex@fpt.com", "0901234567");
        service.addProduct(p);
        service.addCustomer(c);
        Order order = new Order(UUID.randomUUID().toString(), c, null);
        order.addOrderItem(new OrderItem(UUID.randomUUID().toString(), p, 5, p.getPrice()));
        assertThrows(DataAccessException.class, () -> service.placeOrder(order));
        Product dbProduct = service.getProductById(p.getId()).orElseThrow();
        assertEquals(2, dbProduct.getStockQuantity());
    }

    // Test 7: Polymorphic Discount Policy (Percentage & Bulk)
    @Test
    public void testPolymorphicDiscountPolicy() {
        Customer c = new Customer(UUID.randomUUID().toString(), "Alex", "alex@fpt.com", "0901234567");
        Product p = new Product(UUID.randomUUID().toString(), "MOU-1011", "Mouse", "Accessories", BigDecimal.valueOf(100), 10);

        Order order = new Order(UUID.randomUUID().toString(), c, null);
        order.addOrderItem(new OrderItem(UUID.randomUUID().toString(), p, 3, p.getPrice())); // Total: $300

        order.setDiscountPolicy(new PercentageDiscount(BigDecimal.valueOf(10)));
        assertEquals(BigDecimal.valueOf(30).setScale(2), order.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(270).setScale(2), order.getFinalTotal());

        order.setDiscountPolicy(new BulkDiscount(5, BigDecimal.valueOf(15)));
        assertEquals(BigDecimal.ZERO, order.getDiscountAmount());

        order.addOrderItem(new OrderItem(UUID.randomUUID().toString(), p, 3, p.getPrice())); // Total is now 6 items, $600
        assertEquals(BigDecimal.valueOf(90).setScale(2), order.getDiscountAmount()); // 15% of $600 is $90
        assertEquals(BigDecimal.valueOf(510).setScale(2), order.getFinalTotal());
    }

    // Test 8: Streams reporting & Analytics
    @Test
    public void testStreamsReporting() throws DataAccessException {
        Product p1 = new Product(UUID.randomUUID().toString(), "LAP-1022", "Laptop", "Electronics", BigDecimal.valueOf(1000), 10);
        Product p2 = new Product(UUID.randomUUID().toString(), "MON-5011", "Monitor", "Hardware", BigDecimal.valueOf(300), 5);
        Product p3 = new Product(UUID.randomUUID().toString(), "MOU-8022", "Mouse", "Accessories", BigDecimal.valueOf(50), 20);

        service.addProduct(p1);
        service.addProduct(p2);
        service.addProduct(p3);

        Customer c = new Customer(UUID.randomUUID().toString(), "Bob", "bob@gmail.com", "0912345678");
        service.addCustomer(c);

        Order o1 = new Order(UUID.randomUUID().toString(), c, null);
        o1.addOrderItem(new OrderItem(UUID.randomUUID().toString(), p1, 1, p1.getPrice()));
        service.placeOrder(o1);

        Order o2 = new Order(UUID.randomUUID().toString(), c, null);
        o2.addOrderItem(new OrderItem(UUID.randomUUID().toString(), p2, 1, p2.getPrice()));
        o2.addOrderItem(new OrderItem(UUID.randomUUID().toString(), p3, 2, p3.getPrice()));
        service.placeOrder(o2);

        assertEquals(BigDecimal.valueOf(1400).setScale(2), service.calculateTotalRevenue());

        Map<String, BigDecimal> revenueMap = service.getRevenueByCategory();
        assertEquals(BigDecimal.valueOf(1000).setScale(2), revenueMap.get("Electronics"));
        assertEquals(BigDecimal.valueOf(300).setScale(2), revenueMap.get("Hardware"));
        assertEquals(BigDecimal.valueOf(100).setScale(2), revenueMap.get("Accessories"));

        List<Product> lowStock = service.getLowStockProducts(5);
        assertEquals(1, lowStock.size());
        assertEquals("MON-5011", lowStock.get(0).getSku());
    }
}
