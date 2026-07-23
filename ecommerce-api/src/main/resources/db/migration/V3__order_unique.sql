ALTER TABLE customer_orders
DROP CONSTRAINT uk_orders_idempotency_key;

ALTER TABLE customer_orders
    ADD CONSTRAINT uk_orders_user_idempotency_key
        UNIQUE (user_id, idempotency_key);