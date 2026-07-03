```markdown
# 📦 StockPilot - Inventory & Order Management System

A Java SE console-based application for managing inventory, customers, and orders with support for discount policies, CSV import/export, and concurrency simulation.

---

## 📝 Description

StockPilot is a command-line inventory and order management system built as a Java SE assignment. It demonstrates core Java concepts including:

- **JDBC** with H2 database (file-based persistence)
- **Maven** for dependency management and build
- **Repository Pattern** for data access
- **Streams API** for analytical reporting
- **Concurrency** with thread-safe flash sale simulation
- **File I/O** for CSV import and document export
- **Transaction Management** with commit/rollback

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Java** | 17 | Core programming language |
| **Maven** | 3.9+ | Build automation & dependency management |
| **H2 Database** | 2.2.224 | Embedded file-based database |
| **JDBC** | - | Database connectivity |
| **Java Streams** | - | Data processing & reporting |
| **Java Concurrency** | - | Thread-safe flash sale simulation |

---

## Project Structure

```
stockpilot-java/
├── src/
│   ├── main/
│   │   ├── java/com/stockpilot/
│   │   │   ├── Main.java                 # CLI entry point
│   │   │   ├── concurrent/              # FlashSaleSimulator
│   │   │   ├── exception/               # Custom exceptions
│   │   │   ├── io/                      # CSVImporter, DocumentExporter
│   │   │   ├── model/                   # Product, Customer, Order, OrderItem
│   │   │   ├── repository/              # JDBC repositories
│   │   │   ├── service/                 # Business logic, discount policies
│   │   │   └── util/                    # DBConnectionHelper
│   │   └── resources/
│   │       └── schema.sql               # Database schema definition
│   └── test/                            # Unit tests (JUnit 5)
├── data/                                # H2 database files (auto-generated)
├── products.csv                         # Sample data for CSV import
├── pom.xml                              # Maven configuration
└── README.md                            # This file
```

---

## Database Schema

The system uses H2 database with the following tables:

### `products` (F1)
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | VARCHAR(50) | Primary Key (UUID) |
| `sku` | VARCHAR(10) | Unique product identifier |
| `name` | VARCHAR(255) | Product name |
| `category` | VARCHAR(100) | Product category |
| `price` | DECIMAL(10,2) | Unit price |
| `stock_quantity` | INT | Available stock |

### `customers` (F2)
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | VARCHAR(50) | Primary Key (UUID) |
| `name` | VARCHAR(255) | Customer name |
| `email` | VARCHAR(255) | Unique email address |
| `phone` | VARCHAR(50) | Phone number |

### `orders` (F3)
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | VARCHAR(50) | Primary Key (UUID) |
| `customer_id` | VARCHAR(50) | Foreign Key → customers(id) |
| `order_date` | TIMESTAMP | Order creation timestamp |
| `discount_policy_type` | VARCHAR(100) | Applied discount policy |
| `total_original` | DECIMAL(10,2) | Subtotal before discount |
| `total_discount` | DECIMAL(10,2) | Discount amount |
| `total_final` | DECIMAL(10,2) | Final total after discount |

### `order_items` (F3)
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | VARCHAR(50) | Primary Key (UUID) |
| `order_id` | VARCHAR(50) | Foreign Key → orders(id) ON DELETE CASCADE |
| `product_id` | VARCHAR(50) | Foreign Key → products(id) |
| `quantity` | INT | Quantity ordered |
| `unit_price` | DECIMAL(10,2) | Price at time of order |

**Entity Relationship Diagram:**
```
customers (1) - (N) orders (1) - (N) order_items (N) - (1) products
```

---

## How to Set Up and Run

### Prerequisites
- **Java 17** or higher
- **Maven 3.9+** (or use IntelliJ IDEA with built-in Maven)

### Step 1: Clone the Repository
```bash
git clone https://github.com/nguyentungduong221003-glitch/stockpilot-DuongNT182.git
cd stockpilot-DuongNT182
```

### Step 2: Build the Project
```bash
mvn clean package
```

This will:
- Download all dependencies (H2, JUnit)
- Compile the source code
- Run tests (if any)
- Create a FAT JAR file at `target/stockpilot-1.0.0.jar`

### Step 3: Run the Application

**Option A: Run with Maven**
```bash
mvn exec:java -Dexec.mainClass="com.stockpilot.Main"
```

**Option B: Run the JAR file directly**
```bash
java -jar target/stockpilot-1.0.0.jar
```

**Option C: Run from IntelliJ IDEA**
- Open the project
- Right-click on `Main.java` → `Run 'Main.main()'`

### Step 4: The H2 Database
- The database file is auto-created at `./data/stockpilot.mv.db`
- Schema is initialized on first run
- For debugging, access the **H2 Console** at: http://localhost:8082
  - JDBC URL: `jdbc:h2:./data/stockpilot`
  - Username: `sa`
  - Password: (leave empty)

---

## 📋 Feature Checklist

| Feature | Requirement | Status |
| :--- | :--- | :--- |
| **F1 - Product Management** | | |
| | Add Product (CLI input) | ✅ Pass |
| | List All Products | ✅ Pass |
| | Search Product by SKU | ✅ Pass |
| | Update Stock Quantity | ✅ Pass |
| | Product Repository (CRUD) | ✅ Good |
| **F2 - Customer Management** | | |
| | Register Customer | ✅ Pass |
| | List All Customers | ✅ Pass |
| | Find Customer by Email | ✅ Pass |
| | Customer Repository (CRUD) | ✅ Good |
| **F3 - Order & Discount** | | |
| | Create Order with Items | ✅ Pass |
| | Apply Discount Policies | ✅ Pass |
| | - No Discount | ✅ Pass |
| | - Percentage Discount (10%) | ✅ Pass |
| | - Bulk Discount (>=5 items, 15%) | ✅ Pass |
| | Atomic Transaction (commit/rollback) | ✅ Good |
| | Order Repository | ✅ Good |
| **F4 - Streams Analytics** | | |
| | Calculate Total Revenue | ✅ Pass |
| | Revenue by Category | ✅ Pass |
| | Low Stock Alerts (<5 items) | ✅ Pass |
| | Top N Best-Selling Products | ✅ Good |
| | Order Count | ✅ Pass |
| **F5 - File I/O** | | |
| | Import Products from CSV | ✅ Pass |
| | Export Invoice (Order) | ✅ Pass |
| | Export Sales Report | ✅ Pass |
| | Try-with-resources usage | ✅ Good |
| **F6 - Concurrency** | | |
| | Flash Sale Simulation | ✅ Pass |
| | - Unsynchronized (race condition) | ✅ Pass |
| | - Synchronized (thread-safe) | ✅ Pass |
| | ExecutorService usage | ✅ Good |
| **Database** | | |
| | H2 File Mode | ✅ Pass |
| | Schema Initialization | ✅ Pass |
| | PreparedStatement (no SQL injection) | ✅ Good |
| | Connection Pooling (auto-close) | ✅ Good |

---

## ⚡ Race Condition Write-Up

### What is a Race Condition?

A **race condition** occurs when multiple threads access shared data concurrently, and the final outcome depends on the timing of thread execution. In the context of stock management, this can lead to **overselling** - selling more products than available in inventory.

### Demonstration in StockPilot

When running the **unsynchronized** flash sale simulation (Option 1):
- 10+ threads concurrently attempt to purchase the same product
- Without synchronization, threads may read the same stock value (e.g., stock = 10)
- Multiple threads decrement the stock based on the stale value
- Result: `stock` becomes negative, overselling occurs

**Example:**
```
Thread 1 reads stock = 10 → decrements to 9 → writes stock = 9
Thread 2 reads stock = 10 (before Thread 1 writes) → decrements to 9 → writes stock = 9
Thread 3 reads stock = 10 → decrements to 9 → writes stock = 9
...
Final stock = 9 (instead of 7!) → 3 items oversold!
```

### Solution: Synchronized Access

Using `synchronized` keyword (Option 2):
- Threads acquire a **lock** before accessing shared stock
- Only one thread can read and decrement stock at a time
- Guarantees correct inventory updates

**Result:**
```
stock = 10 → Thread 1 buys 1 → stock = 9
stock = 9 → Thread 2 buys 1 → stock = 8
stock = 8 → Thread 3 buys 1 → stock = 7
...
Final stock = 7 (correct!)
```

### Running the Simulation
In the application menu, select `9` → Enter product SKU → Choose:
- **1**: Unsynchronized (show race condition)
- **2**: Synchronized (show thread-safety)

---

## 📊 Sample Data

A sample `products.csv` file is provided in the root directory for testing CSV import:

```csv
sku,name,category,price,stock_quantity
AAA-1001,Laptop,Electronics,999.99,10
AAA-1002,Smartphone,Electronics,599.99,15
BBB-2001,Coffee Maker,Home Appliances,79.99,5
CCC-3001,Notebook,Stationery,2.99,100
```

---

## 👨‍💻 Author

| Name | GitHub |
| :--- | :--- |
| NguyenTungDuong | https://github.com/nguyentungduong221003-glitch|

---

## 📚 References

- [H2 Database Documentation](https://www.h2database.com/html/main.html)
- [Maven in 5 Minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
- [JDBC Tutorial](https://docs.oracle.com/javase/tutorial/jdbc/)
- [Java Concurrency Guide](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [Java Streams API](https://docs.oracle.com/javase/tutorial/collections/streams/)

---

## 🧪 Testing

Run tests (if implemented):
```bash
mvn test
```
---

## Contributing

This is a student assignment. For suggestions or improvements, feel free to open an issue or submit a pull request.

---

```


