CREATE TYPE reservation_status AS ENUM ('ACTIVE', 'CONFIRMED', 'EXPIRED', 'CANCELLED');

CREATE TABLE IF NOT EXISTS reservations (
        id BIGSERIAL PRIMARY KEY,
        quantity INTEGER NOT NULL,
        status reservation_status NOT NULL DEFAULT 'ACTIVE',
        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
        expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
        product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT
);

CREATE INDEX idx_reservations_product_id ON reservations(product_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_created_at ON reservations(created_at);
CREATE INDEX idx_reservations_expires_at ON reservations(expires_at);

CREATE INDEX idx_reservations_status_created_at ON reservations(status, created_at);