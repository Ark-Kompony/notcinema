-- ============================================
-- Cinema Management System - Sample Data
-- Bishkek Cinemas Project
-- ============================================

-- Enable pg_trgm extension for full-text search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============================================
-- GENRES
-- ============================================

INSERT INTO GENRES (name) VALUES
('Action'),
('Comedy'),
('Drama'),
('Horror'),
('Sci-Fi'),
('Romance'),
('Thriller'),
('Animation'),
('Documentary'),
('Fantasy'),
('Crime'),
('Adventure');

-- ============================================
-- MOVIES
-- ============================================

INSERT INTO MOVIES (title, description, age_rating, duration_min, poster_url, trailer_url, status, release_date, country, director, imdb_rating) VALUES
('Dune: Part Three', 'The epic conclusion to the Dune saga. Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.', '12+', 166, 'https://example.com/dune3.jpg', 'https://youtube.com/dune3', 'in_theaters', '2026-03-15', 'USA', 'Denis Villeneuve', 8.9),
('The Last Nomad', 'A touching story about a Kyrgyz family preserving their nomadic traditions in modern times.', '6+', 118, 'https://example.com/nomad.jpg', 'https://youtube.com/nomad', 'in_theaters', '2026-04-20', 'Kyrgyzstan', 'Aktan Arym Kubat', 7.8),
('Cosmic Horizon', 'A thrilling space adventure where humanity discovers an ancient alien civilization.', '12+', 142, 'https://example.com/cosmic.jpg', 'https://youtube.com/cosmic', 'in_theaters', '2026-05-01', 'USA', 'Christopher Nolan', 8.5),
('Bishkek Nights', 'A romantic comedy set in modern Bishkek, following three friends navigating love and career.', '16+', 105, 'https://example.com/bishkek.jpg', 'https://youtube.com/bishkek', 'in_theaters', '2026-05-10', 'Kyrgyzstan', 'Mirlan Abdykalykov', 7.2),
('The Silk Road Mystery', 'An archaeological thriller uncovering secrets along the ancient Silk Road.', '12+', 128, 'https://example.com/silkroad.jpg', 'https://youtube.com/silkroad', 'coming_soon', '2026-06-15', 'China/Kyrgyzstan', 'Zhang Yimou', 7.9),
('Laugh Out Loud', 'A hilarious comedy about a struggling comedian who accidentally becomes famous.', '12+', 98, 'https://example.com/lol.jpg', 'https://youtube.com/lol', 'in_theaters', '2026-05-05', 'USA', 'Judd Apatow', 7.4),
('Mountain Spirit', 'An animated adventure featuring Kyrgyz folklore and mythology.', '0+', 92, 'https://example.com/mountain.jpg', 'https://youtube.com/mountain', 'in_theaters', '2026-04-25', 'Kyrgyzstan', 'Bektur Ryskul', 8.1),
('Dark Waters', 'A psychological horror film about a group trapped in an abandoned Soviet facility.', '18+', 110, 'https://example.com/dark.jpg', 'https://youtube.com/dark', 'in_theaters', '2026-05-12', 'Russia', 'Andrei Zvyagintsev', 7.6);

-- ============================================
-- MOVIE_GENRES
-- ============================================

INSERT INTO MOVIE_GENRES (movie_id, genre_id) VALUES
(1, 5), (1, 12), (1, 3),  -- Dune: Sci-Fi, Adventure, Drama
(2, 3), (2, 9),            -- Last Nomad: Drama, Documentary
(3, 5), (3, 12), (3, 7),   -- Cosmic: Sci-Fi, Adventure, Thriller
(4, 6), (4, 2),            -- Bishkek Nights: Romance, Comedy
(5, 7), (5, 12), (5, 11),  -- Silk Road: Thriller, Adventure, Crime
(6, 2),                    -- Laugh Out Loud: Comedy
(7, 8), (7, 12), (7, 10),  -- Mountain Spirit: Animation, Adventure, Fantasy
(8, 4), (8, 7);            -- Dark Waters: Horror, Thriller

-- ============================================
-- CINEMAS (Real Bishkek locations)
-- ============================================

