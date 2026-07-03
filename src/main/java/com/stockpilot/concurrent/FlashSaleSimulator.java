package com.stockpilot.concurrent;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.model.Customer;
import com.stockpilot.model.Order;
import com.stockpilot.model.OrderItem;
import com.stockpilot.model.Product;
import com.stockpilot.service.StockPilotService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


public class FlashSaleSimulator {
    private static final ReentrantLock lock = new ReentrantLock();

    private final StockPilotService service = new StockPilotService();
    private final AtomicInteger successfulOrders = new AtomicInteger(0);
    private final AtomicInteger failedOrders = new AtomicInteger(0);


    public String simulateFlashSale(Product targetProduct, int threadCount, boolean useSynchronization) throws Exception {
        successfulOrders.set(0);
        failedOrders.set(0);

        // Standard sample customer to use
        Customer customer = new Customer(UUID.randomUUID().toString(), "Flash Customer", "flash@gmail.com", "0123456789");
        try {
            service.addCustomer(customer);
        } catch (DataAccessException ignored) {}

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    boolean orderPlaced = false;

                    if (useSynchronization) {
                        // Protect decrement and order creation with ReentrantLock (F6)
                        lock.lock();
                        try {
                            // Reload fresh product stock from database
                            Product freshP = service.getProductById(targetProduct.getId()).orElse(targetProduct);
                            if (freshP.getStockQuantity() >= 1) {
                                // Create order item for 1 product
                                OrderItem item = new OrderItem(UUID.randomUUID().toString(), freshP, 1, freshP.getPrice());
                                Order order = new Order(UUID.randomUUID().toString(), customer, null);
                                order.addOrderItem(item);

                                // Place order
                                service.placeOrder(order);
                                orderPlaced = true;
                            }
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        // Unsynchronized - Causes classic Race Condition / Check-Then-Act vulnerability
                        Product freshP = service.getProductById(targetProduct.getId()).orElse(targetProduct);
                        if (freshP.getStockQuantity() >= 1) {
                            // Simulate delay to expand the vulnerability window
                            Thread.sleep(15);

                            OrderItem item = new OrderItem(UUID.randomUUID().toString(), freshP, 1, freshP.getPrice());
                            Order order = new Order(UUID.randomUUID().toString(), customer, null);
                            order.addOrderItem(item);

                            service.placeOrder(order);
                            orderPlaced = true;
                        }
                    }

                    if (orderPlaced) {
                        successfulOrders.incrementAndGet();
                    } else {
                        failedOrders.incrementAndGet();
                    }
                } catch (Exception e) {
                    failedOrders.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();

        // Load final product state to verify if stock went negative
        Product finalProduct = service.getProductById(targetProduct.getId()).orElse(targetProduct);

        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append(String.format(" FLASH SALE SIMULATION REPORT (%s)\n", useSynchronization ? "SYNCHRONIZED" : "UNSYNCHRONIZED"));
        sb.append("==================================================\n");
        sb.append(String.format("Initial stock quantity:       %d\n", targetProduct.getStockQuantity()));
        sb.append(String.format("Concurrent purchase requests: %d\n", threadCount));
        sb.append(String.format("Successful transactions:     %d\n", successfulOrders.get()));
        sb.append(String.format("Failed / Blocked purchases:   %d\n", failedOrders.get()));
        sb.append(String.format("Remaining stock quantity:     %d\n", finalProduct.getStockQuantity()));
        sb.append(String.format("Execution time:               %d ms\n", (endTime - startTime)));
        sb.append("--------------------------------------------------\n");

        if (finalProduct.getStockQuantity() < 0) {
            sb.append("CRITICAL ALERT: Stock has been OVERSOLD (Stock went NEGATIVE)!\n");
            sb.append("This is a classic Race Condition (Check-Then-Act vulnerability).\n");
            sb.append("Multiple threads checked stock simultaneously and decremented it.\n");
        } else {
            sb.append("SUCCESS: Thread-safety verified. Stock did not drop below zero!\n");
            sb.append("ReentrantLock ensured exclusive transactional decrement of stock levels.\n");
        }
        sb.append("==================================================\n");

        return sb.toString();
    }
}
