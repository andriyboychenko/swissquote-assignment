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

--changeset andriy:0013-first-five-demo-risk-bands
--comment Pin the first five documented demo customers to predictable LOW, LOW, MEDIUM, MEDIUM, HIGH analysis examples.
--validCheckSum 9:7ac11a302098d0d0849cb2a53927e55c
WITH demo_risk_bands(customer_id, risk_band) AS (
    VALUES
        ('005514e6-1ebe-8010-de91-aff66d1d9484'::UUID, 'LOW'),
        ('0a3ab26d-12b1-0efc-65d4-a2d6cc72ec67'::UUID, 'LOW'),
        ('0abe215d-4832-1215-fe7a-264bfb844be9'::UUID, 'MEDIUM'),
        ('0c534877-7dee-ed33-5278-68e39c8fe785'::UUID, 'MEDIUM'),
        ('0ddc4d69-0dcf-fba9-15c2-88a68e6665de'::UUID, 'HIGH')
)
DELETE FROM risk_assessments assessment
USING transactions tx, demo_risk_bands bands
WHERE assessment.transaction_id = tx.transaction_id
  AND tx.customer_id = bands.customer_id;

WITH demo_risk_bands(customer_id, risk_band) AS (
    VALUES
        ('005514e6-1ebe-8010-de91-aff66d1d9484'::UUID, 'LOW'),
        ('0a3ab26d-12b1-0efc-65d4-a2d6cc72ec67'::UUID, 'LOW'),
        ('0abe215d-4832-1215-fe7a-264bfb844be9'::UUID, 'MEDIUM'),
        ('0c534877-7dee-ed33-5278-68e39c8fe785'::UUID, 'MEDIUM'),
        ('0ddc4d69-0dcf-fba9-15c2-88a68e6665de'::UUID, 'HIGH')
),
ranked_transactions AS (
    SELECT
        tx.transaction_id,
        bands.risk_band,
        ROW_NUMBER() OVER (PARTITION BY tx.customer_id ORDER BY tx.created_at, tx.transaction_id) AS row_number
    FROM transactions tx
    JOIN demo_risk_bands bands ON bands.customer_id = tx.customer_id
)
UPDATE transactions tx
SET
    amount = CASE
        WHEN ranked_transactions.risk_band = 'LOW' THEN ROUND((25 + ranked_transactions.row_number)::NUMERIC, 2)
        WHEN ranked_transactions.risk_band = 'MEDIUM' AND ranked_transactions.row_number <= 3 THEN 7500.00
        WHEN ranked_transactions.risk_band = 'HIGH' AND ranked_transactions.row_number <= 7 THEN 18000.00
        ELSE tx.amount
    END,
    status = CASE
        WHEN ranked_transactions.risk_band = 'LOW' THEN 'Completed'
        WHEN ranked_transactions.risk_band = 'MEDIUM' AND ranked_transactions.row_number = 3 THEN 'Failed'
        WHEN ranked_transactions.risk_band = 'HIGH' AND ranked_transactions.row_number IN (3, 6) THEN 'Reversed'
        ELSE tx.status
    END,
    risk_indicators = '[]'::jsonb
FROM ranked_transactions
WHERE ranked_transactions.transaction_id = tx.transaction_id;

