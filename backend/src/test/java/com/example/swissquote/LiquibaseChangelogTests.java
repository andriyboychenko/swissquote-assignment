package com.example.swissquote;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibaseChangelogTests {

    @Test
    void masterChangelogUsesFormattedSql() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).startsWith("--liquibase formatted sql");
            assertThat(changelog).contains("--changeset ");
        }
    }

    @Test
    void masterChangelogContainsTransactionAndRiskSchema() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).contains("CREATE TYPE activity_type AS ENUM ('CARD', 'PAYMENT', 'CRYPTO')");
            assertThat(changelog).contains("CREATE TYPE risk_rule_applies_to AS ENUM ('CARD', 'PAYMENT', 'CRYPTO', 'ALL')");
            assertThat(changelog).contains("CREATE TABLE customers");
            assertThat(changelog).contains("customer_id UUID PRIMARY KEY");
            assertThat(changelog).contains("CREATE TABLE transactions");
            assertThat(changelog).contains("transaction_id UUID PRIMARY KEY");
            assertThat(changelog).contains("customer_id UUID REFERENCES customers(customer_id)");
            assertThat(changelog).contains("activity_type activity_type");
            assertThat(changelog).contains("amount NUMERIC(18, 2)");
            assertThat(changelog).contains("created_at TIMESTAMPTZ");
            assertThat(changelog).contains("CREATE TABLE card_activity");
            assertThat(changelog).contains("CREATE TABLE payment_activity");
            assertThat(changelog).contains("CREATE TABLE crypto_activity");
            assertThat(changelog).contains("CREATE TABLE risk_rules");
            assertThat(changelog).contains("CREATE TABLE risk_assessments");
            assertThat(changelog).contains("triggered_at TIMESTAMPTZ");
            assertThat(changelog).contains("score_contribution NUMERIC(5, 2)");
        }
    }

    @Test
    void masterChangelogIndexesForeignKeys() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).contains("CREATE INDEX idx_transactions_customer_id ON transactions(customer_id)");
            assertThat(changelog).contains("transaction_id UUID PRIMARY KEY REFERENCES transactions(transaction_id)");
            assertThat(changelog).contains("CREATE INDEX idx_risk_assessments_transaction_id ON risk_assessments(transaction_id)");
            assertThat(changelog).contains("CREATE INDEX idx_risk_assessments_rule_id ON risk_assessments(rule_id)");
        }
    }

    @Test
    void masterChangelogContainsPseudonymousOperatorUsersSchema() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).contains("--changeset andriy:0003-operator-users");
            assertThat(changelog).contains("CREATE TABLE operator_users");
            assertThat(changelog).contains("operator_id UUID PRIMARY KEY");
            assertThat(changelog).contains("provider VARCHAR(40) NOT NULL");
            assertThat(changelog).contains("provider_subject_hash VARCHAR(64) NOT NULL");
            assertThat(changelog).contains("blocked BOOLEAN NOT NULL DEFAULT FALSE");
            assertThat(changelog).contains("block_reason TEXT");
            assertThat(changelog).contains("created_at TIMESTAMPTZ NOT NULL");
            assertThat(changelog).contains("last_login_at TIMESTAMPTZ NOT NULL");
            assertThat(changelog).contains("CONSTRAINT uk_operator_users_provider_subject_hash UNIQUE (provider, provider_subject_hash)");
            assertThat(changelog).contains("CREATE INDEX idx_operator_users_blocked ON operator_users(blocked)");
        }
    }

    @Test
    void masterChangelogContainsCompactDemoActivitySeedData() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).contains("--changeset andriy:0004-demo-customer-activity-data");
            assertThat(changelog).contains("FROM GENERATE_SERIES(1, 100) AS customer_number");
            assertThat(changelog).contains("CROSS JOIN GENERATE_SERIES(1, 100) AS activity_number");
            assertThat(changelog).contains("INSERT INTO card_activity");
            assertThat(changelog).contains("INSERT INTO payment_activity");
            assertThat(changelog).contains("INSERT INTO crypto_activity");
            assertThat(changelog).contains("INSERT INTO risk_rules");
            assertThat(changelog).contains("INSERT INTO risk_assessments");
            assertThat(changelog).contains("'885be553-1447-42fd-b0d3-b8463c7813b4'");
        }
    }

    @Test
    void masterChangelogContainsAdditionalDemoRiskRules() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).contains("--changeset andriy:0005-more-demo-risk-rules");
            assertThat(changelog).contains("Repeated failed card authorizations");
            assertThat(changelog).contains("Unusual merchant category pattern");
            assertThat(changelog).contains("Card-not-present high-value activity");
            assertThat(changelog).contains("High-value international wire");
            assertThat(changelog).contains("New receiver payment velocity");
            assertThat(changelog).contains("Round-amount structuring signal");
            assertThat(changelog).contains("High-value crypto transfer");
            assertThat(changelog).contains("Multiple blockchain destinations");
        }
    }

    @Test
    void masterChangelogRandomizesFirstFiveRiskPlacement() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).contains("--changeset andriy:0014-randomize-demo-risk-placement");
            assertThat(changelog).contains("ORDER BY MD5(tx.transaction_id::TEXT)");
            assertThat(changelog).contains("Distribute existing first-five flagged transactions across each customer's timeline");
        }
    }

    @Test
    void masterChangelogDropsLegacyMarketQuoteTable() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).contains("--changeset andriy:0006-drop-legacy-market-quote");
            assertThat(changelog).contains("DROP TABLE IF EXISTS market_quote");
        }
    }

    @Test
    void masterChangelogRebuildsDemoActivityDataWithCustomerVariation() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).contains("--changeset andriy:0007-randomized-demo-activity-data");
            assertThat(changelog).contains("DELETE FROM transactions");
            assertThat(changelog).contains("MD5('type-' || customer_number || '-' || activity_number)");
            assertThat(changelog).contains("MD5('status-' || customer_number || '-' || activity_number)");
            assertThat(changelog).contains("CASE status_bucket % 12");
            assertThat(changelog).contains("FROM GENERATE_SERIES(1, 100) AS customer_number");
            assertThat(changelog).contains("CROSS JOIN GENERATE_SERIES(1, 100) AS activity_number");
        }
    }

    @Test
    void masterChangelogPersistsAiAnalysisOperatorDisplayName() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).contains("CREATE TABLE ai_analysis_requests");
            assertThat(changelog).contains("requested_by_operator_id UUID REFERENCES operator_users(operator_id)");
            assertThat(changelog).contains("requested_by_operator_display_name VARCHAR(160) NOT NULL DEFAULT 'Unknown operator'");
            assertThat(changelog).contains("--changeset andriy:0012-ai-analysis-request-operator-display-name");
            assertThat(changelog).contains("ADD COLUMN IF NOT EXISTS requested_by_operator_display_name");
        }
    }

    @Test
    void masterChangelogPinsFirstFiveDemoCustomersToKnownRiskBands() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).contains("--changeset andriy:0013-first-five-demo-risk-bands");
            assertThat(changelog).contains("--validCheckSum 9:7ac11a302098d0d0849cb2a53927e55c");
            assertThat(changelog).contains("'005514e6-1ebe-8010-de91-aff66d1d9484'::UUID, 'LOW'");
            assertThat(changelog).contains("'0a3ab26d-12b1-0efc-65d4-a2d6cc72ec67'::UUID, 'LOW'");
            assertThat(changelog).contains("'0abe215d-4832-1215-fe7a-264bfb844be9'::UUID, 'MEDIUM'");
            assertThat(changelog).contains("'0c534877-7dee-ed33-5278-68e39c8fe785'::UUID, 'MEDIUM'");
            assertThat(changelog).contains("'0ddc4d69-0dcf-fba9-15c2-88a68e6665de'::UUID, 'HIGH'");
            assertThat(changelog).contains("WHEN risk_band = 'MEDIUM' THEN row_number <= 3");
            assertThat(changelog).contains("WHEN risk_band = 'HIGH' THEN row_number <= 7");
            assertThat(changelog).contains("DELETE FROM ai_analysis_requests");
        }
    }

    @Test
    void coreRiskBandChangesetDoesNotReferenceAiDatabaseTables() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            String riskBandChangeset = changelog.substring(
                    changelog.indexOf("--changeset andriy:0013-first-five-demo-risk-bands"),
                    changelog.indexOf("--changeset andriy:0014-randomize-demo-risk-placement")
            );

            assertThat(riskBandChangeset).doesNotContain("ai_analysis_");
            assertThat(riskBandChangeset).doesNotContain("risk_indicators");
            assertThat(riskBandChangeset).contains("'c41d73e7-3d8a-4f3d-bab4-b6054cf7957e'::UUID");
            assertThat(riskBandChangeset).contains("'3c8ee0aa-cf16-406d-a0e7-49dbf7466601'::UUID");
            assertThat(riskBandChangeset).doesNotContain("'27a67f38-d581-4015-b738-e468d8736007'::UUID");
            assertThat(riskBandChangeset).doesNotContain("'1d16706c-8c4a-41b4-b99d-2e6f1e566003'::UUID");
        }
    }

    @Test
    void masterChangelogEnsuresEveryDemoCustomerHasFiveFlaggedActivities() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/db/changelog/db.changelog-master.sql")) {
            assertThat(inputStream).isNotNull();

            String changelog = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(changelog).contains("--changeset andriy:0015-minimum-demo-risk-signals");
            assertThat(changelog).contains("customer_signal_counts.flagged_count < 5");
            assertThat(changelog).contains("candidate_number <= 5 - flagged_count");
            assertThat(changelog).contains("minimum-five-flagged-");
            assertThat(changelog).contains("SET risk_indicators = indicator_values.risk_indicators");
        }
    }
}
