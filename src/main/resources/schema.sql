-- 1. Products Table (F1)
CREATE TABLE IF NOT EXISTS products (
                                        id VARCHAR(50) PRIMARY KEY,
    sku VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0
    );

-- 2. Customers Table (F2)
CREATE TABLE IF NOT EXISTS customers (
                                         id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50) NOT NULL
    );

-- 3. Orders Table (F3)
CREATE TABLE IF NOT EXISTS orders (
                                      id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    discount_policy_type VARCHAR(100) NOT NULL,
    total_original DECIMAL(10,2) NOT NULL,
    total_discount DECIMAL(10,2) NOT NULL,
    total_final DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
    );

-- 4. Order Items Table (F3)
CREATE TABLE IF NOT EXISTS order_items (
                                           id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
    );