WITH demo_risk_bands(customer_id, risk_band) AS (
    VALUES
        ('005514e6-1ebe-8010-de91-aff66d1d9484'::UUID, 'LOW'),
        ('0a3ab26d-12b1-0efc-65d4-a2d6cc72ec67'::UUID, 'LOW'),
        ('0abe215d-4832-1215-fe7a-264bfb844be9'::UUID, 'MEDIUM'),
        ('0c534877-7dee-ed33-5278-68e39c8fe785'::UUID, 'MEDIUM'),
        ('0ddc4d69-0dcf-fba9-15c2-88a68e6665de'::UUID, 'HIGH')
),
ranked_transactions AS (
    SELECT
        tx.transaction_id,
        tx.activity_type,
        tx.created_at,
        bands.risk_band,
        ROW_NUMBER() OVER (PARTITION BY tx.customer_id ORDER BY tx.created_at, tx.transaction_id) AS row_number
    FROM transactions tx
    JOIN demo_risk_bands bands ON bands.customer_id = tx.customer_id
),
selected_transactions AS (
    SELECT *
    FROM ranked_transactions
    WHERE CASE
        WHEN risk_band = 'MEDIUM' THEN row_number <= 3
        WHEN risk_band = 'HIGH' THEN row_number <= 7
        ELSE FALSE
    END
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
        SUBSTRING(MD5('first-five-demo-risk-band-' || transaction_id), 1, 8) || '-' ||
        SUBSTRING(MD5('first-five-demo-risk-band-' || transaction_id), 9, 4) || '-' ||
        SUBSTRING(MD5('first-five-demo-risk-band-' || transaction_id), 13, 4) || '-' ||
        SUBSTRING(MD5('first-five-demo-risk-band-' || transaction_id), 17, 4) || '-' ||
        SUBSTRING(MD5('first-five-demo-risk-band-' || transaction_id), 21, 12)
    )::UUID,
    transaction_id,
    CASE
        WHEN activity_type = 'PAYMENT' THEN 'c8692f01-167f-449d-9286-571f1c1f6f01'::UUID
        WHEN activity_type = 'CRYPTO' THEN '27a67f38-d581-4015-b738-e468d8736007'::UUID
        ELSE '1d16706c-8c4a-41b4-b99d-2e6f1e566003'::UUID
    END,
    created_at + INTERVAL '2 minutes',
    CASE
        WHEN risk_band = 'MEDIUM' AND row_number <= 2 THEN 20.00
        WHEN risk_band = 'MEDIUM' THEN 15.00
        WHEN risk_band = 'HIGH' AND row_number = 1 THEN 25.00
        ELSE 20.00
    END
FROM selected_transactions;

WITH demo_risk_bands(customer_id, risk_band) AS (
    VALUES
        ('005514e6-1ebe-8010-de91-aff66d1d9484'::UUID, 'LOW'),
        ('0a3ab26d-12b1-0efc-65d4-a2d6cc72ec67'::UUID, 'LOW'),
        ('0abe215d-4832-1215-fe7a-264bfb844be9'::UUID, 'MEDIUM'),
        ('0c534877-7dee-ed33-5278-68e39c8fe785'::UUID, 'MEDIUM'),
        ('0ddc4d69-0dcf-fba9-15c2-88a68e6665de'::UUID, 'HIGH')
),
indicator_values AS (
    SELECT
        ra.transaction_id,
        JSONB_AGG(
            JSONB_BUILD_OBJECT(
                'ruleName', rr.rule_name,
                'severity', CASE
                    WHEN ra.score_contribution >= 20 THEN 'HIGH'
                    WHEN ra.score_contribution >= 12 THEN 'MEDIUM'
                    ELSE 'LOW'
                END,
                'scoreContribution', ra.score_contribution
            )
            ORDER BY ra.score_contribution DESC, rr.rule_name ASC
        ) AS risk_indicators
    FROM risk_assessments ra
    JOIN risk_rules rr ON rr.rule_id = ra.rule_id
    JOIN transactions tx ON tx.transaction_id = ra.transaction_id
    JOIN demo_risk_bands bands ON bands.customer_id = tx.customer_id
    GROUP BY ra.transaction_id
)
UPDATE transactions tx
SET risk_indicators = indicator_values.risk_indicators
FROM indicator_values
WHERE indicator_values.transaction_id = tx.transaction_id;

--changeset andriy:0014-randomize-demo-risk-placement
--comment Distribute existing first-five flagged transactions across each customer's timeline without changing risk bands.
WITH demo_customers(customer_id) AS (
    VALUES
        ('005514e6-1ebe-8010-de91-aff66d1d9484'::UUID),
        ('0a3ab26d-12b1-0efc-65d4-a2d6cc72ec67'::UUID),
        ('0abe215d-4832-1215-fe7a-264bfb844be9'::UUID),
        ('0c534877-7dee-ed33-5278-68e39c8fe785'::UUID),
        ('0ddc4d69-0dcf-fba9-15c2-88a68e6665de'::UUID)
),
flagged_transactions AS (
    SELECT
        tx.transaction_id,
        tx.customer_id,
        ROW_NUMBER() OVER (
            PARTITION BY tx.customer_id
            ORDER BY MD5(tx.transaction_id::TEXT)
        ) AS flagged_number,
        MOD(ABS(HASHTEXTEXTENDED(tx.transaction_id::TEXT, 0)), 90) AS timeline_slot
    FROM transactions tx
    JOIN demo_customers demo ON demo.customer_id = tx.customer_id
    WHERE EXISTS (
        SELECT 1
        FROM risk_assessments assessment
        WHERE assessment.transaction_id = tx.transaction_id
    )
),
customer_start AS (
    SELECT customer_id, MIN(created_at) AS first_created_at
    FROM transactions
    WHERE customer_id IN (SELECT customer_id FROM demo_customers)
    GROUP BY customer_id
)
UPDATE transactions tx
SET created_at = customer_start.first_created_at
    + ((flagged_transactions.timeline_slot + flagged_transactions.flagged_number) * INTERVAL '4 hours')
