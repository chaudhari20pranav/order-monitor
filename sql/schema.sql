-- ============================================================
-- ORDER MONITOR – PostgreSQL Schema
-- Run this on your NeonDB instance before first launch.
-- (Hibernate will also auto-create tables via ddl-auto=update,
--  but this script gives you full control and comments.)
-- ============================================================

-- ── Users ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL CHECK (role IN ('ADMIN','SUBSCRIBER')),
    last_active TIMESTAMP    DEFAULT NOW(),
    created_at  TIMESTAMP    DEFAULT NOW()
);

-- ── Orders ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT        NOT NULL REFERENCES users(id),
    product_name   VARCHAR(100)  NOT NULL,
    category       VARCHAR(50)   NOT NULL,
    quantity       INT           NOT NULL CHECK (quantity > 0),
    price          DECIMAL(15,2) NOT NULL CHECK (price > 0),
    payment_status VARCHAR(50)   NOT NULL DEFAULT 'PENDING'
                                         CHECK (payment_status IN ('PENDING','COMPLETED')),
    order_status   VARCHAR(50)   NOT NULL DEFAULT 'PLACED'
                                         CHECK (order_status IN ('PLACED','PAID','SHIPPED','DELIVERED','CANCELLED')),
    placed_at      TIMESTAMP     DEFAULT NOW(),
    shipped_at     TIMESTAMP,
    delivered_at   TIMESTAMP,
    updated_at     TIMESTAMP     DEFAULT NOW()
);

-- ── Notifications ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS notifications (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT  REFERENCES users(id),
    message    TEXT    NOT NULL,
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ── Order Events (audit trail) ───────────────────────────────
CREATE TABLE IF NOT EXISTS order_events (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT      NOT NULL REFERENCES orders(id),
    event_type VARCHAR(50) NOT NULL,
    message    TEXT,
    created_at TIMESTAMP   DEFAULT NOW()
);

-- ── Indexes for common query patterns ────────────────────────
CREATE INDEX IF NOT EXISTS idx_orders_user_id      ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status       ON orders(order_status);
CREATE INDEX IF NOT EXISTS idx_orders_placed_at    ON orders(placed_at);
CREATE INDEX IF NOT EXISTS idx_notifs_user_id      ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_events_order_id     ON order_events(order_id);
CREATE INDEX IF NOT EXISTS idx_users_email         ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_last_active   ON users(last_active);

-- ── Optional: seed a default admin account ───────────────────
-- Password below is BCrypt hash of: admin123
-- Change this before production!
INSERT INTO users (full_name, email, password, role)
VALUES (
    'Platform Admin',
    'admin@ordermonitor.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ADMIN'
) ON CONFLICT (email) DO NOTHING;