INSERT INTO CINEMAS (name, address, city, phone, email, is_active) VALUES
('Vefa Center Cinema', 'Chui Avenue 157, Vefa Center', 'Bishkek', '+996 312 123456', 'info@vefacinema.kg', true),
('Asia Mall Cinema', 'Ibraimov Street 115, Asia Mall', 'Bishkek', '+996 312 234567', 'contact@asiamall.kg', true),
('Ala-Too Cinema', 'Erkindik Boulevard 21', 'Bishkek', '+996 312 345678', 'info@alatoo-cinema.kg', true),
('Dordoi Plaza Cinema', 'Manas Avenue 49, Dordoi Plaza', 'Bishkek', '+996 312 456789', 'cinema@dordoiplaza.kg', true);

-- ============================================
-- HALLS
-- ============================================

INSERT INTO HALLS (cinema_id, name, type, total_seats, is_active) VALUES
-- Vefa Center (3 halls)
(1, 'Hall 1 - IMAX', 'IMAX', 150, true),
(1, 'Hall 2 - 3D', '3D', 120, true),
(1, 'Hall 3 - Standard', '2D', 100, true),
-- Asia Mall (2 halls)
(2, 'Premium Hall', '3D', 80, true),
(2, 'Standard Hall', '2D', 90, true),
-- Ala-Too (2 halls)
(3, 'VIP Hall', '2D', 60, true),
(3, 'Main Hall', '2D', 110, true),
-- Dordoi Plaza (2 halls)
(4, 'Hall A', '3D', 100, true),
(4, 'Hall B', '2D', 85, true);

-- ============================================
-- SEATS (Sample for Hall 1 - IMAX at Vefa)
-- ============================================

DO $$
DECLARE
    hall_id_var INTEGER := 1;
    row_num INTEGER;
    seat_num INTEGER;
    seat_type_var VARCHAR(20);
BEGIN
    -- 10 rows, 15 seats per row = 150 seats
    FOR row_num IN 1..10 LOOP
        FOR seat_num IN 1..15 LOOP
            -- First 2 rows: standard
            -- Rows 3-7: standard
            -- Rows 8-10: VIP
            IF row_num >= 8 THEN
                seat_type_var := 'vip';
            ELSE
                seat_type_var := 'standard';
            END IF;

            INSERT INTO SEATS (hall_id, row_number, seat_number, seat_type, pos_x, pos_y, is_active)
            VALUES (hall_id_var, row_num, seat_num, seat_type_var, seat_num * 50.0, row_num * 60.0, true);
        END LOOP;
    END LOOP;
END $$;

-- Add seats for other halls (simplified - 10 rows x 10 seats)
DO $$
DECLARE
    hall_rec RECORD;
    row_num INTEGER;
    seat_num INTEGER;
BEGIN
    FOR hall_rec IN SELECT id FROM HALLS WHERE id > 1 LOOP
        FOR row_num IN 1..10 LOOP
            FOR seat_num IN 1..10 LOOP
                INSERT INTO SEATS (hall_id, row_number, seat_number, seat_type, pos_x, pos_y, is_active)
                VALUES (hall_rec.id, row_num, seat_num, 'standard', seat_num * 50.0, row_num * 60.0, true);
            END LOOP;
        END LOOP;
    END LOOP;
END $$;

-- ============================================
-- SHOWTIMES (Next 7 days)
-- ============================================

INSERT INTO SHOWTIMES (movie_id, hall_id, start_time, end_time, language, subtitles, base_price, is_active) VALUES
-- Today
(1, 1, '2026-05-25 10:00:00', '2026-05-25 12:46:00', 'original', 'ru', 350.00, true),
(1, 1, '2026-05-25 14:00:00', '2026-05-25 16:46:00', 'ru', 'none', 350.00, true),
(1, 1, '2026-05-25 18:00:00', '2026-05-25 20:46:00', 'original', 'ru', 400.00, true),
(3, 2, '2026-05-25 11:00:00', '2026-05-25 13:22:00', 'original', 'ru', 300.00, true),
(3, 2, '2026-05-25 15:00:00', '2026-05-25 17:22:00', 'ru', 'none', 300.00, true),
(4, 3, '2026-05-25 12:00:00', '2026-05-25 13:45:00', 'ru', 'none', 250.00, true),
(4, 3, '2026-05-25 16:00:00', '2026-05-25 17:45:00', 'ru', 'none', 250.00, true),
(6, 4, '2026-05-25 13:00:00', '2026-05-25 14:38:00', 'original', 'ru', 280.00, true),
(7, 5, '2026-05-25 10:30:00', '2026-05-25 12:02:00', 'ky', 'ru', 200.00, true),
(7, 5, '2026-05-25 14:30:00', '2026-05-25 16:02:00', 'ru', 'none', 200.00, true),
(8, 6, '2026-05-25 20:00:00', '2026-05-25 21:50:00', 'original', 'ru', 320.00, true),

