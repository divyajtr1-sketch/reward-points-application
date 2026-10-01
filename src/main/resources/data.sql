INSERT INTO customers (id, name) VALUES
    (1, 'Alice'),
    (2, 'Bob'),
    (3, 'Charlie');

INSERT INTO transactions (id, customer_id, amount, transaction_date) VALUES
    (1, 1, 120.00, '2026-07-10'),
    (2, 1, 75.00,  '2026-08-12'),
    (3, 1, 40.00,  '2026-09-15'),
    (4, 2, 200.00, '2026-07-05'),
    (5, 2, 50.00,  '2026-08-18'),
    (6, 2, 150.00, '2026-09-20'),
    (7, 3, 110.00, '2026-07-08'),
    (8, 3, 80.00,  '2026-08-21'),
    (9, 3, 100.00, '2026-09-25');

ALTER TABLE customers ALTER COLUMN id RESTART WITH 4;
ALTER TABLE transactions ALTER COLUMN id RESTART WITH 10;
