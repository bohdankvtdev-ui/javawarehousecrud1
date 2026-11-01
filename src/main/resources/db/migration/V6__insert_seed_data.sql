
INSERT INTO warehouses (name, address, city, capacity)
VALUES
('Central Warehouse', '12 Industrial Street', 'Warsaw', 15000),
('Northern Warehouse', '45 Logistics Avenue', 'Gdansk', 10000);


INSERT INTO categories (name, description)
VALUES
('Electronics', 'Electronic devices and components'),
('Computer Accessories', 'Computer and laptop accessories'),
('Office Supplies', 'Everyday office materials'),
('Furniture', 'Office and warehouse furniture'),
('Tools', 'Hand tools and equipment'),
('Packaging', 'Packaging and shipping materials'),
('Safety Equipment', 'Personal protective equipment'),
('Networking', 'Network devices and cables'),
('Lighting', 'Lighting equipment and accessories'),
('Cleaning Supplies', 'Cleaning products and equipment');


INSERT INTO suppliers (name, email, phone)
VALUES
('TechSource Europe', 'sales@techsource.eu', '+48221000001'),
('Global Electronics', 'contact@globalelectronics.eu', '+48221000002'),
('OfficePro', 'orders@officepro.eu', '+48221000003'),
('FurniTrade', 'sales@furnitrade.eu', '+48221000004'),
('ToolMaster', 'info@toolmaster.eu', '+48221000005'),
('PackLine', 'orders@packline.eu', '+48221000006'),
('SafeWork', 'contact@safework.eu', '+48221000007'),
('NetConnect', 'sales@netconnect.eu', '+48221000008'),
('BrightLight', 'info@brightlight.eu', '+48221000009'),
('CleanSupply', 'orders@cleansupply.eu', '+48221000010'),
('EuroTech Distribution', 'sales@eurotech.eu', '+48221000011'),
('Universal Wholesale', 'contact@universalwholesale.eu', '+48221000012');



WITH selected_inventory AS (
    SELECT
        w.id AS warehouse_id,
        i.id AS item_id,
        w.name AS warehouse_name,
        i.item_name,
        ROW_NUMBER() OVER (
            PARTITION BY w.id
            ORDER BY i.item_name
        ) AS row_num
    FROM warehouses w
    CROSS JOIN warehouse_items i
    WHERE
        w.name = 'Central Warehouse'
        OR (
            w.name = 'Northern Warehouse'
            AND i.item_name NOT IN (
                'LED Panel 60x60',
                'Floor Cleaning Liquid 5L'
            )
        )
)
INSERT INTO inventory (
    warehouse_id,
    item_id,
    quantity,
    min_quantity,
    location,
    warehouse_section
)
SELECT
    warehouse_id,
    item_id,

    CASE
        WHEN row_num % 9 = 0 THEN 3
        ELSE 20 + (row_num * 13) % 180
    END,

    10 + (row_num * 7) % 25,

    'R' || LPAD(row_num::TEXT, 2, '0') ||
    '-S' || LPAD((row_num % 10 + 1)::TEXT, 2, '0'),

    CASE
        WHEN row_num % 3 = 0 THEN 'A'
        WHEN row_num % 3 = 1 THEN 'B'
        ELSE 'C'
    END

FROM selected_inventory;


INSERT INTO warehouse_items (
    item_name,
    type,
    category_id,
    description,
    price,
    weight,
    supplier_id,
    status
)
SELECT
    p.item_name,
    p.type,
    c.id,
    p.description,
    p.price,
    p.weight,
    s.id,
    'ACTIVE'
