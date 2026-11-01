-- =============================================================================
-- STUDY: Expand demo data ~6× so search / pagination / indexes are meaningful.
-- Uses generate_series (Postgres) — readable and easy to tune the COUNT.
-- Extra warehouses/suppliers/categories + ~60 catalog items + stock rows.
-- =============================================================================

-- ----- +2 warehouses (total 4) -----
INSERT INTO warehouses (id, name, address, city, capacity) VALUES
    ('a0000000-0000-4000-8000-000000000003', 'Southern Hub', '8 Port Road', 'Krakow', 8000),
    ('a0000000-0000-4000-8000-000000000004', 'Western Depot', '22 Freight Blvd', 'Wroclaw', 9000);

-- ----- +3 categories -----
INSERT INTO categories (id, name, description) VALUES
    ('b0000000-0000-4000-8000-000000000006', 'Networking', 'Switches, routers, cables'),
    ('b0000000-0000-4000-8000-000000000007', 'Storage', 'SSDs, NAS, USB drives'),
    ('b0000000-0000-4000-8000-000000000008', 'Safety', 'PPE, first-aid, signage');

-- ----- +4 suppliers -----
INSERT INTO suppliers (id, name, email, phone) VALUES
    ('c0000000-0000-4000-8000-000000000005', 'NetCable PL', 'sales@netcable.pl', '+48221000005'),
    ('c0000000-0000-4000-8000-000000000006', 'DiskWorld', 'hello@diskworld.eu', '+48221000006'),
    ('c0000000-0000-4000-8000-000000000007', 'SafeWork Co', 'orders@safework.eu', '+48221000007'),
    ('c0000000-0000-4000-8000-000000000008', 'BulkOffice', 'bulk@office.eu', '+48221000008');

-- ----- ~60 extra catalog items (ids d…010 … d…04b) -----
-- STUDY: procedural seed — change `generate_series(1, 60)` to grow/shrink demo size.
INSERT INTO warehouse_items (
    id, item_name, type, description, category_id, supplier_id, price, weight, status
)
SELECT
    ('d0000000-0000-4000-8000-' || lpad(to_hex(15 + g), 12, '0'))::uuid,
    CASE (g % 8)
        WHEN 0 THEN 'Laptop Bag Pro ' || g
        WHEN 1 THEN 'USB Hub 4-Port ' || g
        WHEN 2 THEN 'Cat6 Patch Cable ' || g || 'm'
        WHEN 3 THEN 'NVMe SSD ' || (128 + (g % 8) * 128) || 'GB'
        WHEN 4 THEN 'Wireless Headset ' || g
        WHEN 5 THEN 'Safety Gloves Pack ' || g
        WHEN 6 THEN 'Label Printer Tape ' || g
        ELSE 'Warehouse Bin Box ' || g
    END,
    CASE (g % 8)
        WHEN 0 THEN 'Bag'
        WHEN 1 THEN 'Hub'
        WHEN 2 THEN 'Cable'
        WHEN 3 THEN 'SSD'
        WHEN 4 THEN 'Audio'
        WHEN 5 THEN 'PPE'
        WHEN 6 THEN 'Consumable'
        ELSE 'Storage'
    END,
    'Bulk seed item #' || g || ' for search/pagination demos',
    CASE (g % 8)
        WHEN 0 THEN 'b0000000-0000-4000-8000-000000000002'::uuid
        WHEN 1 THEN 'b0000000-0000-4000-8000-000000000002'::uuid
        WHEN 2 THEN 'b0000000-0000-4000-8000-000000000006'::uuid
        WHEN 3 THEN 'b0000000-0000-4000-8000-000000000007'::uuid
        WHEN 4 THEN 'b0000000-0000-4000-8000-000000000001'::uuid
        WHEN 5 THEN 'b0000000-0000-4000-8000-000000000008'::uuid
        WHEN 6 THEN 'b0000000-0000-4000-8000-000000000003'::uuid
        ELSE 'b0000000-0000-4000-8000-000000000004'::uuid
    END,
    CASE (g % 4)
        WHEN 0 THEN 'c0000000-0000-4000-8000-000000000005'::uuid
        WHEN 1 THEN 'c0000000-0000-4000-8000-000000000006'::uuid
        WHEN 2 THEN 'c0000000-0000-4000-8000-000000000007'::uuid
        ELSE 'c0000000-0000-4000-8000-000000000008'::uuid
    END,
    round((10 + (g % 50) * 7.35)::numeric, 2),
    round((0.1 + (g % 20) * 0.15)::numeric, 2),
    CASE WHEN g % 17 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END