-- Tomorrow
(1, 1, '2026-05-26 10:00:00', '2026-05-26 12:46:00', 'original', 'ru', 350.00, true),
(1, 1, '2026-05-26 18:00:00', '2026-05-26 20:46:00', 'ru', 'none', 400.00, true),
(2, 7, '2026-05-26 11:00:00', '2026-05-26 12:58:00', 'ky', 'ru', 280.00, true),
(2, 7, '2026-05-26 15:00:00', '2026-05-26 16:58:00', 'ru', 'ky', 280.00, true),
(3, 8, '2026-05-26 12:00:00', '2026-05-26 14:22:00', 'original', 'ru', 320.00, true),
(4, 9, '2026-05-26 14:00:00', '2026-05-26 15:45:00', 'ru', 'none', 240.00, true),
(6, 4, '2026-05-26 16:00:00', '2026-05-26 17:38:00', 'original', 'ru', 280.00, true),
(7, 5, '2026-05-26 10:00:00', '2026-05-26 11:32:00', 'ky', 'ru', 200.00, true),
(8, 6, '2026-05-26 21:00:00', '2026-05-26 22:50:00', 'original', 'ru', 320.00, true);

-- ============================================
-- PRICES (Seat type modifiers)
-- ============================================

DO $$
DECLARE
    showtime_rec RECORD;
BEGIN
    FOR showtime_rec IN SELECT id FROM SHOWTIMES LOOP
        INSERT INTO PRICES (showtime_id, seat_type, price_modifier) VALUES
        (showtime_rec.id, 'standard', 0.00),
        (showtime_rec.id, 'vip', 150.00),
        (showtime_rec.id, 'couch', 200.00);
    END LOOP;
END $$;

-- ============================================
-- USERS
-- ============================================