FROM (
    VALUES
    ('Lenovo ThinkPad E14', 'Laptop', 'Business laptop 14 inch', 850.00, 1.65, 'Electronics', 'TechSource Europe'),
    ('Dell Latitude 5540', 'Laptop', 'Professional laptop 15 inch', 1100.00, 1.75, 'Electronics', 'Global Electronics'),
    ('Samsung Monitor 27', 'Monitor', '27 inch Full HD monitor', 220.00, 4.50, 'Electronics', 'Global Electronics'),
    ('LG UltraWide Monitor', 'Monitor', '34 inch ultrawide monitor', 480.00, 6.80, 'Electronics', 'EuroTech Distribution'),
    ('Logitech MX Master 3S', 'Mouse', 'Wireless productivity mouse', 95.00, 0.14, 'Computer Accessories', 'TechSource Europe'),
    ('Mechanical Keyboard RGB', 'Keyboard', 'Mechanical gaming keyboard', 110.00, 0.95, 'Computer Accessories', 'Global Electronics'),
    ('USB-C Hub 7-in-1', 'Adapter', 'Multiport USB-C adapter', 35.00, 0.12, 'Computer Accessories', 'TechSource Europe'),
    ('USB-C Docking Station', 'Dock', 'Professional docking station', 180.00, 0.65, 'Computer Accessories', 'EuroTech Distribution'),
    ('A4 Copy Paper 500 Sheets', 'Paper', 'Standard office paper', 5.50, 2.50, 'Office Supplies', 'OfficePro'),
    ('Blue Ballpoint Pens 50 Pack', 'Pens', 'Blue ink ballpoint pens', 8.00, 0.40, 'Office Supplies', 'Universal Wholesale'),
    ('Permanent Markers 12 Pack', 'Markers', 'Black permanent markers', 6.50, 0.25, 'Office Supplies', 'OfficePro'),
    ('Ergonomic Office Chair', 'Chair', 'Adjustable ergonomic chair', 210.00, 15.00, 'Furniture', 'FurniTrade'),
    ('Standing Office Desk', 'Desk', 'Height adjustable desk', 450.00, 28.00, 'Furniture', 'FurniTrade'),
    ('Metal Storage Cabinet', 'Cabinet', 'Lockable metal cabinet', 180.00, 35.00, 'Furniture', 'Universal Wholesale'),
    ('Cordless Drill 18V', 'Drill', 'Professional cordless drill', 130.00, 2.10, 'Tools', 'ToolMaster'),
    ('Electric Screwdriver Set', 'Screwdriver', 'Rechargeable screwdriver kit', 65.00, 0.90, 'Tools', 'ToolMaster'),
    ('Adjustable Wrench 12 Inch', 'Wrench', 'Heavy duty adjustable wrench', 22.00, 0.70, 'Tools', 'Universal Wholesale'),
    ('Cardboard Box Medium', 'Box', 'Standard shipping box medium', 1.20, 0.35, 'Packaging', 'PackLine'),
    ('Cardboard Box Large', 'Box', 'Large shipping cardboard box', 2.00, 0.60, 'Packaging', 'PackLine'),
    ('Bubble Wrap Roll 50m', 'Wrap', 'Protective bubble packaging roll', 18.00, 1.50, 'Packaging', 'Universal Wholesale'),
    ('Protective Safety Helmet', 'Helmet', 'Industrial protective helmet', 25.00, 0.45, 'Safety Equipment', 'SafeWork'),
    ('Safety Gloves Large', 'Gloves', 'Protective work gloves', 9.00, 0.20, 'Safety Equipment', 'SafeWork'),
    ('Cat6 Ethernet Cable 10m', 'Cable', 'Network cable Cat6', 8.00, 0.30, 'Networking', 'NetConnect'),
    ('TP-Link Gigabit Switch 8 Port', 'Switch', '8 port gigabit network switch', 35.00, 0.50, 'Networking', 'NetConnect'),
    ('LED Panel 60x60', 'Panel', 'Office ceiling LED panel', 38.00, 2.40, 'Lighting', 'BrightLight'),
    ('Floor Cleaning Liquid 5L', 'Detergent', 'Industrial floor cleaning liquid', 16.00, 5.20, 'Cleaning Supplies', 'CleanSupply')
) AS p(
    item_name,
    type,
    description,
    price,
    weight,
    category_name,
    supplier_name
)
JOIN categories c ON c.name = p.category_name
JOIN suppliers s ON s.name = p.supplier_name;
