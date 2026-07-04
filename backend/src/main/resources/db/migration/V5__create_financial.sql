-- Financial transactions (revenue and expenses, unified ledger)
CREATE TABLE financial_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type            VARCHAR(10)         NOT NULL, -- INCOME, EXPENSE
    category        VARCHAR(50)         NOT NULL, -- MONTHLY_FEE, PRODUCT_SALE, SALARY, RENT, MAINTENANCE, OTHER
    description     VARCHAR(255)        NOT NULL,
    amount          NUMERIC(10,2)       NOT NULL,
    discount        NUMERIC(10,2)       NOT NULL DEFAULT 0,
    penalty         NUMERIC(10,2)       NOT NULL DEFAULT 0,
    payment_method  VARCHAR(20),        -- PIX, CREDIT_CARD, DEBIT_CARD, CASH, BOLETO
    status          VARCHAR(20)         NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, OVERDUE, CANCELED
    due_date        DATE                NOT NULL,
    paid_at         TIMESTAMP,
    student_id      UUID                REFERENCES students (id) ON DELETE SET NULL,
    enrollment_id   UUID                REFERENCES enrollments (id) ON DELETE SET NULL,
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_financial_transactions_type ON financial_transactions (type);
CREATE INDEX idx_financial_transactions_status ON financial_transactions (status);
CREATE INDEX idx_financial_transactions_due_date ON financial_transactions (due_date);
CREATE INDEX idx_financial_transactions_student_id ON financial_transactions (student_id);
