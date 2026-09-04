--liquibase formatted sql

--changeset andriy:0001-initial-empty runOnChange:false
--comment Initial Liquibase baseline. Add schema changes in new SQL changesets when the domain model is defined.
SELECT 1;

--changeset andriy:0002-transaction-risk-schema
CREATE TYPE activity_type AS ENUM ('CARD', 'PAYMENT', 'CRYPTO');
CREATE TYPE risk_rule_applies_to AS ENUM ('CARD', 'PAYMENT', 'CRYPTO', 'ALL');

CREATE TABLE customers (
    customer_id UUID PRIMARY KEY
);

CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY,
    customer_id UUID REFERENCES customers(customer_id),
    activity_type activity_type,
    amount NUMERIC(18, 2),
    currency VARCHAR(10),
    status VARCHAR,
    created_at TIMESTAMPTZ
);

CREATE INDEX idx_transactions_customer_id ON transactions(customer_id);

CREATE TABLE card_activity (
    transaction_id UUID PRIMARY KEY REFERENCES transactions(transaction_id),
    card_pan VARCHAR,
    card_type VARCHAR,
    merchant_name VARCHAR,
    mcc_code VARCHAR(4),
    card_present BOOLEAN,
    authorization_code VARCHAR,
    decline_reason VARCHAR
);

CREATE TABLE payment_activity (
    transaction_id UUID PRIMARY KEY REFERENCES transactions(transaction_id),
    payment_method VARCHAR,
    sender_account VARCHAR,
    receiver_account VARCHAR,
    receiver_bank_country CHAR(2)
);

CREATE TABLE crypto_activity (
    transaction_id UUID PRIMARY KEY REFERENCES transactions(transaction_id),
    blockchain VARCHAR,
    wallet_address_from VARCHAR,
    wallet_address_to VARCHAR,
    tx_hash VARCHAR,
    exchange_name VARCHAR
);

CREATE TABLE risk_rules (
    rule_id UUID PRIMARY KEY,
    rule_name VARCHAR,
    applies_to risk_rule_applies_to,
    threshold_logic TEXT,
    weight NUMERIC(5, 2)
);

CREATE TABLE risk_assessments (
    assessment_id UUID PRIMARY KEY,
    transaction_id UUID REFERENCES transactions(transaction_id),
    rule_id UUID REFERENCES risk_rules(rule_id),
    triggered_at TIMESTAMPTZ,
    score_contribution NUMERIC(5, 2)
);

CREATE INDEX idx_risk_assessments_transaction_id ON risk_assessments(transaction_id);
CREATE INDEX idx_risk_assessments_rule_id ON risk_assessments(rule_id);

--changeset andriy:0003-operator-users
CREATE TABLE operator_users (
    operator_id UUID PRIMARY KEY,
    provider VARCHAR(40) NOT NULL,
    provider_subject_hash VARCHAR(64) NOT NULL,
    blocked BOOLEAN NOT NULL DEFAULT FALSE,
    block_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    last_login_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_operator_users_provider_subject_hash UNIQUE (provider, provider_subject_hash)
);

CREATE INDEX idx_operator_users_blocked ON operator_users(blocked);

--changeset andriy:0004-demo-customer-activity-data
--comment Demo seed data: 100 customers, each with 100 activities split across card, payment, and crypto.
WITH generated_customers AS (
    SELECT
        customer_number,
        (
            SUBSTRING(MD5('customer-' || customer_number), 1, 8) || '-' ||
            SUBSTRING(MD5('customer-' || customer_number), 9, 4) || '-' ||
            SUBSTRING(MD5('customer-' || customer_number), 13, 4) || '-' ||
            SUBSTRING(MD5('customer-' || customer_number), 17, 4) || '-' ||
            SUBSTRING(MD5('customer-' || customer_number), 21, 12)
        )::UUID AS customer_id
    FROM GENERATE_SERIES(1, 100) AS customer_number
)
INSERT INTO customers (customer_id)
SELECT customer_id
FROM generated_customers;

