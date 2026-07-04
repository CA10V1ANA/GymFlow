-- Suppliers
CREATE TABLE suppliers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150)        NOT NULL,
    document        VARCHAR(20),
    phone           VARCHAR(30),
    email           VARCHAR(180),
    address         VARCHAR(255),
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now()
);

-- Product categories
CREATE TABLE product_categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100)        NOT NULL UNIQUE
);

-- Products (supplements, accessories)
CREATE TABLE products (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150)        NOT NULL,
    description     TEXT,
    category_id     UUID                REFERENCES product_categories (id),
    supplier_id     UUID                REFERENCES suppliers (id),
    sku             VARCHAR(50)         NOT NULL UNIQUE,
    cost_price      NUMERIC(10,2)       NOT NULL,
    sale_price      NUMERIC(10,2)       NOT NULL,
    stock_quantity  INTEGER             NOT NULL DEFAULT 0,
    min_stock       INTEGER             NOT NULL DEFAULT 0,
    active          BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_name ON products (name);

-- Stock movements (entry/exit) & sales
CREATE TABLE stock_movements (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID                NOT NULL REFERENCES products (id),
    type            VARCHAR(10)         NOT NULL, -- ENTRY, EXIT
    quantity        INTEGER             NOT NULL,
    unit_price      NUMERIC(10,2)       NOT NULL,
    reason          VARCHAR(100)        NOT NULL, -- PURCHASE, SALE, ADJUSTMENT, LOSS
    student_id      UUID                REFERENCES students (id) ON DELETE SET NULL,
    created_by      UUID                REFERENCES users (id) ON DELETE SET NULL,
    created_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_movements_product_id ON stock_movements (product_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements (created_at);
