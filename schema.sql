-- ============================================
-- Cinema Management System - Database Schema
-- Bishkek Cinemas Project
-- PostgreSQL 14+
-- ============================================

-- Enable UUID extension for generating unique identifiers
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- BLOCK 1: CONTENT (Movies & Genres)
-- ============================================

CREATE TABLE GENRES (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE MOVIES (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    age_rating VARCHAR(10) NOT NULL CHECK (age_rating IN ('0+', '6+', '12+', '16+', '18+')),
    duration_min INTEGER NOT NULL CHECK (duration_min > 0),
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'coming_soon' CHECK (status IN ('in_theaters', 'coming_soon', 'archived')),
    release_date DATE NOT NULL,
    country VARCHAR(100),
    director VARCHAR(200),
    imdb_rating DECIMAL(3,1) CHECK (imdb_rating >= 0 AND imdb_rating <= 10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE MOVIE_GENRES (
    movie_id INTEGER NOT NULL REFERENCES MOVIES(id) ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES GENRES(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

-- ============================================
-- BLOCK 2: INFRASTRUCTURE (Cinemas, Halls, Seats)
-- ============================================

CREATE TABLE CINEMAS (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(300) NOT NULL,
    city VARCHAR(100) NOT NULL DEFAULT 'Bishkek',
    phone VARCHAR(20),
    email VARCHAR(150),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE HALLS (
    id SERIAL PRIMARY KEY,
    cinema_id INTEGER NOT NULL REFERENCES CINEMAS(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('2D', '3D', 'IMAX')),
    total_seats INTEGER NOT NULL CHECK (total_seats > 0),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(cinema_id, name)
);

CREATE TABLE SEATS (
    id SERIAL PRIMARY KEY,
    hall_id INTEGER NOT NULL REFERENCES HALLS(id) ON DELETE CASCADE,
    row_number INTEGER NOT NULL CHECK (row_number > 0),
    seat_number INTEGER NOT NULL CHECK (seat_number > 0),
    seat_type VARCHAR(20) NOT NULL CHECK (seat_type IN ('standard', 'vip', 'couch')),
    pos_x FLOAT,
    pos_y FLOAT,
    is_active BOOLEAN DEFAULT true,
    UNIQUE(hall_id, row_number, seat_number)
);

-- ============================================
-- BLOCK 3: SCHEDULE (Showtimes & Prices)
-- ============================================

CREATE TABLE SHOWTIMES (
    id SERIAL PRIMARY KEY,
    movie_id INTEGER NOT NULL REFERENCES MOVIES(id) ON DELETE CASCADE,
    hall_id INTEGER NOT NULL REFERENCES HALLS(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    language VARCHAR(20) NOT NULL CHECK (language IN ('ru', 'ky', 'en', 'original')),
    subtitles VARCHAR(20) NOT NULL DEFAULT 'none' CHECK (subtitles IN ('none', 'ru', 'ky', 'en')),
    base_price DECIMAL(10,2) NOT NULL CHECK (base_price >= 0),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_showtime_times CHECK (end_time > start_time),
    CONSTRAINT check_future_showtime CHECK (start_time >= CURRENT_TIMESTAMP - INTERVAL '1 day')
);

CREATE TABLE PRICES (
    id SERIAL PRIMARY KEY,
    showtime_id INTEGER NOT NULL REFERENCES SHOWTIMES(id) ON DELETE CASCADE,
    seat_type VARCHAR(20) NOT NULL CHECK (seat_type IN ('standard', 'vip', 'couch')),
    price_modifier DECIMAL(10,2) NOT NULL DEFAULT 0,
    UNIQUE(showtime_id, seat_type)
);

-- ============================================
-- BLOCK 4: USERS & LOYALTY
-- ============================================

CREATE TABLE USERS (
    id SERIAL PRIMARY KEY,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    birth_date DATE,
    avatar_url VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'MANAGER', 'ADMIN')),
    email_verified BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE LOYALTY_ACCOUNTS (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES USERS(id) ON DELETE CASCADE,
    ltv DECIMAL(12,2) DEFAULT 0 CHECK (ltv >= 0),
    discount_pct FLOAT DEFAULT 0 CHECK (discount_pct >= 0 AND discount_pct <= 50),
    bonus_balance DECIMAL(10,2) DEFAULT 0 CHECK (bonus_balance >= 0),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE LOYALTY_TRANSACTIONS (
    id SERIAL PRIMARY KEY,
    loyalty_account_id INTEGER NOT NULL REFERENCES LOYALTY_ACCOUNTS(id) ON DELETE CASCADE,
    order_id INTEGER REFERENCES ORDERS(id) ON DELETE SET NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('accrual', 'redemption')),
    amount DECIMAL(10,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- BLOCK 5: SALES (Orders, Payments, Tickets)
-- ============================================

CREATE TABLE PROMOCODES (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(20) NOT NULL CHECK (discount_type IN ('percent', 'fixed')),
    discount_value DECIMAL(10,2) NOT NULL CHECK (discount_value > 0),
    max_uses INTEGER CHECK (max_uses > 0),
    used_count INTEGER DEFAULT 0 CHECK (used_count >= 0),
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_by INTEGER REFERENCES USERS(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_promocode_dates CHECK (valid_until > valid_from),
    CONSTRAINT check_promocode_uses CHECK (used_count <= max_uses OR max_uses IS NULL)
);

CREATE TABLE ORDERS (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES USERS(id) ON DELETE CASCADE,
    promocode_id INTEGER REFERENCES PROMOCODES(id) ON DELETE SET NULL,
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    discount_amount DECIMAL(10,2) DEFAULT 0 CHECK (discount_amount >= 0),
    bonus_used DECIMAL(10,2) DEFAULT 0 CHECK (bonus_used >= 0),
    final_amount DECIMAL(10,2) GENERATED ALWAYS AS (total_amount - discount_amount - bonus_used) STORED,
    payment_method VARCHAR(50) CHECK (payment_method IN ('card', 'cash', 'odengi', 'elsom', 'bonus')),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (payment_status IN ('pending', 'paid', 'cancelled', 'refunded')),
    refund_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE PAYMENT_TRANSACTIONS (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES ORDERS(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL CHECK (provider IN ('stripe', 'odengi', 'elsom', 'cash')),
    bank_transaction_id VARCHAR(100),
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('pending', 'success', 'failed', 'refunded')),
    error_code VARCHAR(50),
    error_message TEXT,
    raw_response JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TICKETS (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES ORDERS(id) ON DELETE CASCADE,
    showtime_id INTEGER NOT NULL REFERENCES SHOWTIMES(id) ON DELETE CASCADE,
    seat_id INTEGER NOT NULL REFERENCES SEATS(id) ON DELETE CASCADE,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'reserved' CHECK (status IN ('free', 'reserved', 'sold', 'used', 'cancelled')),
    qr_code VARCHAR(255) UNIQUE NOT NULL,
    scanned_at TIMESTAMP,
    scanned_by INTEGER REFERENCES USERS(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_seat_showtime UNIQUE(showtime_id, seat_id)
);

-- ============================================
-- BLOCK 6: ANALYTICS (Cart, Logs, Notifications)
-- ============================================

CREATE TABLE CART_SESSIONS (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES USERS(id) ON DELETE CASCADE,
    showtime_id INTEGER NOT NULL REFERENCES SHOWTIMES(id) ON DELETE CASCADE,
    seat_id INTEGER NOT NULL REFERENCES SEATS(id) ON DELETE CASCADE,
    session_token VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'purchased', 'expired')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_active_seat_reservation UNIQUE(showtime_id, seat_id, status)
);

CREATE TABLE ACTION_LOGS (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES USERS(id) ON DELETE SET NULL,
    event_type VARCHAR(50) NOT NULL,
    payload JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE NOTIFICATIONS (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES USERS(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL CHECK (type IN ('order_confirmed', 'ticket_ready', 'promo', 'reminder', 'refund')),
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('email', 'sms', 'push')),
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    is_sent BOOLEAN DEFAULT false,
    sent_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

-- Foreign key indexes
CREATE INDEX idx_movie_genres_movie ON MOVIE_GENRES(movie_id);
CREATE INDEX idx_movie_genres_genre ON MOVIE_GENRES(genre_id);
CREATE INDEX idx_halls_cinema ON HALLS(cinema_id);
CREATE INDEX idx_seats_hall ON SEATS(hall_id);
CREATE INDEX idx_showtimes_movie ON SHOWTIMES(movie_id);
CREATE INDEX idx_showtimes_hall ON SHOWTIMES(hall_id);
CREATE INDEX idx_prices_showtime ON PRICES(showtime_id);
CREATE INDEX idx_loyalty_accounts_user ON LOYALTY_ACCOUNTS(user_id);
CREATE INDEX idx_loyalty_transactions_account ON LOYALTY_TRANSACTIONS(loyalty_account_id);
CREATE INDEX idx_loyalty_transactions_order ON LOYALTY_TRANSACTIONS(order_id);
CREATE INDEX idx_orders_user ON ORDERS(user_id);
CREATE INDEX idx_orders_promocode ON ORDERS(promocode_id);
CREATE INDEX idx_payment_transactions_order ON PAYMENT_TRANSACTIONS(order_id);
CREATE INDEX idx_tickets_order ON TICKETS(order_id);
CREATE INDEX idx_tickets_showtime ON TICKETS(showtime_id);
CREATE INDEX idx_tickets_seat ON TICKETS(seat_id);
CREATE INDEX idx_cart_sessions_user ON CART_SESSIONS(user_id);
CREATE INDEX idx_cart_sessions_showtime ON CART_SESSIONS(showtime_id);
CREATE INDEX idx_action_logs_user ON ACTION_LOGS(user_id);
CREATE INDEX idx_notifications_user ON NOTIFICATIONS(user_id);

-- Query optimization indexes
CREATE INDEX idx_movies_status ON MOVIES(status, release_date);
CREATE INDEX idx_showtimes_active_time ON SHOWTIMES(is_active, start_time) WHERE is_active = true;
CREATE INDEX idx_showtimes_date ON SHOWTIMES(start_time);
CREATE INDEX idx_orders_status ON ORDERS(payment_status, created_at);
CREATE INDEX idx_tickets_status ON TICKETS(status);
CREATE INDEX idx_cart_expires ON CART_SESSIONS(expires_at, status) WHERE status = 'active';
CREATE INDEX idx_promocodes_code_active ON PROMOCODES(code) WHERE is_active = true;
CREATE INDEX idx_action_logs_event_time ON ACTION_LOGS(event_type, created_at);
CREATE INDEX idx_notifications_pending ON NOTIFICATIONS(is_sent, created_at) WHERE is_sent = false;

-- Full-text search indexes (for movie search)
CREATE INDEX idx_movies_title_trgm ON MOVIES USING gin(title gin_trgm_ops);
CREATE INDEX idx_movies_description_trgm ON MOVIES USING gin(description gin_trgm_ops);

-- ============================================
-- TRIGGERS
-- ============================================

-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_movies_updated_at BEFORE UPDATE ON MOVIES
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cinemas_updated_at BEFORE UPDATE ON CINEMAS
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON USERS
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON ORDERS
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Auto-update loyalty LTV when order is paid
CREATE OR REPLACE FUNCTION update_loyalty_ltv()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.payment_status = 'paid' AND OLD.payment_status != 'paid' THEN
        UPDATE LOYALTY_ACCOUNTS
        SET ltv = ltv + NEW.final_amount,
            updated_at = CURRENT_TIMESTAMP
        WHERE user_id = NEW.user_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_loyalty_ltv AFTER UPDATE ON ORDERS
    FOR EACH ROW EXECUTE FUNCTION update_loyalty_ltv();

-- Auto-increment promocode usage
CREATE OR REPLACE FUNCTION increment_promocode_usage()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.payment_status = 'paid' AND OLD.payment_status != 'paid' AND NEW.promocode_id IS NOT NULL THEN
        UPDATE PROMOCODES
        SET used_count = used_count + 1
        WHERE id = NEW.promocode_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_increment_promocode AFTER UPDATE ON ORDERS
    FOR EACH ROW EXECUTE FUNCTION increment_promocode_usage();

-- Generate QR code on ticket creation
CREATE OR REPLACE FUNCTION generate_ticket_qr()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.qr_code IS NULL OR NEW.qr_code = '' THEN
        NEW.qr_code = 'TICKET-' || NEW.id || '-' || uuid_generate_v4();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_generate_qr BEFORE INSERT ON TICKETS
    FOR EACH ROW EXECUTE FUNCTION generate_ticket_qr();

-- Prevent booking past showtimes
CREATE OR REPLACE FUNCTION check_showtime_not_past()
RETURNS TRIGGER AS $$
DECLARE
    showtime_start TIMESTAMP;
BEGIN
    SELECT start_time INTO showtime_start
    FROM SHOWTIMES
    WHERE id = NEW.showtime_id;

    IF showtime_start < CURRENT_TIMESTAMP THEN
        RAISE EXCEPTION 'Cannot book tickets for past showtimes';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_past_showtime BEFORE INSERT ON TICKETS
    FOR EACH ROW EXECUTE FUNCTION check_showtime_not_past();

-- ============================================
-- VIEWS FOR COMMON QUERIES
-- ============================================

-- Available seats for a showtime
CREATE OR REPLACE VIEW available_seats_view AS
SELECT
    s.id as seat_id,
    s.hall_id,
    s.row_number,
    s.seat_number,
    s.seat_type,
    st.id as showtime_id,
    st.start_time,
    CASE
        WHEN t.id IS NOT NULL THEN false
        WHEN cs.id IS NOT NULL AND cs.expires_at > CURRENT_TIMESTAMP THEN false
        ELSE true
    END as is_available
FROM SEATS s
CROSS JOIN SHOWTIMES st
LEFT JOIN TICKETS t ON t.seat_id = s.id AND t.showtime_id = st.id AND t.status IN ('reserved', 'sold', 'used')
LEFT JOIN CART_SESSIONS cs ON cs.seat_id = s.id AND cs.showtime_id = st.id AND cs.status = 'active'
WHERE s.is_active = true AND st.is_active = true;

-- Revenue by cinema
CREATE OR REPLACE VIEW revenue_by_cinema AS
SELECT
    c.id as cinema_id,
    c.name as cinema_name,
    c.city,
    COUNT(DISTINCT o.id) as total_orders,
    SUM(o.final_amount) as total_revenue,
    DATE_TRUNC('month', o.paid_at) as month
FROM CINEMAS c
JOIN HALLS h ON h.cinema_id = c.id
JOIN SHOWTIMES st ON st.hall_id = h.id
JOIN TICKETS t ON t.showtime_id = st.id
JOIN ORDERS o ON o.id = t.order_id
WHERE o.payment_status = 'paid'
GROUP BY c.id, c.name, c.city, DATE_TRUNC('month', o.paid_at);

-- Popular movies
CREATE OR REPLACE VIEW popular_movies AS
SELECT
    m.id,
    m.title,
    m.poster_url,
    m.imdb_rating,
    COUNT(DISTINCT t.id) as tickets_sold,
    SUM(t.price) as total_revenue,
    AVG(t.price) as avg_ticket_price
FROM MOVIES m
JOIN SHOWTIMES st ON st.movie_id = m.id
JOIN TICKETS t ON t.showtime_id = st.id
JOIN ORDERS o ON o.id = t.order_id
WHERE o.payment_status = 'paid'
GROUP BY m.id, m.title, m.poster_url, m.imdb_rating
ORDER BY tickets_sold DESC;

-- ============================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================

COMMENT ON TABLE MOVIES IS 'Movie catalog with metadata';
COMMENT ON TABLE GENRES IS 'Genre reference table';
COMMENT ON TABLE MOVIE_GENRES IS 'Many-to-many relationship between movies and genres';
COMMENT ON TABLE CINEMAS IS 'Physical cinema locations in Bishkek';
COMMENT ON TABLE HALLS IS 'Screening halls within cinemas';
COMMENT ON TABLE SEATS IS 'Individual seats with coordinates for interactive seat maps';
COMMENT ON TABLE SHOWTIMES IS 'Movie screening schedule with language/subtitle options';
COMMENT ON TABLE PRICES IS 'Price modifiers by seat type for each showtime';
COMMENT ON TABLE USERS IS 'User accounts with role-based access';
COMMENT ON TABLE LOYALTY_ACCOUNTS IS 'Loyalty program accounts tracking LTV and bonuses';
COMMENT ON TABLE LOYALTY_TRANSACTIONS IS 'History of loyalty point accruals and redemptions';
COMMENT ON TABLE PROMOCODES IS 'Promotional discount codes';
COMMENT ON TABLE ORDERS IS 'Customer orders with payment tracking';
COMMENT ON TABLE PAYMENT_TRANSACTIONS IS 'Payment gateway transaction logs';
COMMENT ON TABLE TICKETS IS 'Individual tickets with QR codes for validation';
COMMENT ON TABLE CART_SESSIONS IS 'Temporary seat reservations during checkout';
COMMENT ON TABLE ACTION_LOGS IS 'User behavior tracking for analytics';
COMMENT ON TABLE NOTIFICATIONS IS 'Email/SMS/Push notification queue';

COMMENT ON COLUMN SEATS.pos_x IS 'X coordinate for interactive seat map rendering';
COMMENT ON COLUMN SEATS.pos_y IS 'Y coordinate for interactive seat map rendering';
COMMENT ON COLUMN LOYALTY_ACCOUNTS.ltv IS 'Lifetime value - total amount spent by user';
COMMENT ON COLUMN LOYALTY_ACCOUNTS.discount_pct IS 'Current loyalty discount percentage (0-50%)';
COMMENT ON COLUMN ORDERS.final_amount IS 'Computed: total_amount - discount_amount - bonus_used';
COMMENT ON COLUMN TICKETS.qr_code IS 'Unique QR code for ticket validation at entrance';
COMMENT ON COLUMN PAYMENT_TRANSACTIONS.raw_response IS 'Full JSON response from payment provider for debugging';
