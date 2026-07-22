CREATE TABLE products (
    id UUID PRIMARY KEY,
    sku VARCHAR(64) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    category VARCHAR(100) NOT NULL,
    price_amount NUMERIC(19,2) NOT NULL CHECK (price_amount >= 0),
    price_currency CHAR(3) NOT NULL,
    stock INTEGER NOT NULL CHECK (stock >= 0),
    weight_kg NUMERIC(10,3) NOT NULL CHECK (weight_kg >= 0),
    image_url VARCHAR(2048),
    active BOOLEAN NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_products_sku UNIQUE (sku)
);

CREATE INDEX idx_products_search_order ON products (name, id);
CREATE INDEX idx_products_category ON products (category);

CREATE TABLE product_imports (
    id UUID PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    file_content BYTEA,
    status VARCHAR(32) NOT NULL,
    created_count INTEGER NOT NULL CHECK (created_count >= 0),
    updated_count INTEGER NOT NULL CHECK (updated_count >= 0),
    rejected_count INTEGER NOT NULL CHECK (rejected_count >= 0),
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE product_import_errors (
    id UUID PRIMARY KEY,
    import_id UUID NOT NULL,
    row_number INTEGER NOT NULL CHECK (row_number > 0),
    sku VARCHAR(64),
    reason VARCHAR(1000) NOT NULL,
    CONSTRAINT fk_import_errors_import FOREIGN KEY (import_id) REFERENCES product_imports(id),
    CONSTRAINT uk_import_errors_row UNIQUE (import_id, row_number)
);

CREATE INDEX idx_import_errors_import ON product_import_errors (import_id, row_number);

CREATE TABLE carts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_carts_user_status ON carts (user_id, status);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id),
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uk_cart_items_product UNIQUE (cart_id, product_id)
);

CREATE INDEX idx_cart_items_cart ON cart_items (cart_id);

CREATE TABLE customer_orders (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_amount NUMERIC(19,2) NOT NULL CHECK (total_amount >= 0),
    total_currency CHAR(3) NOT NULL,
    payment_reference VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_orders_cart UNIQUE (cart_id),
    CONSTRAINT fk_orders_cart FOREIGN KEY (cart_id) REFERENCES carts(id)
);

CREATE INDEX idx_orders_user_created ON customer_orders (user_id, created_at);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    sku VARCHAR(64) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price_amount NUMERIC(19,2) NOT NULL CHECK (unit_price_amount >= 0),
    unit_price_currency CHAR(3) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    line_total_amount NUMERIC(19,2) NOT NULL CHECK (line_total_amount >= 0),
    line_total_currency CHAR(3) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES customer_orders(id)
);

CREATE INDEX idx_order_items_order ON order_items (order_id);
