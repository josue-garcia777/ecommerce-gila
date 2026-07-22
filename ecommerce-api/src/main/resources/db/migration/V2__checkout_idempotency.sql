ALTER TABLE customer_orders ADD COLUMN idempotency_key VARCHAR(128);

UPDATE customer_orders
SET idempotency_key = 'legacy-' || CAST(id AS VARCHAR(36));

ALTER TABLE customer_orders ALTER COLUMN idempotency_key SET NOT NULL;
ALTER TABLE customer_orders ALTER COLUMN payment_reference DROP NOT NULL;
ALTER TABLE customer_orders ADD CONSTRAINT uk_orders_idempotency_key UNIQUE (idempotency_key);
