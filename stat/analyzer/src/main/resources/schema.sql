-- таблица истории взаимодействий пользователя с событиями
CREATE TABLE IF NOT EXISTS interactions (
    user_id    BIGINT       NOT NULL,
    event_id   BIGINT       NOT NULL,
    weight     DOUBLE PRECISION NOT NULL,  -- 0..1, максимальный вес
    updated_at TIMESTAMPTZ  NOT NULL,
    CONSTRAINT interactions_pk PRIMARY KEY (user_id, event_id)
);

-- полезные индексы
CREATE INDEX IF NOT EXISTS ix_interactions_user_updated
    ON interactions (user_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS ix_interactions_event
    ON interactions (event_id);

-- таблица коэффициентов сходства событий (симметричная пара, first < second)
CREATE TABLE IF NOT EXISTS event_similarity (
    event_a    BIGINT       NOT NULL,
    event_b    BIGINT       NOT NULL,
    score      DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    CONSTRAINT event_similarity_pk PRIMARY KEY (event_a, event_b),
    CONSTRAINT event_similarity_order CHECK (event_a < event_b)
);

CREATE INDEX IF NOT EXISTS ix_event_similarity_a ON event_similarity(event_a);
CREATE INDEX IF NOT EXISTS ix_event_similarity_b ON event_similarity(event_b);