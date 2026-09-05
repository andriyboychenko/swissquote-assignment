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
                "PAN ****1234, MCC 5411, Decline: Insufficient funds"
        );
        CustomerActivitySummary summary = new CustomerActivitySummary(1, 1, 0, 0, 0, 0);
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
        )).thenReturn(summary);
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
                                "PAN"
                        ),
                        new CustomerActivitySort("amount", SortDirection.ASC)
                )
        );

        ArgumentCaptor<MapSqlParameterSource> parametersCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(
                contains("ca.decline_reason"),
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
        assertThat(report.summary()).isEqualTo(summary);
    }
}