FROM flagged_transactions
JOIN customer_start ON customer_start.customer_id = flagged_transactions.customer_id
WHERE tx.transaction_id = flagged_transactions.transaction_id;

--changeset andriy:0009-transaction-risk-indicators
--comment Persist row-level risk indicators as JSONB metadata for customer activity review highlighting.
ALTER TABLE transactions
    ADD COLUMN risk_indicators JSONB NOT NULL DEFAULT '[]'::jsonb;

CREATE INDEX idx_transactions_risk_indicators ON transactions USING GIN (risk_indicators);

WITH indicator_values AS (
    SELECT
        ra.transaction_id,
        JSONB_AGG(
            JSONB_BUILD_OBJECT(
                'ruleName', rr.rule_name,
                'severity', CASE
                    WHEN ra.score_contribution >= 20 THEN 'HIGH'
                    WHEN ra.score_contribution >= 12 THEN 'MEDIUM'
                    ELSE 'LOW'
                END,
                'scoreContribution', ra.score_contribution
            )
            ORDER BY ra.score_contribution DESC, rr.rule_name ASC
        ) AS risk_indicators
    FROM risk_assessments ra
    JOIN risk_rules rr ON rr.rule_id = ra.rule_id
    GROUP BY ra.transaction_id
)
UPDATE transactions t
SET risk_indicators = indicator_values.risk_indicators
FROM indicator_values
WHERE indicator_values.transaction_id = t.transaction_id;

--changeset andriy:0008-ai-analysis-schema
--comment Persist AI analysis requests, results, and RAG evidence for later operator review.
CREATE TABLE ai_analysis_requests (
    analysis_request_id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(customer_id),
    requested_by_operator_id UUID REFERENCES operator_users(operator_id),
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    failure_reason TEXT
);

CREATE INDEX idx_ai_analysis_requests_customer_id ON ai_analysis_requests(customer_id);
CREATE INDEX idx_ai_analysis_requests_requested_by_operator_id ON ai_analysis_requests(requested_by_operator_id);
CREATE INDEX idx_ai_analysis_requests_status ON ai_analysis_requests(status);

