package com.example.swissquote.infrastructure.persistence;

import com.example.swissquote.domain.activity.ActivityType;
import com.example.swissquote.domain.activity.CustomerActivity;
import com.example.swissquote.domain.activity.CustomerActivityFilter;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySearchCriteria;
import com.example.swissquote.domain.activity.CustomerActivitySort;
import com.example.swissquote.domain.activity.CustomerActivitySummary;
import com.example.swissquote.domain.activity.SortDirection;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcCustomerActivityQueryAdapterTests {

    @Test
    void findActivityReportAppliesFiltersAndSafeSorting() {
        UUID customerId = UUID.randomUUID();
        CustomerActivity activity = new CustomerActivity(
                UUID.randomUUID(),
                ActivityType.CARD,
                BigDecimal.valueOf(125.50),
                "CHF",
                "Completed",
                Instant.parse("2026-09-04T12:00:00Z"),
                "Merchant 001",
                "Credit",
                "PAN ****1234, MCC 5411, Decline: Insufficient funds",
                List.of()
        );
        CustomerActivitySummary filteredSummary = new CustomerActivitySummary(1, 1, 0, 0, 0, 0);
        CustomerActivitySummary customerSummary = new CustomerActivitySummary(100, 39, 31, 30, 5, 12);
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        when(jdbcTemplate.query(
                contains("ORDER BY t.amount ASC, t.transaction_id ASC"),
                any(MapSqlParameterSource.class),
                org.mockito.ArgumentMatchers.<RowMapper<CustomerActivity>>any()
        )).thenReturn(List.of(activity));
        when(jdbcTemplate.queryForObject(
                contains("LOWER(COALESCE(ca.merchant_name, pa.receiver_account, cra.wallet_address_to)) LIKE :counterparty"),
                any(MapSqlParameterSource.class),
                org.mockito.ArgumentMatchers.<RowMapper<CustomerActivitySummary>>any()
        )).thenReturn(filteredSummary);
        when(jdbcTemplate.queryForObject(
                contains("WHERE t.customer_id = :customerId"),
                any(MapSqlParameterSource.class),
                org.mockito.ArgumentMatchers.<RowMapper<CustomerActivitySummary>>any()
        )).thenReturn(customerSummary);
        JdbcCustomerActivityQueryAdapter adapter = new JdbcCustomerActivityQueryAdapter(jdbcTemplate);

        CustomerActivityReport report = adapter.findActivityReport(
                customerId,
                50,
                0,
                new CustomerActivitySearchCriteria(
                        new CustomerActivityFilter(
                                Instant.parse("2026-09-01T00:00:00Z"),
                                Instant.parse("2026-09-05T00:00:00Z"),
                                ActivityType.CARD,
                                "Completed",
                                BigDecimal.TEN,
                                BigDecimal.valueOf(200),
                                "CHF",
                                "Merchant",
                                "Credit",
                                "PAN",
                                true
                        ),
                        new CustomerActivitySort("amount", SortDirection.ASC)
                )
        );

        ArgumentCaptor<MapSqlParameterSource> parametersCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(
                contains("t.risk_indicators <> '[]'::jsonb"),
                parametersCaptor.capture(),
                org.mockito.ArgumentMatchers.<RowMapper<CustomerActivity>>any()
        );
        MapSqlParameterSource parameters = parametersCaptor.getValue();
        assertThat(parameters.getValue("customerId")).isEqualTo(customerId);
        assertThat(parameters.getValue("limit")).isEqualTo(50);
        assertThat(parameters.getValue("activityType")).isEqualTo("CARD");
        assertThat(parameters.getValue("status")).isEqualTo("Completed");
        assertThat(parameters.getValue("amountMin")).isEqualTo(BigDecimal.TEN);
        assertThat(parameters.getValue("currency")).isEqualTo("CHF");
        assertThat(parameters.getValue("counterparty")).isEqualTo("%merchant%");
        assertThat(report.activities()).containsExactly(activity);
        assertThat(report.summary()).isEqualTo(customerSummary);
    }

    @Test
    void findActivityReportMapsRiskIndicatorsFromJsonMetadata() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        CustomerActivitySummary filteredSummary = new CustomerActivitySummary(1, 1, 0, 0, 0, 0);
        CustomerActivitySummary customerSummary = new CustomerActivitySummary(100, 39, 31, 30, 5, 12);
        ArgumentCaptor<RowMapper<CustomerActivity>> rowMapperCaptor = ArgumentCaptor.captor();
        when(jdbcTemplate.query(
                contains("risk_indicators"),
                any(MapSqlParameterSource.class),
                rowMapperCaptor.capture()
        )).thenAnswer(invocation -> List.of(rowMapperCaptor.getValue().mapRow(resultSet(transactionId), 0)));
        when(jdbcTemplate.queryForObject(
                contains("COUNT(*)::INTEGER AS total_activities"),
                any(MapSqlParameterSource.class),
                org.mockito.ArgumentMatchers.<RowMapper<CustomerActivitySummary>>any()
        )).thenReturn(filteredSummary);
        when(jdbcTemplate.queryForObject(
                contains("WHERE t.customer_id = :customerId"),
                any(MapSqlParameterSource.class),
                org.mockito.ArgumentMatchers.<RowMapper<CustomerActivitySummary>>any()
        )).thenReturn(customerSummary);
        JdbcCustomerActivityQueryAdapter adapter = new JdbcCustomerActivityQueryAdapter(jdbcTemplate);

        CustomerActivityReport report = adapter.findActivityReport(
                customerId,
                50,
                0,
                CustomerActivitySearchCriteria.defaultCriteria()
        );

        assertThat(report.activities().getFirst().riskIndicators()).hasSize(1);
        assertThat(report.activities().getFirst().riskIndicators().getFirst().ruleName())
                .isEqualTo("High-value card transaction");
        assertThat(report.activities().getFirst().riskIndicators().getFirst().severity()).isEqualTo("HIGH");
        assertThat(report.activities().getFirst().riskIndicators().getFirst().scoreContribution())
                .isEqualByComparingTo("20.00");
    }

    private static ResultSet resultSet(UUID transactionId) throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("transaction_id", UUID.class)).thenReturn(transactionId);
        when(resultSet.getString("activity_type")).thenReturn("CARD");
        when(resultSet.getBigDecimal("amount")).thenReturn(BigDecimal.valueOf(125.50));
        when(resultSet.getString("currency")).thenReturn("CHF");
        when(resultSet.getString("status")).thenReturn("Completed");
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.parse("2026-09-04T12:00:00Z")));
        when(resultSet.getString("counterparty")).thenReturn("Merchant 001");
        when(resultSet.getString("channel")).thenReturn("Credit");
        when(resultSet.getString("detail")).thenReturn("PAN ****1234, MCC 5411");
        when(resultSet.getString("risk_indicators")).thenReturn("""
                [
                  {
                    "ruleName": "High-value card transaction",
                    "severity": "HIGH",
                    "scoreContribution": 20.00
                  }
                ]
                """);
        return resultSet;
    }
}
