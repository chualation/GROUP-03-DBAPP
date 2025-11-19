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
    supplier_id INT,
	location_id INT,
    description TEXT,
    category VARCHAR(50), -- Ingredient, Beverage, Packaging, Kitchen Supply
    unit_of_measure VARCHAR(50), -- kg, liters, pcs, packs
    reorder_level DECIMAL(10,2) DEFAULT 0,
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

-- Product
INSERT INTO Product (product_name, description, category, unit_of_measure, reorder_level, product_status)
VALUES
('Rice', 'Long grain rice', 'Ingredient', 'kg', 10, 'Active'),
('Chicken', 'Fresh chicken meat', 'Ingredient', 'kg', 5, 'Active'),
('Soda Can', 'Carbonated beverage', 'Beverage', 'pcs', 20, 'Active'),
('Disposable Box', 'Food container', 'Packaging', 'pcs', 50, 'Active'),
('Olive Oil', 'Extra virgin olive oil', 'Ingredient', 'liters', 5, 'Active'),
('Cheese', 'Mozzarella cheese', 'Ingredient', 'kg', 3, 'Active'),
('Tomatoes', 'Fresh red tomatoes', 'Ingredient', 'kg', 8, 'Active'),
('Ground Pork', 'Lean ground pork meat', 'Ingredient', 'kg', 4, 'Active'),
('Beef Patty', 'Frozen beef patty', 'Ingredient', 'pcs', 30, 'Active'),
('Burger Buns', 'Freshly baked burger buns', 'Ingredient', 'pcs', 40, 'Active'),
('Lettuce', 'Crisp iceberg lettuce', 'Ingredient', 'kg', 3, 'Active'),
('Onions', 'White onions', 'Ingredient', 'kg', 5, 'Active'),
('Garlic', 'Fresh garlic bulbs', 'Ingredient', 'kg', 2, 'Active'),
('Spaghetti Pasta', 'Dry pasta noodles', 'Ingredient', 'kg', 6, 'Active'),
('Tomato Sauce', 'Canned tomato sauce', 'Ingredient', 'cans', 15, 'Active'),
('Milk', 'Evaporated milk', 'Ingredient', 'cans', 12, 'Active'),
('Sugar', 'White refined sugar', 'Ingredient', 'kg', 10, 'Active'),
('Salt', 'Iodized salt', 'Ingredient', 'kg', 8, 'Active'),
('Black Pepper', 'Ground black pepper', 'Ingredient', 'g', 200, 'Active'),
('Soy Sauce', 'Dark soy sauce', 'Ingredient', 'bottles', 6, 'Active'),
('Vinegar', 'Cane vinegar', 'Ingredient', 'bottles', 6, 'Active'),
('Paper Cups', '12 oz disposable cups', 'Packaging', 'pcs', 100, 'Active'),
('Plastic Spoons', 'Disposable spoons', 'Packaging', 'pcs', 200, 'Active'),
('Plastic Forks', 'Disposable forks', 'Packaging', 'pcs', 200, 'Active'),
('Napkins', 'Paper napkins', 'Packaging', 'pcs', 300, 'Active'),
('Bottled Water', 'Mineral water 500ml', 'Beverage', 'bottles', 40, 'Active'),
('Orange Juice', 'Fresh carton orange juice', 'Beverage', 'liters', 10, 'Active'),
('Frozen Fries', 'Shoestring french fries', 'Ingredient', 'kg', 7, 'Active'),
('Butter', 'Salted butter', 'Ingredient', 'kg', 4, 'Active'),
('Ketchup', 'Tomato ketchup', 'Ingredient', 'bottles', 10, 'Active');

-- Supplier
INSERT INTO Supplier (supplier_name, contact_person, contact_number, email, address, supplier_status)
VALUES
('Fresh Farms', 'John Doe', '09123456789', 'john@freshfarms.com', '123 Farm St., Manila', 'Active'),
('Beverage Supplies Inc.', 'Jane Smith', '09987654321', 'jane@bevsupply.com', '456 Drink Rd., Makati', 'Active'),
('Packaging World', 'Alice Tan', '09234567890', 'alice@packworld.com', '789 Packaging Ave., Quezon City', 'Active');

-- StockMovement (IN)
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(1, 3, 1, 50, 'IN', '2025-11-12', 'Initial rice stock'),
(2, 1, 1, 20, 'IN', '2025-11-12', 'Fresh chicken delivery'),
(3, 2, 2, 100, 'IN', '2025-11-12', 'Soda cans restock'),
(4, 4, 3, 200, 'IN', '2025-11-12', 'Packaging supply'),
(5, 2, 1, 10, 'IN', '2025-11-12', 'Olive oil delivery'),
(6, 2, 1, 5, 'IN', '2025-11-12', 'Cheese delivery');

-- StockMovement (OUT)
INSERT INTO StockMovement (product_id, location_id, supplier_id, quantity, movement_type, movement_date, reason)
VALUES
(1, 3, NULL, 50, 'OUT', '2025-11-13', 'Used for prep'),
(2, 1, NULL, 19	, 'OUT', '2025-11-13', 'Used for prep'),
(3, 2, NULL, 10, 'OUT', '2025-11-13', 'Served to customers'),
(2, 1, NULL, 0.5, 'OUT', '2025-11-13', 'Spoilage'),
(5, 2, NULL, 1.2, 'OUT', '2025-11-13', 'Used in recipe'),
(6, 2, NULL, 0.8, 'OUT', '2025-11-13', 'Used in recipe');

SELECT 
    p.product_id,
    p.product_name,
    p.category,
    p.unit_of_measure,
    l.location_name,
    SUM(CASE WHEN sm.movement_type = 'IN' THEN sm.quantity ELSE 0 END) -
    SUM(CASE WHEN sm.movement_type = 'OUT' THEN sm.quantity ELSE 0 END) AS current_stock,
    p.reorder_level,
    CASE 
        WHEN SUM(CASE WHEN sm.movement_type = 'IN' THEN sm.quantity ELSE 0 END) -
             SUM(CASE WHEN sm.movement_type = 'OUT' THEN sm.quantity ELSE 0 END) <= p.reorder_level
        THEN '⚠ Reorder Needed'
        ELSE 'OK'
    END AS stock_status
FROM Product p
JOIN StockMovement sm ON p.product_id = sm.product_id
JOIN StorageLocation l ON sm.location_id = l.location_id
GROUP BY p.product_id, l.location_id
ORDER BY p.category, p.product_name, l.location_name;