CREATE TABLE ai_analysis_results (
    analysis_result_id UUID PRIMARY KEY,
    analysis_request_id UUID NOT NULL UNIQUE REFERENCES ai_analysis_requests(analysis_request_id),
    customer_id UUID NOT NULL REFERENCES customers(customer_id),
    risk_level VARCHAR(20) NOT NULL,
    summary TEXT NOT NULL,
    recommendations TEXT NOT NULL,
    model_name VARCHAR(120) NOT NULL,
    prompt_version VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ai_analysis_results_customer_id ON ai_analysis_results(customer_id);

CREATE TABLE ai_analysis_evidence (
    evidence_id UUID PRIMARY KEY,
    analysis_result_id UUID NOT NULL REFERENCES ai_analysis_results(analysis_result_id),
    source_type VARCHAR(40) NOT NULL,
    source_reference TEXT NOT NULL,
    excerpt TEXT NOT NULL,
    relevance_score NUMERIC(6, 4) NOT NULL
);

CREATE INDEX idx_ai_analysis_evidence_analysis_result_id ON ai_analysis_evidence(analysis_result_id);

--changeset andriy:0012-ai-analysis-request-operator-display-name
--comment Store the display name shown for the operator who requested an AI analysis.
ALTER TABLE ai_analysis_requests
ADD COLUMN IF NOT EXISTS requested_by_operator_display_name VARCHAR(160) NOT NULL DEFAULT 'Unknown operator';

--changeset andriy:0010-low-risk-demo-customer
--comment Keep one known demo customer intentionally low risk for operator testing.
DELETE FROM ai_analysis_evidence evidence
USING ai_analysis_results result
JOIN ai_analysis_requests request ON request.analysis_request_id = result.analysis_request_id
WHERE evidence.analysis_result_id = result.analysis_result_id
  AND request.customer_id = '005514e6-1ebe-8010-de91-aff66d1d9484';

DELETE FROM ai_analysis_results result
USING ai_analysis_requests request
WHERE result.analysis_request_id = request.analysis_request_id
  AND request.customer_id = '005514e6-1ebe-8010-de91-aff66d1d9484';

DELETE FROM ai_analysis_requests
WHERE customer_id = '005514e6-1ebe-8010-de91-aff66d1d9484';

DELETE FROM risk_assessments assessment
USING transactions tx
WHERE assessment.transaction_id = tx.transaction_id
  AND tx.customer_id = '005514e6-1ebe-8010-de91-aff66d1d9484';

WITH low_risk_transactions AS (
    SELECT
        transaction_id,
        ROW_NUMBER() OVER (ORDER BY created_at, transaction_id) AS row_number
    FROM transactions
    WHERE customer_id = '005514e6-1ebe-8010-de91-aff66d1d9484'
)
UPDATE transactions tx
SET
    amount = ROUND((25 + (low_risk_transactions.row_number % 450))::NUMERIC, 2),
    status = 'Completed',
    risk_indicators = '[]'::jsonb
FROM low_risk_transactions
WHERE low_risk_transactions.transaction_id = tx.transaction_id;

UPDATE card_activity
SET decline_reason = NULL
WHERE transaction_id IN (
    SELECT transaction_id
    FROM transactions
    WHERE customer_id = '005514e6-1ebe-8010-de91-aff66d1d9484'
);

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

--changeset andriy:0006-drop-legacy-market-quote
--comment Remove leftover quote-demo table from older local database volumes.
DROP TABLE IF EXISTS market_quote;

--changeset andriy:0007-randomized-demo-activity-data
--comment Rebuild demo customer activity with deterministic per-customer variation in activity type and status.
DELETE FROM risk_assessments;
DELETE FROM card_activity;
DELETE FROM payment_activity;
DELETE FROM crypto_activity;
DELETE FROM transactions;
DELETE FROM customers;

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
        ASCII(SUBSTRING(MD5('type-' || customer_number || '-' || activity_number), 1, 1)) AS type_bucket,
        ASCII(SUBSTRING(MD5('status-' || customer_number || '-' || activity_number), 1, 1)) AS status_bucket,
        ASCII(SUBSTRING(MD5('amount-' || customer_number || '-' || activity_number), 1, 1)) AS amount_bucket,
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
        )::UUID AS transaction_id
    FROM GENERATE_SERIES(1, 100) AS customer_number
    CROSS JOIN GENERATE_SERIES(1, 100) AS activity_number
),
typed_transactions AS (
    SELECT
        customer_number,
        activity_number,
        amount_bucket,
        status_bucket,
        customer_id,
        transaction_id,
        CASE type_bucket % 3
            WHEN 0 THEN 'CARD'::activity_type
            WHEN 1 THEN 'PAYMENT'::activity_type
            ELSE 'CRYPTO'::activity_type
        END AS activity_type
    FROM generated_transactions
)
INSERT INTO transactions (transaction_id, customer_id, activity_type, amount, currency, status, created_at)
SELECT
    transaction_id,
    customer_id,
    activity_type,
    ROUND((((customer_number * 113) + (activity_number * 29) + (amount_bucket * 17)) % 35000 + 10)::NUMERIC, 2),
    CASE activity_type
        WHEN 'CRYPTO' THEN CASE (amount_bucket + activity_number) % 4 WHEN 0 THEN 'BTC' WHEN 1 THEN 'ETH' WHEN 2 THEN 'USDC' ELSE 'SOL' END
        ELSE CASE (amount_bucket + customer_number + activity_number) % 5 WHEN 0 THEN 'CHF' WHEN 1 THEN 'EUR' WHEN 2 THEN 'USD' WHEN 3 THEN 'GBP' ELSE 'JPY' END
    END,
    CASE status_bucket % 12
        WHEN 0 THEN 'Failed'
        WHEN 1 THEN 'Pending'
        WHEN 2 THEN 'Reversed'
        ELSE 'Completed'
    END,
    TIMESTAMPTZ '2026-01-01 00:00:00+00' + (((customer_number * 137) + (activity_number * 23) + status_bucket) * INTERVAL '11 minutes')
