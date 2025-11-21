-- 1. Create database
CREATE DATABASE IF NOT EXISTS CloudKitchenInventory_db;
USE CloudKitchenInventory_db;

-- 2. Drop tables if they exist
DROP TABLE IF EXISTS StockMovement;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS Supplier;
DROP TABLE IF EXISTS StorageLocation;

-- 3. Create tables in correct order

-- Storage locations
CREATE TABLE StorageLocation (
    location_id INT PRIMARY KEY AUTO_INCREMENT,
    location_name VARCHAR(255) NOT NULL,
    area_description VARCHAR(255),
    capacity DECIMAL(10,2), -- in kg/liters or pcs
    temperature_control VARCHAR(50) DEFAULT 'None'
);

-- Suppliers
CREATE TABLE Supplier (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_name VARCHAR(100),
    contact_person VARCHAR(100),
    contact_number VARCHAR(30),
    email VARCHAR(100),
    address VARCHAR(255),
    supplier_status VARCHAR(30) DEFAULT 'Active'
);

-- Products (ingredients, beverages, packaging)
CREATE TABLE Product (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50), -- Ingredient, Beverage, Packaging, Kitchen Supply
    unit_of_measure VARCHAR(50), -- kg, liters, pcs, packs
    reorder_level DECIMAL(10,2) DEFAULT 0,
    supplier_id INT,
    location_id INT,
    product_status VARCHAR(30) DEFAULT 'Active',
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id),
    FOREIGN KEY (location_id) REFERENCES StorageLocation(location_id)
);

-- Stock movements
CREATE TABLE StockMovement (
    movement_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    location_id INT NOT NULL,
    supplier_id INT,
    quantity DECIMAL(10,2) NOT NULL,
    movement_type ENUM('IN','OUT') NOT NULL,
    movement_date DATE NOT NULL,
    reason VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES Product(product_id),
    FOREIGN KEY (location_id) REFERENCES StorageLocation(location_id),
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);

-- 4. Insert sample data

-- StorageLocation
INSERT INTO StorageLocation (location_name, area_description, capacity, temperature_control)
VALUES
('Freezer', 'Meat & seafood', 200, 'Freezer'),
('Cold Storage', 'Vegetables & dairy', 150, 'Refrigerated'),
('Dry Pantry', 'Spices & dry goods', 500, 'None'),
('Packaging Room', 'Disposable boxes & cups', 200, 'None');

-- Supplier
INSERT INTO Supplier (supplier_name, contact_person, contact_number, email, address, supplier_status)
VALUES
('Fresh Farms', 'John Doe', '09123456789', 'john@freshfarms.com', '123 Farm St., Manila', 'Active'),
('Beverage Supplies Inc.', 'Jane Smith', '09987654321', 'jane@bevsupply.com', '456 Drink Rd., Makati', 'Active'),
('Packaging World', 'Alice Tan', '09234567890', 'alice@packworld.com', '789 Packaging Ave., Quezon City', 'Active');

