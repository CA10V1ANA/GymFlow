-- Attendance / Check-in
CREATE TABLE attendances (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID                NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    check_in        TIMESTAMP           NOT NULL DEFAULT now(),
    check_out       TIMESTAMP,
    method          VARCHAR(20)         NOT NULL DEFAULT 'CODE', -- QR_CODE, CODE, MANUAL
    created_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_attendances_student_id ON attendances (student_id);
CREATE INDEX idx_attendances_check_in ON attendances (check_in);

-- Employees (extends users with role-specific data)
CREATE TABLE employees (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID                NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    position        VARCHAR(100)        NOT NULL,
    hired_at        DATE                NOT NULL,
    salary          NUMERIC(10,2),
    cpf             VARCHAR(14)         UNIQUE,
    status          VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_employees_user_id ON employees (user_id);
