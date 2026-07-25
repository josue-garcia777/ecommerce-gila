ALTER TABLE user_accounts DROP COLUMN shipping_recipient_name;
ALTER TABLE user_accounts RENAME COLUMN shipping_line1 TO address_line1;
ALTER TABLE user_accounts RENAME COLUMN shipping_line2 TO address_line2;
ALTER TABLE user_accounts RENAME COLUMN shipping_city TO address_city;
ALTER TABLE user_accounts RENAME COLUMN shipping_state_or_province TO address_state;
ALTER TABLE user_accounts RENAME COLUMN shipping_postal_code TO address_postal_code;
ALTER TABLE user_accounts RENAME COLUMN shipping_country_code TO address_country_code;

ALTER TABLE customer_orders DROP COLUMN shipping_recipient_name;
ALTER TABLE customer_orders RENAME COLUMN shipping_line1 TO address_line1;
ALTER TABLE customer_orders RENAME COLUMN shipping_line2 TO address_line2;
ALTER TABLE customer_orders RENAME COLUMN shipping_city TO address_city;
ALTER TABLE customer_orders RENAME COLUMN shipping_state_or_province TO address_state;
ALTER TABLE customer_orders RENAME COLUMN shipping_postal_code TO address_postal_code;
ALTER TABLE customer_orders RENAME COLUMN shipping_country_code TO address_country_code;
