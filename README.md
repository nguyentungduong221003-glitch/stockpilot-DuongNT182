# 📦 StockPilot - Inventory & Order Management System

A Java SE console-based application for managing inventory, customers, and orders.

---

## 🛠️ Tech Stack

- **Java 17**
- **Maven**
- **H2 Database** (file-based persistence)
- **JDBC**

---

## 📂 Project Structure

```
stockpilot-java/
├── src/main/java/com/stockpilot/
│   ├── Main.java
│   ├── concurrent/
│   ├── exception/
│   ├── io/
│   ├── model/
│   ├── repository/
│   ├── service/
│   └── util/
├── src/main/resources/
│   └── schema.sql
├── data/                    (auto-generated)
├── products.csv             (sample data for import)
├── pom.xml
└── README.md
```

---

## 🗄️ Database Schema

| Table | Description |
| :--- | :--- |
| `products` | Product catalog (id, sku, name, category, price, stock) |
| `customers` | Customer registry (id, name, email, phone) |
| `orders` | Order header (id, customer_id, order_date, discount, totals) |
| `order_items` | Order line items (id, order_id, product_id, quantity, unit_price) |

**Relationships:** `customers (1) → orders (N) → order_items (N) → products (1)`

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- Maven 3.9+

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/<yourusername>/stockpilot-<yourname>.git
cd stockpilot-<yourname>

# 2. Build and run
mvn clean compile test package
java -jar target/stockpilot-1.0.0.jar
```

### H2 Console (for debugging)

Access at: http://localhost:8082
- JDBC URL: `jdbc:h2:./data/stockpilot`
- Username: `sa`
- Password: (empty)

---

## ⚡ Race Condition Write-Up

When multiple threads buy the same product without synchronization:

1. Threads read the same stock value (e.g., stock = 10)
2. Multiple threads decrement based on stale data
3. Result: stock becomes negative → overselling occurs

**Solution:** Using `synchronized` ensures only one thread updates stock at a time.

---

## ✅ Feature Checklist

| Feature | Status |
| :--- | :--- |
| **F1 - Product Management** (CRUD) | ✅ |
| **F2 - Customer Management** (CRUD) | ✅ |
| **F3 - Order & Discount** (No, %, Bulk) | ✅ |
| **F4 - Streams Analytics** (Revenue, Reports) | ✅ |
| **F5 - File I/O** (CSV Import, Export) | ✅ |
| **F6 - Concurrency** (Flash Sale Sim) | ✅ |
| **H2 File Mode** | ✅ |
| **Transaction (commit/rollback)** | ✅ |

---

## 📝 Sample Data

`products.csv` for testing:

```csv
sku,name,category,price,stock_quantity
AAA-1001,Laptop,Electronics,999.99,10
AAA-1002,Smartphone,Electronics,599.99,15
BBB-2001,Coffee Maker,Home Appliances,79.99,5
```
