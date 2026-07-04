-- Users & Security
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150)        NOT NULL,
    email           VARCHAR(180)        NOT NULL UNIQUE,
    password_hash   VARCHAR(255)        NOT NULL,
    role            VARCHAR(30)         NOT NULL, -- ADMIN, RECEPTIONIST, INSTRUCTOR, STUDENT
    phone           VARCHAR(30),
    avatar_url      VARCHAR(500),
    active          BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);

CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID                NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash      VARCHAR(255)        NOT NULL UNIQUE,
    expires_at      TIMESTAMP           NOT NULL,
    revoked         BOOLEAN             NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID                REFERENCES users (id) ON DELETE SET NULL,
    user_name       VARCHAR(150),
    action          VARCHAR(100)        NOT NULL,
    entity_name     VARCHAR(100),
    entity_id       VARCHAR(100),
    details         TEXT,
    ip_address      VARCHAR(64),
    created_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
