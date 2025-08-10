-- Создание схемы, если ещё не существует
CREATE SCHEMA IF NOT EXISTS users;

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users.users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_users_email ON users.users(email);
CREATE INDEX IF NOT EXISTS idx_users_name ON users.users(name);