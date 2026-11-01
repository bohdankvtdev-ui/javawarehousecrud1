CREATE TABLE warehouses (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255),
    city VARCHAR(100),
    capacity INTEGER CHECK (capacity >= 0),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);