INSERT INTO USERS (phone, email, password_hash, first_name, last_name, birth_date, role, email_verified, is_active) VALUES
('+996555123456', 'admin@cinema.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', '1990-01-01', 'ADMIN', true, true),
('+996555234567', 'manager@cinema.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Manager', 'User', '1992-05-15', 'MANAGER', true, true),
('+996555345678', 'aibek@mail.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Aibek', 'Asanov', '1995-03-20', 'USER', true, true),
('+996555456789', 'ainura@mail.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ainura', 'Bekova', '1998-07-12', 'USER', true, true),
('+996555567890', 'nurlan@mail.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nurlan', 'Toktomushev', '1993-11-08', 'USER', true, true),
('+996555678901', 'gulnara@mail.kg', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Gulnara', 'Sydykova', '1997-02-25', 'USER', true, true);

-- Password for all test users: "password123"

-- ============================================
-- LOYALTY_ACCOUNTS
-- ============================================

INSERT INTO LOYALTY_ACCOUNTS (user_id, ltv, discount_pct, bonus_balance) VALUES
(3, 5000.00, 5.0, 250.00),
(4, 12000.00, 10.0, 600.00),
(5, 3000.00, 3.0, 150.00),
(6, 8000.00, 8.0, 400.00);

-- ============================================
-- PROMOCODES
-- ============================================

INSERT INTO PROMOCODES (code, discount_type, discount_value, max_uses, used_count, valid_from, valid_until, is_active, created_by) VALUES
('WELCOME2026', 'percent', 20.00, 100, 15, '2026-05-01 00:00:00', '2026-06-30 23:59:59', true, 1),
('SUMMER50', 'fixed', 50.00, 200, 45, '2026-05-15 00:00:00', '2026-08-31 23:59:59', true, 1),
('VIP100', 'fixed', 100.00, 50, 8, '2026-05-01 00:00:00', '2026-12-31 23:59:59', true, 1),
('STUDENT15', 'percent', 15.00, NULL, 120, '2026-01-01 00:00:00', '2026-12-31 23:59:59', true, 1),
('EXPIRED', 'percent', 30.00, 10, 10, '2026-04-01 00:00:00', '2026-04-30 23:59:59', false, 1);

-- ============================================
-- SAMPLE ORDERS & TICKETS
-- ============================================

-- Order 1: Aibek bought 2 tickets for Dune
INSERT INTO ORDERS (user_id, promocode_id, total_amount, discount_amount, bonus_used, payment_method, payment_status, created_at, paid_at)
VALUES (3, 1, 700.00, 140.00, 0.00, 'card', 'paid', '2026-05-20 14:30:00', '2026-05-20 14:32:00');

INSERT INTO TICKETS (order_id, showtime_id, seat_id, price, status, qr_code)
VALUES
(1, 1, 50, 350.00, 'sold', 'TICKET-1-' || uuid_generate_v4()),
(1, 1, 51, 350.00, 'sold', 'TICKET-2-' || uuid_generate_v4());

INSERT INTO PAYMENT_TRANSACTIONS (order_id, provider, bank_transaction_id, amount, status, raw_response, created_at)
VALUES (1, 'stripe', 'pi_3MtwBwLkdIwHu7ix28a3tqPa', 560.00, 'success', '{"id":"pi_3MtwBwLkdIwHu7ix28a3tqPa","status":"succeeded"}', '2026-05-20 14:32:00');

-- Order 2: Ainura bought 4 tickets for Cosmic Horizon
INSERT INTO ORDERS (user_id, promocode_id, total_amount, discount_amount, bonus_used, payment_method, payment_status, created_at, paid_at)
VALUES (4, 2, 1200.00, 50.00, 100.00, 'card', 'paid', '2026-05-21 10:15:00', '2026-05-21 10:17:00');

INSERT INTO TICKETS (order_id, showtime_id, seat_id, price, status, qr_code)
VALUES
(2, 4, 200, 300.00, 'sold', 'TICKET-3-' || uuid_generate_v4()),
(2, 4, 201, 300.00, 'sold', 'TICKET-4-' || uuid_generate_v4()),
(2, 4, 202, 300.00, 'sold', 'TICKET-5-' || uuid_generate_v4()),
(2, 4, 203, 300.00, 'sold', 'TICKET-6-' || uuid_generate_v4());

INSERT INTO PAYMENT_TRANSACTIONS (order_id, provider, bank_transaction_id, amount, status, raw_response, created_at)
VALUES (2, 'odengi', 'ODG-2026052110170001', 1050.00, 'success', '{"transaction_id":"ODG-2026052110170001","status":"completed"}', '2026-05-21 10:17:00');

-- Order 3: Pending order (cart not completed)
INSERT INTO ORDERS (user_id, promocode_id, total_amount, discount_amount, bonus_used, payment_method, payment_status, created_at)
VALUES (5, NULL, 500.00, 0.00, 0.00, 'card', 'pending', '2026-05-25 09:00:00');

INSERT INTO TICKETS (order_id, showtime_id, seat_id, price, status, qr_code)
VALUES
(3, 2, 55, 250.00, 'reserved', 'TICKET-7-' || uuid_generate_v4()),
(3, 2, 56, 250.00, 'reserved', 'TICKET-8-' || uuid_generate_v4());

-- ============================================
-- LOYALTY_TRANSACTIONS
-- ============================================

INSERT INTO LOYALTY_TRANSACTIONS (loyalty_account_id, order_id, type, amount, description, created_at)
VALUES
(1, 1, 'accrual', 28.00, 'Earned 5% cashback on order #1', '2026-05-20 14:32:00'),
(2, 2, 'redemption', -100.00, 'Used bonus points on order #2', '2026-05-21 10:17:00'),
(2, 2, 'accrual', 105.00, 'Earned 10% cashback on order #2', '2026-05-21 10:17:00');

-- ============================================
-- CART_SESSIONS (Active reservations)
-- ============================================

INSERT INTO CART_SESSIONS (user_id, showtime_id, seat_id, session_token, expires_at, status, created_at)
VALUES
(5, 2, 55, 'sess_' || uuid_generate_v4(), CURRENT_TIMESTAMP + INTERVAL '10 minutes', 'active', CURRENT_TIMESTAMP),
(5, 2, 56, 'sess_' || uuid_generate_v4(), CURRENT_TIMESTAMP + INTERVAL '10 minutes', 'active', CURRENT_TIMESTAMP);

-- ============================================
-- ACTION_LOGS (Sample user activity)
-- ============================================

INSERT INTO ACTION_LOGS (user_id, event_type, payload, ip_address, user_agent, created_at)
VALUES
(3, 'view_movie', '{"movie_id": 1, "movie_title": "Dune: Part Three"}', '192.168.1.100', 'Mozilla/5.0', '2026-05-20 14:25:00'),
(3, 'select_showtime', '{"showtime_id": 1, "start_time": "2026-05-25 10:00:00"}', '192.168.1.100', 'Mozilla/5.0', '2026-05-20 14:27:00'),
(3, 'select_seats', '{"seat_ids": [50, 51]}', '192.168.1.100', 'Mozilla/5.0', '2026-05-20 14:28:00'),
(3, 'apply_promocode', '{"code": "WELCOME2026", "discount": 140.00}', '192.168.1.100', 'Mozilla/5.0', '2026-05-20 14:29:00'),
(3, 'complete_payment', '{"order_id": 1, "amount": 560.00}', '192.168.1.100', 'Mozilla/5.0', '2026-05-20 14:32:00'),
(4, 'view_movie', '{"movie_id": 3, "movie_title": "Cosmic Horizon"}', '192.168.1.101', 'Mozilla/5.0', '2026-05-21 10:10:00'),
(4, 'complete_payment', '{"order_id": 2, "amount": 1050.00}', '192.168.1.101', 'Mozilla/5.0', '2026-05-21 10:17:00');

-- ============================================
-- NOTIFICATIONS
-- ============================================

INSERT INTO NOTIFICATIONS (user_id, type, channel, title, body, is_sent, sent_at, created_at)
VALUES
(3, 'order_confirmed', 'email', 'Order Confirmed', 'Your order #1 has been confirmed. Total: 560 KGS', true, '2026-05-20 14:32:30', '2026-05-20 14:32:00'),
(3, 'ticket_ready', 'sms', 'Tickets Ready', 'Your tickets for Dune: Part Three are ready. Show QR code at entrance.', true, '2026-05-20 14:33:00', '2026-05-20 14:32:00'),
(4, 'order_confirmed', 'email', 'Order Confirmed', 'Your order #2 has been confirmed. Total: 1050 KGS', true, '2026-05-21 10:17:30', '2026-05-21 10:17:00'),
(4, 'ticket_ready', 'push', 'Tickets Ready', 'Your tickets for Cosmic Horizon are ready!', true, '2026-05-21 10:18:00', '2026-05-21 10:17:00'),
(5, 'promo', 'email', 'Special Offer!', 'Use code SUMMER50 for 50 KGS off your next booking!', false, NULL, '2026-05-25 08:00:00');

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Check data counts
SELECT 'GENRES' as table_name, COUNT(*) as count FROM GENRES
UNION ALL SELECT 'MOVIES', COUNT(*) FROM MOVIES
UNION ALL SELECT 'CINEMAS', COUNT(*) FROM CINEMAS
UNION ALL SELECT 'HALLS', COUNT(*) FROM HALLS
UNION ALL SELECT 'SEATS', COUNT(*) FROM SEATS
UNION ALL SELECT 'SHOWTIMES', COUNT(*) FROM SHOWTIMES
UNION ALL SELECT 'USERS', COUNT(*) FROM USERS
UNION ALL SELECT 'ORDERS', COUNT(*) FROM ORDERS
UNION ALL SELECT 'TICKETS', COUNT(*) FROM TICKETS
UNION ALL SELECT 'PROMOCODES', COUNT(*) FROM PROMOCODES;

-- Show available seats for first showtime
SELECT * FROM available_seats_view WHERE showtime_id = 1 LIMIT 10;

-- Show revenue by cinema
SELECT * FROM revenue_by_cinema;

-- Show popular movies
SELECT * FROM popular_movies;
