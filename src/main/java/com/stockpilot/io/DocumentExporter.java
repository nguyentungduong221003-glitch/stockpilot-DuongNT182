package com.stockpilot.io;

import com.stockpilot.model.Order;
import com.stockpilot.model.OrderItem;
import com.stockpilot.model.Product;
import com.stockpilot.service.StockPilotService;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DocumentExporter {
    private static final String OUTPUT_DIR = "output";

    static {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String exportInvoice(Order order) throws IOException {
        String fileName = String.format("%s/invoice_%s.txt", OUTPUT_DIR, order.getId().substring(0, 8));
        File file = new File(fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("==================================================\n");
            writer.write("               STOCKPILOT INVOICE                 \n");
            writer.write("==================================================\n");
            writer.write(String.format("Order ID:   %s\n", order.getId()));
            writer.write(String.format("Date:       %s\n", order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            writer.write(String.format("Customer:   %s (%s)\n", order.getCustomer().getName(), order.getCustomer().getEmail()));
            writer.write(String.format("Phone:      %s\n", order.getCustomer().getPhone()));
            writer.write("--------------------------------------------------\n");
            writer.write(String.format("%-15s %-20s %-5s %-10s\n", "SKU", "Product Name", "Qty", "Price"));
            writer.write("--------------------------------------------------\n");

            for (OrderItem item : order.getOrderItems()) {
                writer.write(String.format("%-15s %-20s %-5d $%-9.2f\n",
                        item.getProduct().getSku(),
                        item.getProduct().getName().length() > 18 ? item.getProduct().getName().substring(0, 15) + "..." : item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ));
            }

            writer.write("--------------------------------------------------\n");
            writer.write(String.format("Original Total:                           $%.2f\n", order.getOriginalTotal()));
            writer.write(String.format("Discount applied (%s):             -$%.2f\n",
                    order.getDiscountPolicy().getPolicyName(), order.getDiscountAmount()));
            writer.write("--------------------------------------------------\n");
            writer.write(String.format("FINAL TOTAL:                              $%.2f\n", order.getFinalTotal()));
            writer.write("==================================================\n");
            writer.write("        Thank you for business with StockPilot!   \n");
            writer.write("==================================================\n");
        }

        return file.getAbsolutePath();
    }

    public String exportSalesReport(StockPilotService service) throws Exception {
        String fileName = String.format("%s/sales_report.txt", OUTPUT_DIR);
        File file = new File(fileName);

        BigDecimal totalRevenue = service.calculateTotalRevenue();
        long orderCount = service.getOrderCount();
        Map<String, BigDecimal> revenueByCategory = service.getRevenueByCategory();
        List<Product> lowStock = service.getLowStockProducts(5);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("==================================================\n");
            writer.write("          STOCKPILOT SALES ANALYTICS REPORT        \n");
            writer.write("==================================================\n");
            writer.write(String.format("Total Revenue:           $%.2f\n", totalRevenue));
            writer.write(String.format("Total Completed Orders:  %d\n", orderCount));
            writer.write("--------------------------------------------------\n");
            writer.write("               REVENUE BY CATEGORY                \n");
            writer.write("--------------------------------------------------\n");
            for (Map.Entry<String, BigDecimal> entry : revenueByCategory.entrySet()) {
                writer.write(String.format("Category: %-25s -> $%.2f\n", entry.getKey(), entry.getValue()));
            }
            writer.write("--------------------------------------------------\n");
            writer.write("          LOW STOCK ALERT (Threshold < 5)         \n");
            writer.write("--------------------------------------------------\n");
            if (lowStock.isEmpty()) {
                writer.write("All products are well stocked!\n");
            } else {
                writer.write(String.format("%-15s %-25s %-10s\n", "SKU", "Product Name", "In Stock"));
                for (Product p : lowStock) {
                    writer.write(String.format("%-15s %-25s %-10d\n",
                            p.getSku(),
                            p.getName().length() > 22 ? p.getName().substring(0, 20) + "..." : p.getName(),
                            p.getStockQuantity()));
                }
            }
            writer.write("==================================================\n");
            writer.write("             END OF SALES REPORT                  \n");
            writer.write("==================================================\n");
        }

        return file.getAbsolutePath();
    }
}
