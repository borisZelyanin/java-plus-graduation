-- Создание схемы, если ещё не существует
CREATE SCHEMA IF NOT EXISTS event;

-- Таблица категорий
CREATE TABLE IF NOT EXISTS explore.categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_categories_name ON explore.categories(name);

-- Таблица локаций
CREATE TABLE IF NOT EXISTS event.locations (
    id SERIAL PRIMARY KEY,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    UNIQUE (lat, lon)
);
CREATE INDEX IF NOT EXISTS idx_locations_lat_lon ON event.locations(lat, lon);

-- Таблица событий
CREATE TABLE IF NOT EXISTS event.events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    annotation TEXT NOT NULL,
    category_id BIGINT NOT NULL REFERENCES explore.categories(id),
    paid BOOLEAN NOT NULL,
    event_date TIMESTAMP NOT NULL,
    initiator_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    participant_limit INT NOT NULL DEFAULT 0,
    state VARCHAR(255),
    created_on TIMESTAMP NOT NULL DEFAULT now(),
    location_id INT NOT NULL REFERENCES event.locations(id),
    request_moderation BOOLEAN NOT NULL DEFAULT TRUE,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    confirmed_requests INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_events_category_id ON event.events(category_id);
CREATE INDEX IF NOT EXISTS idx_events_event_date ON event.events(event_date);
CREATE INDEX IF NOT EXISTS idx_events_initiator_id ON event.events(initiator_id);
CREATE INDEX IF NOT EXISTS idx_events_state ON event.events(state);
CREATE INDEX IF NOT EXISTS idx_events_location_id ON event.events(location_id);
CREATE INDEX IF NOT EXISTS idx_events_paid ON event.events(paid);

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
