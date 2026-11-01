-- STUDY: seed one DRAFT and one ORDERED PO so the UI has data after migrate.

INSERT INTO purchase_orders (
    id, po_number, supplier_id, warehouse_id, status, ordered_at, expected_at, notes
) VALUES
    (
        'f0000000-0000-4000-8000-000000000001',
        'PO-2026-0001',
        'c0000000-0000-4000-8000-000000000001',
        'a0000000-0000-4000-8000-000000000001',
        'ORDERED',
        CURRENT_TIMESTAMP,
        CURRENT_DATE + 7,
        'Seed PO — receive ThinkPads into Central'
    ),
    (
        'f0000000-0000-4000-8000-000000000002',
        'PO-2026-0002',
        'c0000000-0000-4000-8000-000000000003',
        'a0000000-0000-4000-8000-000000000001',
        'DRAFT',
        NULL,
        CURRENT_DATE + 14,
        'Draft office supplies order'
    );

INSERT INTO purchase_order_lines (
    id, purchase_order_id, item_id, quantity_ordered, quantity_received, unit_cost
) VALUES
    (
        'f1000000-0000-4000-8000-000000000001',
        'f0000000-0000-4000-8000-000000000001',
        'd0000000-0000-4000-8000-000000000001',
        10, 0, 820.00
    ),
    (
        'f1000000-0000-4000-8000-000000000002',
        'f0000000-0000-4000-8000-000000000001',
        'd0000000-0000-4000-8000-000000000004',
        50, 0, 90.00
    ),
    (
        'f1000000-0000-4000-8000-000000000003',
        'f0000000-0000-4000-8000-000000000002',
        'd0000000-0000-4000-8000-000000000007',
        100, 0, 5.00
    );
