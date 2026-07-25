ALTER TABLE user_accounts
    ALTER COLUMN shipping_country_code SET DATA TYPE VARCHAR(2);

ALTER TABLE customer_orders
    ALTER COLUMN shipping_country_code SET DATA TYPE VARCHAR(2);
