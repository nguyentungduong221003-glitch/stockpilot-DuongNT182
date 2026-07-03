package com.stockpilot;

import com.stockpilot.exception.DataAccessException;
import com.stockpilot.exception.InvalidInputException;
import com.stockpilot.exception.ProductNotFoundException;
import com.stockpilot.model.Customer;
import com.stockpilot.model.Order;
import com.stockpilot.model.OrderItem;
import com.stockpilot.model.Product;
import com.stockpilot.service.BulkDiscount;
import com.stockpilot.service.DiscountPolicy;
import com.stockpilot.service.NoDiscount;
import com.stockpilot.service.PercentageDiscount;
import com.stockpilot.service.StockPilotService;
import com.stockpilot.io.CSVImporter;
import com.stockpilot.io.DocumentExporter;
import com.stockpilot.concurrent.FlashSaleSimulator;
import java.math.BigDecimal;
import java.util.*;

public class Main {

    private static final StockPilotService service = new StockPilotService();
    private static final CSVImporter csvImporter = new CSVImporter();
    private static final DocumentExporter docExporter = new DocumentExporter();
    private static final FlashSaleSimulator flashSimulator = new FlashSaleSimulator();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println("      Welcome to StockPilot Java SE CLI System  ");
        System.out.println("================================================");

