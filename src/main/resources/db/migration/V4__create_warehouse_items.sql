CREATE TABLE warehouse_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_name VARCHAR(150) NOT NULL,
    description TEXT,

    category_id UUID REFERENCES categories(id),
    supplier_id UUID REFERENCES suppliers(id),

    price NUMERIC(10,2) CHECK (price >= 0),
    weight NUMERIC(10,2) CHECK (weight >= 0)
);