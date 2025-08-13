-- Создание схемы, если ещё не существует
CREATE SCHEMA IF NOT EXISTS comment;

-- Таблица комментариев
CREATE TABLE IF NOT EXISTS comment.comments (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(2000) NOT NULL,
    event_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_comments_text ON comment.comments(text);
CREATE INDEX IF NOT EXISTS idx_comments_event_id ON comment.comments(event_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comment.comments(author_id);
CREATE INDEX IF NOT EXISTS idx_comments_created ON comment.comments(created);