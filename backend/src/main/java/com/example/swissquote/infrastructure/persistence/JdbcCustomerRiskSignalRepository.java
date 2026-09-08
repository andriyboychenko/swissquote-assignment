package com.example.swissquote.infrastructure.persistence;

import com.example.swissquote.application.analysis.CustomerRiskSignalRepository;
import com.example.swissquote.domain.analysis.RiskSignalSummary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public class JdbcCustomerRiskSignalRepository implements CustomerRiskSignalRepository {

    private static final String SUMMARY_SQL = """
            SELECT
                COUNT(assessment.assessment_id)::INTEGER AS triggered_signals,
                COALESCE(ROUND(AVG(assessment.score_contribution), 2), 0)::NUMERIC AS average_score_contribution,
                COALESCE(MAX(assessment.score_contribution), 0)::NUMERIC AS max_score_contribution
            FROM risk_assessments assessment
            JOIN transactions tx ON tx.transaction_id = assessment.transaction_id
            WHERE tx.customer_id = :customerId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcCustomerRiskSignalRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RiskSignalSummary summarizeForCustomer(UUID customerId) {
        return jdbcTemplate.queryForObject(
                SUMMARY_SQL,
                new MapSqlParameterSource("customerId", customerId),
                (resultSet, rowNumber) -> new RiskSignalSummary(
                        resultSet.getInt("triggered_signals"),
                        resultSet.getBigDecimal("average_score_contribution"),
                        resultSet.getBigDecimal("max_score_contribution")
                )
        );
    }
}