-- Product (now with supplier_id and location_id)
INSERT INTO Product (product_name, description, category, unit_of_measure, reorder_level, supplier_id, location_id, product_status)
VALUES
('Rice', 'Long grain rice', 'Ingredient', 'kg', 10, 1, 3, 'Active'),
('Chicken', 'Fresh chicken meat', 'Ingredient', 'kg', 5, 1, 1, 'Active'),
('Soda Can', 'Carbonated beverage', 'Beverage', 'pcs', 20, 2, 2, 'Active'),
('Disposable Box', 'Food container', 'Packaging', 'pcs', 50, 3, 4, 'Active'),
('Olive Oil', 'Extra virgin olive oil', 'Ingredient', 'liters', 5, 1, 3, 'Active'),
('Cheese', 'Mozzarella cheese', 'Ingredient', 'kg', 3, 1, 2, 'Active'),
('Tomatoes', 'Fresh red tomatoes', 'Ingredient', 'kg', 8, 1, 2, 'Active'),
('Ground Pork', 'Lean ground pork meat', 'Ingredient', 'kg', 4, 1, 1, 'Active'),
('Beef Patty', 'Frozen beef patty', 'Ingredient', 'pcs', 30, 1, 1, 'Active'),
('Burger Buns', 'Freshly baked burger buns', 'Ingredient', 'pcs', 40, 1, 3, 'Active'),
('Lettuce', 'Crisp iceberg lettuce', 'Ingredient', 'kg', 3, 1, 2, 'Active'),
('Onions', 'White onions', 'Ingredient', 'kg', 5, 1, 3, 'Active'),
('Garlic', 'Fresh garlic bulbs', 'Ingredient', 'kg', 2, 1, 3, 'Active'),
('Spaghetti Pasta', 'Dry pasta noodles', 'Ingredient', 'kg', 6, 1, 3, 'Active'),
('Tomato Sauce', 'Canned tomato sauce', 'Ingredient', 'cans', 15, 1, 3, 'Active'),
('Milk', 'Evaporated milk', 'Ingredient', 'cans', 12, 1, 2, 'Active'),
('Sugar', 'White refined sugar', 'Ingredient', 'kg', 10, 1, 3, 'Active'),
('Salt', 'Iodized salt', 'Ingredient', 'kg', 8, 1, 3, 'Active'),
('Black Pepper', 'Ground black pepper', 'Ingredient', 'g', 200, 1, 3, 'Active'),
('Soy Sauce', 'Dark soy sauce', 'Ingredient', 'bottles', 6, 1, 3, 'Active'),
('Vinegar', 'Cane vinegar', 'Ingredient', 'bottles', 6, 1, 3, 'Active'),
('Paper Cups', '12 oz disposable cups', 'Packaging', 'pcs', 100, 3, 4, 'Active'),
('Plastic Spoons', 'Disposable spoons', 'Packaging', 'pcs', 200, 3, 4, 'Active'),
('Plastic Forks', 'Disposable forks', 'Packaging', 'pcs', 200, 3, 4, 'Active'),
('Napkins', 'Paper napkins', 'Packaging', 'pcs', 300, 3, 4, 'Active'),
('Bottled Water', 'Mineral water 500ml', 'Beverage', 'bottles', 40, 2, 2, 'Active'),
('Orange Juice', 'Fresh carton orange juice', 'Beverage', 'liters', 10, 2, 2, 'Active'),		
('Frozen Fries', 'Shoestring french fries', 'Ingredient', 'kg', 7, 1, 1, 'Active'),
('Butter', 'Salted butter', 'Ingredient', 'kg', 4, 1, 2, 'Active'),
('Ketchup', 'Tomato ketchup', 'Ingredient', 'bottles', 10, 1, 3, 'Active');

-- StockMovement Data with varied dates across 2023-2025

-- ===== 2023 TRANSACTIONS =====

-- January 2023 - Initial Stock
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(1, 3, 1, 100, 'IN', '2023-01-05', 'Product Restock'),
(2, 1, 1, 50, 'IN', '2023-01-05', 'Product Restock'),
(3, 2, 2, 200, 'IN', '2023-01-06', 'Product Restock'),
(4, 4, 3, 500, 'IN', '2023-01-06', 'Product Restock'),
(5, 3, 1, 20, 'IN', '2023-01-07', 'Product Restock');

-- February 2023
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(1, 3, NULL, 25, 'OUT', '2023-02-10', 'Sales - Amount: 1250.00'),
(2, 1, NULL, 10, 'OUT', '2023-02-12', 'Sales - Amount: 2500.00'),
(6, 2, 1, 15, 'IN', '2023-02-15', 'Product Restock'),
(7, 2, 1, 30, 'IN', '2023-02-15', 'Product Restock');

-- March 2023
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(3, 2, NULL, 50, 'OUT', '2023-03-08', 'Sales - Amount: 500.00'),
(8, 1, 1, 25, 'IN', '2023-03-10', 'Product Restock'),
(9, 1, 1, 100, 'IN', '2023-03-10', 'Product Restock'),
(10, 3, 1, 150, 'IN', '2023-03-12', 'Product Restock');

-- April 2023
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(1, 3, 1, 50, 'IN', '2023-04-05', 'Product Restock'),
(2, 1, NULL, 8, 'OUT', '2023-04-10', 'Sales - Amount: 2000.00'),
(11, 2, 1, 20, 'IN', '2023-04-12', 'Product Restock'),
(12, 3, 1, 25, 'IN', '2023-04-15', 'Product Restock');

-- May 2023
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(13, 3, 1, 15, 'IN', '2023-05-03', 'Product Restock'),
(14, 3, 1, 30, 'IN', '2023-05-03', 'Product Restock'),
(15, 3, 1, 40, 'IN', '2023-05-05', 'Product Restock'),
(9, 1, NULL, 25, 'OUT', '2023-05-08', 'Sales - Amount: 625.00');

-- ===== 2024 TRANSACTIONS =====

-- January 2024
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(1, 3, 1, 80, 'IN', '2024-01-08', 'Product Restock'),
(2, 1, 1, 40, 'IN', '2024-01-08', 'Product Restock'),
(16, 2, 1, 25, 'IN', '2024-01-10', 'Product Restock'),
(17, 3, 1, 50, 'IN', '2024-01-10', 'Product Restock');

