-- Создание схемы, если ещё не существует
CREATE SCHEMA IF NOT EXISTS explore;

-- Таблица категорий
CREATE TABLE IF NOT EXISTS explore.categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_categories_name ON explore.categories(name);

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS explore.users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_users_email ON explore.users(email);
CREATE INDEX IF NOT EXISTS idx_users_name ON explore.users(name);

-- Таблица локаций
CREATE TABLE IF NOT EXISTS explore.locations (
    id SERIAL PRIMARY KEY,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    UNIQUE (lat, lon)
);
CREATE INDEX IF NOT EXISTS idx_locations_lat_lon ON explore.locations(lat, lon);

-- Таблица событий
CREATE TABLE IF NOT EXISTS explore.events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    annotation TEXT NOT NULL,
    category_id BIGINT NOT NULL REFERENCES explore.categories(id),
    paid BOOLEAN NOT NULL,
    event_date TIMESTAMP NOT NULL,
    initiator_id BIGINT NOT NULL REFERENCES explore.users(id),
    description TEXT NOT NULL,
    participant_limit INT NOT NULL DEFAULT 0,
    state VARCHAR(255),
    created_on TIMESTAMP NOT NULL DEFAULT now(),
    location_id INT NOT NULL REFERENCES explore.locations(id),
    request_moderation BOOLEAN NOT NULL DEFAULT TRUE,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    confirmed_requests INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_events_category_id ON explore.events(category_id);
CREATE INDEX IF NOT EXISTS idx_events_event_date ON explore.events(event_date);
CREATE INDEX IF NOT EXISTS idx_events_initiator_id ON explore.events(initiator_id);
CREATE INDEX IF NOT EXISTS idx_events_state ON explore.events(state);
CREATE INDEX IF NOT EXISTS idx_events_location_id ON explore.events(location_id);
CREATE INDEX IF NOT EXISTS idx_events_paid ON explore.events(paid);

-- Таблица подборок
CREATE TABLE IF NOT EXISTS explore.compilations (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_compilations_pinned ON explore.compilations(pinned);

-- Таблица связи подборки и события
CREATE TABLE IF NOT EXISTS explore.compilation_events (
    compilation_id BIGINT NOT NULL REFERENCES explore.compilations(id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES explore.events(id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);
CREATE INDEX IF NOT EXISTS idx_compilation_events_compilation_id ON explore.compilation_events(compilation_id);
CREATE INDEX IF NOT EXISTS idx_compilation_events_event_id ON explore.compilation_events(event_id);

-- Таблица статусов заявок
CREATE TABLE IF NOT EXISTS explore.request_statuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL
);

-- Таблица заявок на участие
CREATE TABLE IF NOT EXISTS explore.participation_requests (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL REFERENCES explore.users(id),
    event_id BIGINT NOT NULL REFERENCES explore.events(id),
    status_id INT NOT NULL REFERENCES explore.request_statuses(id),
    created TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_request UNIQUE (requester_id, event_id)
);
CREATE INDEX IF NOT EXISTS idx_requests_requester_id ON explore.participation_requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_requests_event_id ON explore.participation_requests(event_id);
CREATE INDEX IF NOT EXISTS idx_requests_status_id ON explore.participation_requests(status_id);
CREATE INDEX IF NOT EXISTS idx_requests_created ON explore.participation_requests(created);

-- Первоначальные статусы
INSERT INTO explore.request_statuses (name) VALUES
('PENDING'),
('CONFIRMED'),
('REJECTED'),
('CANCELED')
ON CONFLICT (name) DO NOTHING;

-- Таблица комментариев
CREATE TABLE IF NOT EXISTS explore.comments (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(2000) NOT NULL,
    event_id BIGINT NOT NULL REFERENCES explore.events(id),
    author_id BIGINT NOT NULL REFERENCES explore.users(id),
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_comments_text ON explore.comments(text);
CREATE INDEX IF NOT EXISTS idx_comments_event_id ON explore.comments(event_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON explore.comments(author_id);
CREATE INDEX IF NOT EXISTS idx_comments_created ON explore.comments(created);