FROM generate_series(1, 60) AS g;

-- ----- Stock: every new item in Central + half in Northern/Southern/Western -----
INSERT INTO inventory (
    id, warehouse_id, item_id, quantity, min_quantity, location, warehouse_section, version, reserved_quantity
)
SELECT
    ('e0000000-0000-4000-8000-' || lpad(to_hex(0x200 + g), 12, '0'))::uuid,
    'a0000000-0000-4000-8000-000000000001'::uuid,
    ('d0000000-0000-4000-8000-' || lpad(to_hex(15 + g), 12, '0'))::uuid,
    20 + (g % 40),
    5 + (g % 10),
    'R' || lpad((10 + g % 20)::text, 2, '0') || '-S' || (1 + g % 4),
    chr(65 + (g % 3)),
    0,
    0
FROM generate_series(1, 60) AS g;

INSERT INTO inventory (
    id, warehouse_id, item_id, quantity, min_quantity, location, warehouse_section, version, reserved_quantity
)
SELECT
    ('e0000000-0000-4000-8000-' || lpad(to_hex(0x300 + g), 12, '0'))::uuid,
    CASE (g % 3)
        WHEN 0 THEN 'a0000000-0000-4000-8000-000000000002'::uuid
        WHEN 1 THEN 'a0000000-0000-4000-8000-000000000003'::uuid
        ELSE 'a0000000-0000-4000-8000-000000000004'::uuid
    END,
    ('d0000000-0000-4000-8000-' || lpad(to_hex(15 + g), 12, '0'))::uuid,
    CASE WHEN g % 11 = 0 THEN 2 ELSE 12 + (g % 25) END, -- some low stock
    8,
    'X' || (g % 9) || '-Y' || (g % 5),
    chr(65 + (g % 3)),
    0,
    0
FROM generate_series(1, 60) AS g
WHERE g % 2 = 0;

-- Extra POs for CRM demos
INSERT INTO purchase_orders (
    id, po_number, supplier_id, warehouse_id, status, ordered_at, expected_at, notes
) VALUES
    (
        'f0000000-0000-4000-8000-000000000003',
        'PO-2026-0010',
        'c0000000-0000-4000-8000-000000000005',
        'a0000000-0000-4000-8000-000000000003',
        'ORDERED',
        CURRENT_TIMESTAMP,
        CURRENT_DATE + 5,
        'Networking restock for Southern Hub'
    ),
    (
        'f0000000-0000-4000-8000-000000000004',
        'PO-2026-0011',
        'c0000000-0000-4000-8000-000000000006',
        'a0000000-0000-4000-8000-000000000004',
        'DRAFT',
        NULL,
        CURRENT_DATE + 10,
        'SSD bulk draft'
    );

INSERT INTO purchase_order_lines (
    id, purchase_order_id, item_id, quantity_ordered, quantity_received, unit_cost
) VALUES
    (
        'f1000000-0000-4000-8000-000000000010',
        'f0000000-0000-4000-8000-000000000003',
        'd0000000-0000-4000-8000-000000000012', -- seed item g=1 → hex 10
        40, 0, 12.50
    ),
    (
        'f1000000-0000-4000-8000-000000000011',
        'f0000000-0000-4000-8000-000000000004',
        'd0000000-0000-4000-8000-000000000013',
        25, 0, 89.00
    );
