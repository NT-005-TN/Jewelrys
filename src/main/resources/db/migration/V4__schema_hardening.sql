UPDATE users SET email_verified = FALSE WHERE email_verified IS NULL;
ALTER TABLE users ALTER COLUMN email_verified SET NOT NULL;

UPDATE products SET is_available = (in_stock > 0) WHERE is_available IS NULL;
ALTER TABLE products ALTER COLUMN is_available SET NOT NULL;
ALTER TABLE products ALTER COLUMN is_available SET DEFAULT FALSE;

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'DELIVERED', 'CANCELLED'));

CREATE UNIQUE INDEX uk_users_email_normalized ON users (LOWER(email));
CREATE UNIQUE INDEX uk_clients_phone_nonnull ON clients (phone) WHERE phone IS NOT NULL;

CREATE INDEX idx_clients_user_id ON clients (user_id);
CREATE INDEX idx_employees_user_id ON employees (user_id);
CREATE INDEX idx_products_catalog ON products (is_available, type, price);
CREATE INDEX idx_product_materials_product_id ON product_materials (product_id);
CREATE INDEX idx_product_materials_material_id ON product_materials (material_id);
CREATE INDEX idx_orders_client_datetime ON orders (client_id, order_datetime DESC);
CREATE INDEX idx_orders_status_datetime ON orders (status, order_datetime DESC);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
CREATE INDEX idx_audit_log_created_at ON audit_log (created_at DESC);
CREATE INDEX idx_audit_log_record ON audit_log (table_name, record_id);
