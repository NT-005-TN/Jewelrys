-- Таблица всех пользователей
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    role VARCHAR(10) NOT NULL CHECK (role IN ('CLIENT', 'SELLER', 'ADMIN')),
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             email_verified BOOLEAN DEFAULT FALSE,
                             verification_token VARCHAR(255),
    verification_token_expires_at TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_token_expires_at TIMESTAMP
    );
COMMENT ON TABLE users IS 'Все пользователи в системе ювелирной мастерской';

-- Таблица клиентов
CREATE TABLE IF NOT EXISTS clients (
                                       id BIGSERIAL PRIMARY KEY,
                                       user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    patronymic VARCHAR(50),
    phone VARCHAR(20),
    is_permanent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                                                                                               );
COMMENT ON TABLE clients IS 'Клиенты ювелирной мастерской';

-- Таблица сотрудников
CREATE TABLE IF NOT EXISTS employees (
                                         id BIGSERIAL PRIMARY KEY,
                                         user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    position VARCHAR(50),
    department VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                                                                                                 );
COMMENT ON TABLE employees IS 'Сотрудники ювелирной мастерской';

-- Таблица материалов
CREATE TABLE IF NOT EXISTS materials (
                                         id BIGSERIAL PRIMARY KEY,
                                         name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );
COMMENT ON TABLE materials IS 'Материалы для изготовления ювелирных изделий';

-- Таблица изделий
CREATE TABLE IF NOT EXISTS products (
                                        id BIGSERIAL PRIMARY KEY,
                                        name VARCHAR(100) NOT NULL,
    description TEXT,
    sku VARCHAR(50) UNIQUE,
    weight DECIMAL(8,3) NOT NULL CHECK (weight > 0),
    price DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    type VARCHAR(20) NOT NULL,
    in_stock INTEGER DEFAULT 0 CHECK (in_stock >= 0),
    is_available BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );
COMMENT ON TABLE products IS 'Ювелирные изделия';

-- Связь изделий с материалами
CREATE TABLE IF NOT EXISTS product_materials (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    material_id BIGINT NOT NULL REFERENCES materials(id) ON DELETE CASCADE,
    quantity DECIMAL(8,3) NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                                                                                        UNIQUE(product_id, material_id)
    );
COMMENT ON TABLE product_materials IS 'Связь изделий с материалами';

-- Последовательность для номера заказа
CREATE SEQUENCE IF NOT EXISTS order_number_seq START 1000000;

-- Таблица заказов
CREATE TABLE IF NOT EXISTS orders (
                                      id BIGSERIAL PRIMARY KEY,
                                      order_number BIGINT NOT NULL UNIQUE DEFAULT nextval('order_number_seq'),
    client_id BIGINT NOT NULL REFERENCES clients(id),
    status VARCHAR(20) DEFAULT 'PENDING',
    total_amount DECIMAL(12,2) CHECK (total_amount >= 0),
    discount_amount DECIMAL(12,2) DEFAULT 0 CHECK (discount_amount >= 0),
    final_amount DECIMAL(12,2) CHECK (final_amount >= 0),
    notes TEXT,
    order_datetime TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             CHECK (final_amount = total_amount - discount_amount)
    );
COMMENT ON TABLE orders IS 'Заказы клиентов';

-- Позиции заказа
CREATE TABLE IF NOT EXISTS order_items (
                                           id BIGSERIAL PRIMARY KEY,
                                           order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(12,2) NOT NULL CHECK (unit_price >= 0),
    total_price DECIMAL(12,2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                                                                              CHECK (total_price = unit_price * quantity)
    );
COMMENT ON TABLE order_items IS 'Позиции в заказе';

-- Таблица аудита (оставляем, но без триггеров)
CREATE TABLE IF NOT EXISTS audit_log (
                                         id BIGSERIAL PRIMARY KEY,
                                         user_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(20) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE')),
    table_name VARCHAR(50) NOT NULL,
    record_id BIGINT NOT NULL,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );
COMMENT ON TABLE audit_log IS 'Аудит и логи';