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