WITH generated_transactions AS (
    SELECT
        customer_number,
        activity_number,
        (
            SUBSTRING(MD5('customer-' || customer_number), 1, 8) || '-' ||
            SUBSTRING(MD5('customer-' || customer_number), 9, 4) || '-' ||
            SUBSTRING(MD5('customer-' || customer_number), 13, 4) || '-' ||
            SUBSTRING(MD5('customer-' || customer_number), 17, 4) || '-' ||
            SUBSTRING(MD5('customer-' || customer_number), 21, 12)
        )::UUID AS customer_id,
        (
            SUBSTRING(MD5('transaction-' || customer_number || '-' || activity_number), 1, 8) || '-' ||
            SUBSTRING(MD5('transaction-' || customer_number || '-' || activity_number), 9, 4) || '-' ||
            SUBSTRING(MD5('transaction-' || customer_number || '-' || activity_number), 13, 4) || '-' ||
            SUBSTRING(MD5('transaction-' || customer_number || '-' || activity_number), 17, 4) || '-' ||
            SUBSTRING(MD5('transaction-' || customer_number || '-' || activity_number), 21, 12)
        )::UUID AS transaction_id,
        CASE activity_number % 3
            WHEN 0 THEN 'CARD'::activity_type
            WHEN 1 THEN 'PAYMENT'::activity_type
            ELSE 'CRYPTO'::activity_type
        END AS activity_type
    FROM GENERATE_SERIES(1, 100) AS customer_number
    CROSS JOIN GENERATE_SERIES(1, 100) AS activity_number
)
INSERT INTO transactions (transaction_id, customer_id, activity_type, amount, currency, status, created_at)
SELECT
    transaction_id,
    customer_id,
    activity_type,
    ROUND((((customer_number * 97) + (activity_number * 13)) % 25000 + 10)::NUMERIC, 2),
    CASE activity_type
        WHEN 'CRYPTO' THEN CASE activity_number % 3 WHEN 0 THEN 'BTC' WHEN 1 THEN 'ETH' ELSE 'USDC' END
        ELSE CASE activity_number % 4 WHEN 0 THEN 'CHF' WHEN 1 THEN 'EUR' WHEN 2 THEN 'USD' ELSE 'GBP' END
    END,
    CASE activity_number % 10
        WHEN 0 THEN 'Failed'
        WHEN 1 THEN 'Pending'
        WHEN 2 THEN 'Reversed'
        ELSE 'Completed'
    END,
    TIMESTAMPTZ '2026-01-01 00:00:00+00' + ((customer_number * 100 + activity_number) * INTERVAL '17 minutes')
FROM generated_transactions;

WITH card_transactions AS (
    SELECT
        transaction_id,
        customer_id,
        ROW_NUMBER() OVER (ORDER BY customer_id, transaction_id) AS row_number
    FROM transactions
    WHERE activity_type = 'CARD'
)
INSERT INTO card_activity (
    transaction_id,
    card_pan,
    card_type,
    merchant_name,
    mcc_code,
    card_present,
    authorization_code,
    decline_reason
)
SELECT
    transaction_id,
    '****' || LPAD((1000 + (row_number % 9000))::TEXT, 4, '0'),
    CASE row_number % 3 WHEN 0 THEN 'Debit' WHEN 1 THEN 'Credit' ELSE 'Prepaid' END,
    'Merchant ' || LPAD((row_number % 250)::TEXT, 3, '0'),
    LPAD((5000 + (row_number % 400))::TEXT, 4, '0'),
    row_number % 2 = 0,
    'AUTH' || LPAD(row_number::TEXT, 8, '0'),
    CASE row_number % 10 WHEN 0 THEN 'Insufficient funds' ELSE NULL END
FROM card_transactions;

WITH payment_transactions AS (
    SELECT
        transaction_id,
        ROW_NUMBER() OVER (ORDER BY customer_id, transaction_id) AS row_number
    FROM transactions
    WHERE activity_type = 'PAYMENT'
)
INSERT INTO payment_activity (
    transaction_id,
    payment_method,
    sender_account,
    receiver_account,
    receiver_bank_country
)
SELECT
    transaction_id,
    CASE row_number % 4 WHEN 0 THEN 'ACH' WHEN 1 THEN 'Wire' WHEN 2 THEN 'SWIFT' ELSE 'P2P' END,
    'CH' || LPAD(row_number::TEXT, 19, '0'),
    'DE' || LPAD((row_number + 100000)::TEXT, 20, '0'),
    CASE row_number % 5 WHEN 0 THEN 'CH' WHEN 1 THEN 'DE' WHEN 2 THEN 'FR' WHEN 3 THEN 'GB' ELSE 'US' END
FROM payment_transactions;

WITH crypto_transactions AS (
    SELECT
        transaction_id,
        ROW_NUMBER() OVER (ORDER BY customer_id, transaction_id) AS row_number
    FROM transactions
    WHERE activity_type = 'CRYPTO'
)
INSERT INTO crypto_activity (
    transaction_id,
    blockchain,
    wallet_address_from,
    wallet_address_to,
    tx_hash,
    exchange_name
)
SELECT
    transaction_id,
    CASE row_number % 4 WHEN 0 THEN 'BTC' WHEN 1 THEN 'ETH' WHEN 2 THEN 'SOL' ELSE 'XRP' END,
    'wallet-from-' || MD5('from-' || row_number),
    'wallet-to-' || MD5('to-' || row_number),
    MD5('tx-' || row_number) || MD5('hash-' || row_number),
    CASE row_number % 4 WHEN 0 THEN 'Swissquote' WHEN 1 THEN 'Coinbase' WHEN 2 THEN 'Kraken' ELSE NULL END
