-- Plans
CREATE TABLE plans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100)        NOT NULL,
    type            VARCHAR(20)         NOT NULL, -- MONTHLY, QUARTERLY, SEMIANNUAL, ANNUAL, CUSTOM
    duration_months INTEGER             NOT NULL,
    price           NUMERIC(10,2)       NOT NULL,
    discount_percentage NUMERIC(5,2)    NOT NULL DEFAULT 0,
    description     TEXT,
    active          BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now()
);

-- Students
CREATE TABLE students (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID                REFERENCES users (id) ON DELETE SET NULL,
    name                    VARCHAR(150)        NOT NULL,
    photo_url               VARCHAR(500),
    cpf                     VARCHAR(14)         NOT NULL UNIQUE,
    rg                      VARCHAR(20),
    gender                  VARCHAR(20),
    phone                   VARCHAR(30)         NOT NULL,
    email                   VARCHAR(180)        NOT NULL,
    zip_code                VARCHAR(10),
    address                 VARCHAR(255),
    address_number          VARCHAR(20),
    address_complement      VARCHAR(100),
    neighborhood            VARCHAR(100),
    city                    VARCHAR(100),
    state                   VARCHAR(2),
    emergency_contact_name  VARCHAR(150),
    emergency_contact_phone VARCHAR(30),
    birth_date              DATE                NOT NULL,
    notes                   TEXT,
    status                  VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, PENDING
    registration_code       VARCHAR(20)         NOT NULL UNIQUE,
    created_at              TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_students_name ON students (name);
CREATE INDEX idx_students_cpf ON students (cpf);
CREATE INDEX idx_students_status ON students (status);

-- Enrollments
CREATE TABLE enrollments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID                NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    plan_id         UUID                NOT NULL REFERENCES plans (id),
    start_date      DATE                NOT NULL,
    end_date        DATE                NOT NULL,
    status          VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, CANCELED, FROZEN, EXPIRED
    frozen_since     DATE,
    price_paid      NUMERIC(10,2)       NOT NULL,
    canceled_at     TIMESTAMP,
    cancel_reason    VARCHAR(255),
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_enrollments_student_id ON enrollments (student_id);
CREATE INDEX idx_enrollments_status ON enrollments (status);
CREATE INDEX idx_enrollments_end_date ON enrollments (end_date);