        boolean exit = false;
        while (!exit) {
            printMenu();
            System.out.print("Please enter your choice (1-10): ");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> createProduct();
                    case "2" -> listProducts();
                    case "3" -> searchProduct();
                    case "4" -> createCustomer();
                    case "5" -> listCustomers();
                    case "6" -> placeCustomerOrder();
                    case "7" -> showSalesReports();
                    case "8" -> importCSVCatalog();
                    case "9" -> runFlashSaleSimulation();
                    case "10" -> {
                        System.out.println("Thank you for using StockPilot. Goodbye!");
                        exit = true;
                    }
                    default -> System.out.println("Invalid choice. Please choose between 1 and 10.");
                }
            } catch (Exception e) {
                System.out.println("\n[ERROR] An error occurred: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("------------------------------------------------");
        System.out.println("                  MAIN MENU                     ");
        System.out.println("------------------------------------------------");
        System.out.println("1. Add a Product (F1)");
        System.out.println("2. List All Products (F1)");
        System.out.println("3. Search Product by SKU (F1)");
        System.out.println("4. Register a Customer (F2)");
        System.out.println("5. List All Customers (F2)");
        System.out.println("6. Build & Place Customer Order (F3)");
        System.out.println("7. View Analytical Reports (Streams) (F4)");
        System.out.println("8. Import Catalog from CSV (File I/O) (F5)");
        System.out.println("9. Simulate Concurrency Flash Sale (F6)");
        System.out.println("10. Exit Application");
        System.out.println("------------------------------------------------");
    }

    private static void createProduct() throws DataAccessException {
        System.out.println("\n--- Add a New Product ---");
        System.out.print("Enter SKU (format AAA-1234): ");
        String sku = scanner.nextLine().trim();

        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Category: ");
        String category = scanner.nextLine().trim();

        System.out.print("Enter Price ($): ");
        BigDecimal price = new BigDecimal(scanner.nextLine().trim());

        System.out.print("Enter Stock Quantity: ");
        int qty = Integer.parseInt(scanner.nextLine().trim());

        Product product = new Product(UUID.randomUUID().toString(), sku, name, category, price, qty);
        service.addProduct(product);
        System.out.println("\n[SUCCESS] Product registered successfully: " + product);
    }

    private static void listProducts() throws DataAccessException {
        System.out.println("\n--- Product Catalog ---");
        List<Product> products = service.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products found in database.");
        } else {
            products.forEach(p -> System.out.printf("- SKU: %-10s | %-25s | Category: %-15s | Price: $%-8.2f | Stock: %d\n",
                    p.getSku(), p.getName(), p.getCategory(), p.getPrice(), p.getStockQuantity()));
        }
    }

    private static void searchProduct() throws DataAccessException {
        System.out.print("\nEnter SKU to search: ");
        String sku = scanner.nextLine().trim();
        Optional<Product> p = service.getProductBySku(sku);
        if (p.isPresent()) {
            System.out.println("\n[FOUND] " + p.get());
        } else {
            System.out.println("\n[NOT FOUND] No product exists for SKU: " + sku);
        }
    }

    private static void createCustomer() throws DataAccessException {
        System.out.println("\n--- Register Customer ---");
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine().trim();

        Customer customer = new Customer(UUID.randomUUID().toString(), name, email, phone);
        service.addCustomer(customer);
        System.out.println("\n[SUCCESS] Customer registered successfully: " + customer);
    }

    private static void listCustomers() throws DataAccessException {
        System.out.println("\n--- Customer Registry ---");
        List<Customer> customers = service.getAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers registered yet.");
        } else {
            customers.forEach(c -> System.out.printf("- Name: %-20s | Email: %-25s | Phone: %s\n",
                    c.getName(), c.getEmail(), c.getPhone()));
        }
    }

    private static void placeCustomerOrder() throws Exception {
        System.out.println("\n--- Place Customer Order ---");
        System.out.print("Enter Customer Email: ");
        String email = scanner.nextLine().trim();

        Customer customer = service.getCustomerByEmail(email)
                .orElseThrow(() -> new InvalidInputException("No customer exists with email: " + email));

        Order order = new Order(UUID.randomUUID().toString(), customer, null);

        boolean addingItems = true;
        while (addingItems) {
            System.out.print("Enter Product SKU to buy: ");
            String sku = scanner.nextLine().trim();

            Product product = service.getProductBySku(sku)
                    .orElseThrow(() -> new ProductNotFoundException("Product SKU not found: " + sku));

            System.out.printf("Enter Quantity (In Stock: %d): ", product.getStockQuantity());
            int qty = Integer.parseInt(scanner.nextLine().trim());

            OrderItem item = new OrderItem(UUID.randomUUID().toString(), product, qty, product.getPrice());
            order.addOrderItem(item);

            System.out.print("Add another item? (y/n): ");
            addingItems = scanner.nextLine().trim().equalsIgnoreCase("y");
        }

        // F3 - Choose discount policy
        System.out.println("\nChoose a Discount Policy:");
        System.out.println("1. No Discount");
        System.out.println("2. Percentage Discount (10% off)");
        System.out.println("3. Bulk Discount (15% off if buying >= 5 total items)");
        System.out.print("Select policy (1-3): ");
        String policyChoice = scanner.nextLine().trim();

        DiscountPolicy policy = switch (policyChoice) {
            case "2" -> new PercentageDiscount(BigDecimal.valueOf(10));
            case "3" -> new BulkDiscount(5, BigDecimal.valueOf(15));
            default -> new NoDiscount();
        };
        order.setDiscountPolicy(policy);

        // F3 Rule: transactional placement
        service.placeOrder(order);
        System.out.println("\n[SUCCESS] Order placed successfully!");

        // Export Invoice automatically (F5)
        String invoicePath = docExporter.exportInvoice(order);
        System.out.println("[SUCCESS] Invoice exported to: " + invoicePath);
    }

    private static void showSalesReports() throws Exception {
        System.out.println("\n--- Analytical Sales Reports (Streams) ---");
        BigDecimal totalRev = service.calculateTotalRevenue();
        long orderCount = service.getOrderCount();

        System.out.printf("Total Completed Orders: %d\n", orderCount);
        System.out.printf("Total Gross Revenue:     $%.2f\n", totalRev);

        System.out.println("\n--- Revenue By Product Category ---");
        service.getRevenueByCategory().forEach((category, revenue) ->
                System.out.printf("- Category: %-18s -> Total Revenue: $%.2f\n", category, revenue));

        System.out.println("\n--- Low Stock Alerts (Products under 5 items in stock) ---");
        List<Product> lowStock = service.getLowStockProducts(5);
        if (lowStock.isEmpty()) {
            System.out.println("All items are adequately stocked.");
        } else {
            lowStock.forEach(p -> System.out.printf("⚠️ SKU: %-10s | %-20s | In Stock: %d\n",
                    p.getSku(), p.getName(), p.getStockQuantity()));
        }

        // Export snapshot (F5)
        String reportPath = docExporter.exportSalesReport(service);
        System.out.println("\n[SUCCESS] Sales Snapshot exported to: " + reportPath);
    }

    private static void importCSVCatalog() throws Exception {
        System.out.println("\n--- Onboard Catalog from CSV ---");
        System.out.print("Enter CSV File Path (or press Enter for 'products.csv'): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            path = "products.csv";
        }
        List<Product> imported = csvImporter.importProducts(path);
        System.out.printf("\n[SUCCESS] Loaded and saved %d new products from catalog CSV file.\n", imported.size());
    }

    private static void runFlashSaleSimulation() throws Exception {
        System.out.println("\n--- High-Traffic Flash Sale Thread Simulation ---");
        System.out.print("Enter Product SKU to target: ");
        String sku = scanner.nextLine().trim();

        Product p = service.getProductBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product SKU not found: " + sku));

        System.out.printf("Current stock of '%s' (SKU: %s): %d\n", p.getName(), p.getSku(), p.getStockQuantity());
        System.out.print("Enter number of concurrent threads (e.g. 10): ");
        int threads = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("\nHow do you want to run the simulation?");
        System.out.println("1. Unsynchronized (Causes classic race condition - overselling stock)");
        System.out.println("2. Synchronized (Lock-protected - robust thread-safety)");
        System.out.print("Choose mode (1-2): ");
        boolean useSync = scanner.nextLine().trim().equals("2");

        System.out.println("\nSpawning threads... Placed transactions in background Executor pool...");
        String report = flashSimulator.simulateFlashSale(p, threads, useSync);
        System.out.println(report);
    }
}
