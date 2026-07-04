-- Default admin user (password: Admin@123 — BCrypt hash, must be changed after first login)
INSERT INTO users (id, name, email, password_hash, role, active)
VALUES (gen_random_uuid(), 'Administrador', 'admin@gymflow.com',
        '$2a$10$DTdnHkF6U4FEd.9Dvgf/zuEBn4Xc1Os.XofGyeov1yIIZgwdwEdX6', 'ADMIN', TRUE);

-- Default plans
INSERT INTO plans (id, name, type, duration_months, price, discount_percentage, description, active) VALUES
    (gen_random_uuid(), 'Mensal', 'MONTHLY', 1, 129.90, 0, 'Plano mensal, sem fidelidade', TRUE),
    (gen_random_uuid(), 'Trimestral', 'QUARTERLY', 3, 349.90, 10, 'Plano trimestral com desconto', TRUE),
    (gen_random_uuid(), 'Semestral', 'SEMIANNUAL', 6, 629.90, 15, 'Plano semestral com desconto', TRUE),
    (gen_random_uuid(), 'Anual', 'ANNUAL', 12, 1099.90, 25, 'Plano anual com maior desconto', TRUE);

-- Product categories
INSERT INTO product_categories (id, name) VALUES
    (gen_random_uuid(), 'Suplementos'),
    (gen_random_uuid(), 'Acessórios'),
    (gen_random_uuid(), 'Vestuário'),
    (gen_random_uuid(), 'Bebidas');