-- February 2024
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(18, 3, 1, 40, 'IN', '2024-02-05', 'Product Restock'),
(19, 3, 1, 5, 'IN', '2024-02-05', 'Product Restock'),
(1, 3, NULL, 30, 'OUT', '2024-02-12', 'Sales - Amount: 1500.00'),
(2, 1, NULL, 12, 'OUT', '2024-02-15', 'Sales - Amount: 3000.00');

-- March 2024
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(20, 3, 1, 20, 'IN', '2024-03-03', 'Product Restock'),
(21, 3, 1, 20, 'IN', '2024-03-03', 'Product Restock'),
(22, 4, 3, 300, 'IN', '2024-03-05', 'Product Restock'),
(23, 4, 3, 500, 'IN', '2024-03-05', 'Product Restock');

-- April 2024
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(24, 4, 3, 500, 'IN', '2024-04-02', 'Product Restock'),
(25, 4, 3, 800, 'IN', '2024-04-02', 'Product Restock'),
(3, 2, NULL, 40, 'OUT', '2024-04-10', 'Sales - Amount: 400.00'),
(4, 4, NULL, 100, 'OUT', '2024-04-12', 'Sales - Amount: 500.00');

-- May 2024
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(26, 2, 2, 100, 'IN', '2024-05-05', 'Product Restock'),
(27, 2, 2, 30, 'IN', '2024-05-05', 'Product Restock'),
(6, 2, NULL, 5, 'OUT', '2024-05-08', 'Sales - Amount: 1500.00'),
(7, 2, NULL, 10, 'OUT', '2024-05-10', 'Sales - Amount: 500.00');

-- June 2024
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(28, 1, 1, 40, 'IN', '2024-06-03', 'Product Restock'),
(29, 2, 1, 20, 'IN', '2024-06-03', 'Product Restock'),
(30, 3, 1, 30, 'IN', '2024-06-05', 'Product Restock'),
(8, 1, NULL, 8, 'OUT', '2024-06-08', 'Sales - Amount: 2000.00');

-- July 2024 - Product Returns
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(2, 1, NULL, 3, 'IN', '2024-07-05', 'Product Return: Excess from catering event'),
(9, 1, NULL, 15, 'IN', '2024-07-08', 'Product Return: Unused stock'),
(1, 3, 1, 60, 'IN', '2024-07-10', 'Product Restock'),
(10, 3, NULL, 30, 'OUT', '2024-07-15', 'Sales - Amount: 300.00');

-- August 2024 - Supplier Returns
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(7, 2, 1, 5, 'OUT', '2024-08-03', 'Supplier Return: Quality issues'),
(11, 2, 1, 2, 'OUT', '2024-08-05', 'Supplier Return: Wilted produce'),
(2, 1, 1, 35, 'IN', '2024-08-10', 'Product Restock'),
(3, 2, 2, 150, 'IN', '2024-08-12', 'Product Restock');

-- September 2024
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(12, 3, NULL, 10, 'OUT', '2024-09-05', 'Sales - Amount: 500.00'),
(13, 3, NULL, 5, 'OUT', '2024-09-08', 'Sales - Amount: 250.00'),
(14, 3, 1, 25, 'IN', '2024-09-10', 'Product Restock'),
(15, 3, 1, 35, 'IN', '2024-09-10', 'Product Restock');

-- October 2024
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(16, 2, 1, 20, 'IN', '2024-10-05', 'Product Restock'),
(17, 3, 1, 40, 'IN', '2024-10-05', 'Product Restock'),
(6, 2, NULL, 8, 'OUT', '2024-10-10', 'Sales - Amount: 2400.00'),
(28, 1, NULL, 12, 'OUT', '2024-10-15', 'Sales - Amount: 1200.00');

-- November 2024
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(1, 3, 1, 100, 'IN', '2024-11-03', 'Product Restock'),
(2, 1, 1, 45, 'IN', '2024-11-03', 'Product Restock'),
(22, 4, NULL, 150, 'OUT', '2024-11-08', 'Sales - Amount: 750.00'),
(23, 4, NULL, 200, 'OUT', '2024-11-10', 'Sales - Amount: 400.00');

-- December 2024
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(3, 2, 2, 180, 'IN', '2024-12-05', 'Product Restock'),
(26, 2, 2, 80, 'IN', '2024-12-05', 'Product Restock'),
(4, 4, 3, 400, 'IN', '2024-12-08', 'Product Restock'),
(9, 1, NULL, 35, 'OUT', '2024-12-12', 'Sales - Amount: 875.00');

-- ===== 2025 TRANSACTIONS =====

-- January 2025
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(1, 3, 1, 75, 'IN', '2025-01-08', 'Product Restock'),
(2, 1, 1, 38, 'IN', '2025-01-08', 'Product Restock'),
(8, 1, 1, 30, 'IN', '2025-01-10', 'Product Restock'),
(10, 3, 1, 120, 'IN', '2025-01-10', 'Product Restock');

