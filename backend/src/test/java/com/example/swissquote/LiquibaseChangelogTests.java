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
}