FROM typed_transactions;

WITH card_transactions AS (
    SELECT
        transaction_id,
        status,
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
    CASE row_number % 6
        WHEN 0 THEN 'Market Lane'
        WHEN 1 THEN 'Cloud Travel'
        WHEN 2 THEN 'Alpine Pharmacy'
        WHEN 3 THEN 'Metro Fuel'
        WHEN 4 THEN 'Global Electronics'
        ELSE 'Harbor Hotel'
    END || ' ' || LPAD((row_number % 300)::TEXT, 3, '0'),
    LPAD((5000 + (row_number % 400))::TEXT, 4, '0'),
    row_number % 3 <> 0,
    'AUTH' || LPAD(row_number::TEXT, 8, '0'),
    CASE WHEN status = 'Failed' THEN CASE row_number % 3 WHEN 0 THEN 'Insufficient funds' WHEN 1 THEN 'Suspected fraud' ELSE 'Issuer unavailable' END ELSE NULL END
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
    CASE row_number % 4 WHEN 0 THEN 'DE' WHEN 1 THEN 'FR' WHEN 2 THEN 'GB' ELSE 'US' END || LPAD((row_number + 100000)::TEXT, 20, '0'),
    CASE row_number % 8 WHEN 0 THEN 'CH' WHEN 1 THEN 'DE' WHEN 2 THEN 'FR' WHEN 3 THEN 'GB' WHEN 4 THEN 'US' WHEN 5 THEN 'SG' WHEN 6 THEN 'AE' ELSE 'BR' END
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
    CASE row_number % 5 WHEN 0 THEN 'BTC' WHEN 1 THEN 'ETH' WHEN 2 THEN 'SOL' WHEN 3 THEN 'XRP' ELSE 'USDC' END,
    'wallet-from-' || MD5('from-' || row_number || '-' || transaction_id),
    'wallet-to-' || MD5('to-' || row_number || '-' || transaction_id),
    MD5('tx-' || row_number || '-' || transaction_id) || MD5('hash-' || row_number),
    CASE row_number % 5 WHEN 0 THEN 'Swissquote' WHEN 1 THEN 'Coinbase' WHEN 2 THEN 'Kraken' WHEN 3 THEN 'Binance' ELSE NULL END
FROM crypto_transactions;

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
        SUBSTRING(MD5('assessment-randomized-' || transaction_id), 1, 8) || '-' ||
        SUBSTRING(MD5('assessment-randomized-' || transaction_id), 9, 4) || '-' ||
        SUBSTRING(MD5('assessment-randomized-' || transaction_id), 13, 4) || '-' ||
        SUBSTRING(MD5('assessment-randomized-' || transaction_id), 17, 4) || '-' ||
        SUBSTRING(MD5('assessment-randomized-' || transaction_id), 21, 12)
    )::UUID,
    transaction_id,
    CASE
        WHEN status IN ('Failed', 'Reversed') THEN '885be553-1447-42fd-b0d3-b8463c7813b4'::UUID
        WHEN activity_type = 'CARD' AND amount >= 5000 THEN '1d16706c-8c4a-41b4-b99d-2e6f1e566003'::UUID
        WHEN activity_type = 'CARD' THEN '3c8ee0aa-cf16-406d-a0e7-49dbf7466601'::UUID
        WHEN activity_type = 'PAYMENT' AND amount >= 25000 THEN 'a6d547bb-9ae1-4e6a-aa2f-8cfaa1186004'::UUID
        WHEN activity_type = 'PAYMENT' THEN 'c8692f01-167f-449d-9286-571f1c1f6f01'::UUID
        WHEN activity_type = 'CRYPTO' AND amount >= 10000 THEN '27a67f38-d581-4015-b738-e468d8736007'::UUID
        ELSE 'c41d73e7-3d8a-4f3d-bab4-b6054cf7957e'::UUID
    END,
    TIMESTAMPTZ '2026-01-01 00:00:00+00' + (row_number * INTERVAL '13 minutes'),
    CASE
        WHEN status IN ('Failed', 'Reversed') THEN 12.50
        WHEN amount >= 25000 THEN 25.00
        WHEN amount >= 10000 THEN 20.00
        ELSE 10.00
    END
FROM scored_transactions
WHERE row_number % 2 = 0;
