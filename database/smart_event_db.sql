-- =====================================================================
-- SMART EVENT ENTRY SYSTEM - MySQL script
-- WARNING: the first line DELETES any old smart_event_db. Run it once
-- for a fresh start (it also re-inserts the test data).
-- =====================================================================
DROP DATABASE IF EXISTS smart_event_db;
CREATE DATABASE smart_event_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_event_db;

-- 1. ADMINS ------------------------------------------------------------
CREATE TABLE admins (
    admin_id      INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash CHAR(64)     NOT NULL,          -- SHA-256 hex text
    full_name     VARCHAR(100) NOT NULL
);

-- 2. EVENTS ------------------------------------------------------------
CREATE TABLE events (
    event_id         INT AUTO_INCREMENT PRIMARY KEY,
    event_name       VARCHAR(150) NOT NULL,
    event_date       DATE         NOT NULL,
    event_time       TIME         NOT NULL,
    venue            VARCHAR(150) NOT NULL,
    max_participants INT          NOT NULL CHECK (max_participants > 0)
);

-- 3. PARTICIPANTS ------------------------------------------------------
CREATE TABLE participants (
    participant_id INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(120) NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    event_id       INT          NOT NULL,
    registered_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_participant_event FOREIGN KEY (event_id)
        REFERENCES events(event_id) ON DELETE CASCADE,
    CONSTRAINT uq_email_per_event UNIQUE (email, event_id)
);

-- 4. TICKETS (one ticket per participant) ------------------------------
CREATE TABLE tickets (
    ticket_id      INT AUTO_INCREMENT PRIMARY KEY,
    ticket_code    VARCHAR(30) NOT NULL UNIQUE,   -- this text is inside the QR code
    participant_id INT         NOT NULL UNIQUE,
    issued_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_participant FOREIGN KEY (participant_id)
        REFERENCES participants(participant_id) ON DELETE CASCADE
);

-- 5. ATTENDANCE (UNIQUE ticket_id = a ticket can enter only once) ------
CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_id     INT         NOT NULL UNIQUE,
    entry_time    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status        VARCHAR(20) NOT NULL DEFAULT 'Present',
    CONSTRAINT fk_attendance_ticket FOREIGN KEY (ticket_id)
        REFERENCES tickets(ticket_id) ON DELETE CASCADE
);

-- ===================== TEST DATA =====================================
-- Login:  username = admin     password = admin123
-- (stored as SHA-256 here; the app upgrades it to BCrypt automatically on first login)
INSERT INTO admins (username, password_hash, full_name)
VALUES ('admin', SHA2('admin123', 256), 'System Administrator');

INSERT INTO events (event_name, event_date, event_time, venue, max_participants) VALUES
('Tech Fest 2026',      '2026-11-15', '10:00:00', 'Main Auditorium',   100),
('Cultural Night 2026', '2026-12-05', '18:30:00', 'Open Air Theatre',   50);

INSERT INTO participants (name, email, phone, event_id) VALUES
('Rahul Sharma', 'rahul.sharma@example.com', '9876543210', 1),
('Priya Patel',  'priya.patel@example.com',  '9123456780', 1),
('Amit Kumar',   'amit.kumar@example.com',   '9988776655', 2),
('Sneha Reddy',  'sneha.reddy@example.com',  '9012345678', 2);

-- Ready-made tickets you can type into the verification screen
INSERT INTO tickets (ticket_code, participant_id) VALUES
('TKT-TEST0001', 1),
('TKT-TEST0002', 2),
('TKT-TEST0003', 3),
('TKT-TEST0004', 4);
