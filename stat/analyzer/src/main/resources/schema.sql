-- 1) схема
CREATE SCHEMA IF NOT EXISTS stats;

-- (необязательно) сделать её первой в search_path для текущей сессии
-- SET search_path TO stats, public;

-- 2) таблица истории взаимодействий пользователя с событиями
CREATE TABLE IF NOT EXISTS stats.interactions (
    user_id    BIGINT              NOT NULL,
    event_id   BIGINT              NOT NULL,
    weight     DOUBLE PRECISION    NOT NULL,  -- 0..1, максимальный вес
    updated_at TIMESTAMPTZ         NOT NULL,
    CONSTRAINT interactions_pk PRIMARY KEY (user_id, event_id)
);

-- индексы
CREATE INDEX IF NOT EXISTS ix_interactions_user_updated
    ON stats.interactions (user_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS ix_interactions_event
    ON stats.interactions (event_id);

-- 3) таблица коэффициентов сходства событий (пара упорядочена: event_a < event_b)
CREATE TABLE IF NOT EXISTS stats.event_similarity (
    event_a    BIGINT              NOT NULL,
    event_b    BIGINT              NOT NULL,
    score      DOUBLE PRECISION    NOT NULL,
    updated_at TIMESTAMPTZ         NOT NULL,
    CONSTRAINT event_similarity_pk PRIMARY KEY (event_a, event_b),
    CONSTRAINT event_similarity_order CHECK (event_a < event_b)
);

CREATE INDEX IF NOT EXISTS ix_event_similarity_a
    ON stats.event_similarity (event_a);

CREATE INDEX IF NOT EXISTS ix_event_similarity_b
    ON stats.event_similarity (event_b);