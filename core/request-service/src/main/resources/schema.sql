-- Создание схемы, если ещё не существует
CREATE SCHEMA IF NOT EXISTS request;

-- Таблица статусов заявок
CREATE TABLE IF NOT EXISTS request.request_statuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL
);

-- Таблица заявок на участие
CREATE TABLE IF NOT EXISTS request.participation_requests (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status_id INT NOT NULL REFERENCES request.request_statuses(id),
    created TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_request UNIQUE (requester_id, event_id)
);
CREATE INDEX IF NOT EXISTS idx_requests_requester_id ON request.participation_requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_requests_event_id ON request.participation_requests(event_id);
CREATE INDEX IF NOT EXISTS idx_requests_status_id ON request.participation_requests(status_id);
CREATE INDEX IF NOT EXISTS idx_requests_created ON request.participation_requests(created);

-- Первоначальные статусы
INSERT INTO request.request_statuses (name) VALUES
('PENDING'),
('CONFIRMED'),
('REJECTED'),
('CANCELED')
ON CONFLICT (name) DO NOTHING;
