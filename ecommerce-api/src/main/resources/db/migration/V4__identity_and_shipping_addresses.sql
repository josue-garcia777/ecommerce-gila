CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL,
    shipping_recipient_name VARCHAR(200),
    shipping_line1 VARCHAR(200),
    shipping_line2 VARCHAR(200),
    shipping_city VARCHAR(120),
    shipping_state_or_province VARCHAR(120),
    shipping_postal_code VARCHAR(32),
    shipping_country_code CHAR(2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_user_accounts_email UNIQUE (email)
);

CREATE TABLE user_roles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES user_accounts(id),
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role),
    CONSTRAINT ck_user_roles_role CHECK (role IN ('CUSTOMER', 'ADMIN'))
);

CREATE INDEX idx_user_roles_user ON user_roles (user_id);

ALTER TABLE carts
    ADD CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES user_accounts(id);

ALTER TABLE customer_orders
    ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES user_accounts(id);

ALTER TABLE customer_orders
    ADD COLUMN shipping_recipient_name VARCHAR(200) NOT NULL;

ALTER TABLE customer_orders
    ADD COLUMN shipping_line1 VARCHAR(200) NOT NULL;

ALTER TABLE customer_orders
    ADD COLUMN shipping_line2 VARCHAR(200);

ALTER TABLE customer_orders
    ADD COLUMN shipping_city VARCHAR(120) NOT NULL;

ALTER TABLE customer_orders
    ADD COLUMN shipping_state_or_province VARCHAR(120);

ALTER TABLE customer_orders
    ADD COLUMN shipping_postal_code VARCHAR(32) NOT NULL;

ALTER TABLE customer_orders
    ADD COLUMN shipping_country_code CHAR(2) NOT NULL;