FROM crypto_transactions;

INSERT INTO risk_rules (rule_id, rule_name, applies_to, threshold_logic, weight)
VALUES
    ('3c8ee0aa-cf16-406d-a0e7-49dbf7466601', 'High-value card transaction', 'CARD', 'amount >= 10000 and card_present = false', 20.00),
    ('c8692f01-167f-449d-9286-571f1c1f6f01', 'Cross-border payment', 'PAYMENT', 'receiver_bank_country differs from customer country', 15.00),
    ('c41d73e7-3d8a-4f3d-bab4-b6054cf7957e', 'Crypto exchange transfer', 'CRYPTO', 'exchange_name is present', 10.00),
    ('885be553-1447-42fd-b0d3-b8463c7813b4', 'Failed or reversed activity', 'ALL', 'status in Failed or Reversed', 12.50);

WITH scored_transactions AS (
    SELECT
        transaction_id,
        activity_type,
        status,
        amount,
        ROW_NUMBER() OVER (ORDER BY created_at, transaction_id) AS row_number
    FROM transactions
    WHERE status IN ('Failed', 'Reversed')
       OR amount >= 10000
       OR activity_type = 'CRYPTO'
)
INSERT INTO risk_assessments (
    assessment_id,
    transaction_id,
    rule_id,
    triggered_at,
    score_contribution
)
SELECT
    (
        SUBSTRING(MD5('assessment-' || transaction_id), 1, 8) || '-' ||
        SUBSTRING(MD5('assessment-' || transaction_id), 9, 4) || '-' ||
        SUBSTRING(MD5('assessment-' || transaction_id), 13, 4) || '-' ||
        SUBSTRING(MD5('assessment-' || transaction_id), 17, 4) || '-' ||
        SUBSTRING(MD5('assessment-' || transaction_id), 21, 12)
    )::UUID,
    transaction_id,
    CASE
        WHEN status IN ('Failed', 'Reversed') THEN '885be553-1447-42fd-b0d3-b8463c7813b4'::UUID
        WHEN activity_type = 'CARD' THEN '3c8ee0aa-cf16-406d-a0e7-49dbf7466601'::UUID
        WHEN activity_type = 'PAYMENT' THEN 'c8692f01-167f-449d-9286-571f1c1f6f01'::UUID
        ELSE 'c41d73e7-3d8a-4f3d-bab4-b6054cf7957e'::UUID
    END,
    TIMESTAMPTZ '2026-01-01 00:00:00+00' + (row_number * INTERVAL '19 minutes'),
    CASE
        WHEN status IN ('Failed', 'Reversed') THEN 12.50
        WHEN amount >= 10000 THEN 20.00
        ELSE 10.00
    END
FROM scored_transactions
WHERE row_number % 2 = 0;

--changeset andriy:0005-more-demo-risk-rules
--comment Additional demo risk rules for richer operator analytics examples.
INSERT INTO risk_rules (rule_id, rule_name, applies_to, threshold_logic, weight)
VALUES
    ('68b1dd57-ec80-4f09-9f9c-4561e58e6001', 'Repeated failed card authorizations', 'CARD', 'three or more failed card attempts for the same customer within 24 hours', 18.00),
    ('2f5225f0-2396-4909-b0cf-4475fce66002', 'Unusual merchant category pattern', 'CARD', 'mcc_code differs from the customer usual merchant categories and amount >= 1500', 8.50),
    ('1d16706c-8c4a-41b4-b99d-2e6f1e566003', 'Card-not-present high-value activity', 'CARD', 'card_present = false and amount >= 5000', 16.00),
    ('a6d547bb-9ae1-4e6a-aa2f-8cfaa1186004', 'High-value international wire', 'PAYMENT', 'payment_method in Wire or SWIFT and amount >= 25000', 25.00),
    ('783ba2e2-384e-44b2-a3a2-501c63f46005', 'New receiver payment velocity', 'PAYMENT', 'multiple payments to first-seen receivers within 48 hours', 14.00),
    ('059f48f5-fd2b-4a8f-bddd-ea345e386006', 'Round-amount structuring signal', 'PAYMENT', 'several payments just below a reporting threshold', 22.00),
    ('27a67f38-d581-4015-b738-e468d8736007', 'High-value crypto transfer', 'CRYPTO', 'amount >= 10000 and tx_hash is present', 21.00),
    ('f699d5cb-7f39-4e43-9b3f-60d4178f6008', 'Multiple blockchain destinations', 'CRYPTO', 'transfers to multiple destination wallets in a short time window', 17.50);