-- February 2025
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(1, 3, NULL, 35, 'OUT', '2025-02-05', 'Sales - Amount: 1750.00'),
(2, 1, NULL, 15, 'OUT', '2025-02-08', 'Sales - Amount: 3750.00'),
(6, 2, 1, 12, 'IN', '2025-02-10', 'Product Restock'),
(7, 2, 1, 25, 'IN', '2025-02-10', 'Product Restock');

-- March 2025
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(11, 2, 1, 18, 'IN', '2025-03-03', 'Product Restock'),
(12, 3, 1, 22, 'IN', '2025-03-03', 'Product Restock'),
(13, 3, 1, 12, 'IN', '2025-03-05', 'Product Restock'),
(3, 2, NULL, 45, 'OUT', '2025-03-08', 'Sales - Amount: 450.00');

-- April 2025
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(18, 3, 1, 35, 'IN', '2025-04-02', 'Product Restock'),
(19, 3, 1, 4, 'IN', '2025-04-02', 'Product Restock'),
(20, 3, 1, 18, 'IN', '2025-04-05', 'Product Restock'),
(28, 1, NULL, 10, 'OUT', '2025-04-10', 'Sales - Amount: 1000.00');

-- May 2025
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(29, 2, 1, 18, 'IN', '2025-05-03', 'Product Restock'),
(30, 3, 1, 25, 'IN', '2025-05-03', 'Product Restock'),
(4, 4, 3, 350, 'IN', '2025-05-05', 'Product Restock'),
(9, 1, NULL, 28, 'OUT', '2025-05-08', 'Sales - Amount: 700.00');

-- June 2025 - Product Returns
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(10, 3, NULL, 20, 'IN', '2025-06-03', 'Product Return: Cancelled order'),
(22, 4, NULL, 50, 'IN', '2025-06-05', 'Product Return: Over-ordered'),
(1, 3, 1, 90, 'IN', '2025-06-08', 'Product Restock'),
(2, 1, 1, 42, 'IN', '2025-06-10', 'Product Restock');

-- July 2025 - Supplier Returns
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(6, 2, 1, 3, 'OUT', '2025-07-03', 'Supplier Return: Expired products'),
(16, 2, 1, 5, 'OUT', '2025-07-05', 'Supplier Return: Damaged packaging'),
(3, 2, 2, 160, 'IN', '2025-07-08', 'Product Restock'),
(26, 2, 2, 90, 'IN', '2025-07-10', 'Product Restock');

-- August 2025
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(7, 2, 1, 28, 'IN', '2025-08-03', 'Product Restock'),
(11, 2, 1, 16, 'IN', '2025-08-03', 'Product Restock'),
(12, 3, NULL, 15, 'OUT', '2025-08-08', 'Sales - Amount: 750.00'),
(13, 3, NULL, 8, 'OUT', '2025-08-10', 'Sales - Amount: 400.00');

-- September 2025
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(14, 3, 1, 32, 'IN', '2025-09-03', 'Product Restock'),
(15, 3, 1, 42, 'IN', '2025-09-03', 'Product Restock'),
(8, 1, 1, 28, 'IN', '2025-09-05', 'Product Restock'),
(9, 1, 1, 95, 'IN', '2025-09-05', 'Product Restock');

-- October 2025
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(17, 3, 1, 45, 'IN', '2025-10-03', 'Product Restock'),
(18, 3, 1, 38, 'IN', '2025-10-03', 'Product Restock'),
(1, 3, NULL, 40, 'OUT', '2025-10-08', 'Sales - Amount: 2000.00'),
(2, 1, NULL, 18, 'OUT', '2025-10-10', 'Sales - Amount: 4500.00');

-- November 2025 (Recent)
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(1, 3, 1, 85, 'IN', '2025-11-05', 'Product Restock'),
(2, 1, 1, 40, 'IN', '2025-11-05', 'Product Restock'),
(22, 4, 3, 280, 'IN', '2025-11-08', 'Product Restock'),
(23, 4, 3, 450, 'IN', '2025-11-08', 'Product Restock'),
(24, 4, 3, 450, 'IN', '2025-11-10', 'Product Restock'),
(25, 4, 3, 750, 'IN', '2025-11-10', 'Product Restock'),
(3, 2, NULL, 38, 'OUT', '2025-11-12', 'Sales - Amount: 380.00'),
(4, 4, NULL, 95, 'OUT', '2025-11-15', 'Sales - Amount: 475.00'),
(6, 2, NULL, 6, 'OUT', '2025-11-18', 'Sales - Amount: 1800.00');
