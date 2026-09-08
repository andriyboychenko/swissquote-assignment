package com.example.swissquote.infrastructure.persistence;

import com.example.swissquote.application.activity.CustomerActivityRepository;
import com.example.swissquote.domain.activity.ActivityType;
import com.example.swissquote.domain.activity.ActivityRiskIndicator;
import com.example.swissquote.domain.activity.CustomerActivity;
import com.example.swissquote.domain.activity.CustomerActivityPage;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySearchCriteria;
import com.example.swissquote.domain.activity.CustomerActivitySummary;
import com.example.swissquote.domain.activity.SortDirection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class JdbcCustomerActivityQueryAdapter implements CustomerActivityRepository {

    private static final String ACTIVITY_DETAIL_SQL = """
            CASE t.activity_type
                WHEN 'CARD' THEN 'PAN ' || ca.card_pan || ', MCC ' || ca.mcc_code ||
                    CASE
                        WHEN NULLIF(TRIM(ca.decline_reason), '') IS NULL THEN ''
                        ELSE ', Decline: ' || ca.decline_reason
                    END
                WHEN 'PAYMENT' THEN 'Receiver country ' || pa.receiver_bank_country
                ELSE 'Exchange ' || COALESCE(cra.exchange_name, 'direct wallet')
            END
            """;
    private static final String CUSTOMER_ACTIVITIES_SQL = """
            SELECT
                t.transaction_id,
                t.activity_type,
                t.amount,
                t.currency,
                t.status,
                t.created_at,
                COALESCE(ca.merchant_name, pa.receiver_account, cra.wallet_address_to) AS counterparty,
                CASE t.activity_type
                    WHEN 'CARD' THEN ca.card_type
                    WHEN 'PAYMENT' THEN pa.payment_method
                    ELSE cra.blockchain
                END AS channel,
                %s AS detail,
                COALESCE(t.risk_indicators, '[]'::jsonb)::TEXT AS risk_indicators
            FROM transactions t
            LEFT JOIN card_activity ca ON ca.transaction_id = t.transaction_id
            LEFT JOIN payment_activity pa ON pa.transaction_id = t.transaction_id
            LEFT JOIN crypto_activity cra ON cra.transaction_id = t.transaction_id
            %s
            %s
            LIMIT :limit
            OFFSET :offset
            """;
    private static final String CUSTOMER_SUMMARY_SQL = """
            SELECT
                COUNT(*)::INTEGER AS total_activities,
                COALESCE(SUM(CASE WHEN t.activity_type = 'CARD' THEN 1 ELSE 0 END), 0)::INTEGER AS card_activities,
                COALESCE(SUM(CASE WHEN t.activity_type = 'PAYMENT' THEN 1 ELSE 0 END), 0)::INTEGER AS payment_activities,
                COALESCE(SUM(CASE WHEN t.activity_type = 'CRYPTO' THEN 1 ELSE 0 END), 0)::INTEGER AS crypto_activities,
                COALESCE(SUM(CASE WHEN t.status = 'Failed' THEN 1 ELSE 0 END), 0)::INTEGER AS failed_activities,
                COALESCE(SUM(CASE WHEN t.status = 'Pending' THEN 1 ELSE 0 END), 0)::INTEGER AS pending_activities
            FROM transactions t
            LEFT JOIN card_activity ca ON ca.transaction_id = t.transaction_id
            LEFT JOIN payment_activity pa ON pa.transaction_id = t.transaction_id
            LEFT JOIN crypto_activity cra ON cra.transaction_id = t.transaction_id
            WHERE t.customer_id = :customerId
            """;
    private static final String FILTERED_CUSTOMER_SUMMARY_SQL = """
            SELECT
                COUNT(*)::INTEGER AS total_activities,
                COALESCE(SUM(CASE WHEN t.activity_type = 'CARD' THEN 1 ELSE 0 END), 0)::INTEGER AS card_activities,
                COALESCE(SUM(CASE WHEN t.activity_type = 'PAYMENT' THEN 1 ELSE 0 END), 0)::INTEGER AS payment_activities,
                COALESCE(SUM(CASE WHEN t.activity_type = 'CRYPTO' THEN 1 ELSE 0 END), 0)::INTEGER AS crypto_activities,
                COALESCE(SUM(CASE WHEN t.status = 'Failed' THEN 1 ELSE 0 END), 0)::INTEGER AS failed_activities,
                COALESCE(SUM(CASE WHEN t.status = 'Pending' THEN 1 ELSE 0 END), 0)::INTEGER AS pending_activities
            FROM transactions t
            LEFT JOIN card_activity ca ON ca.transaction_id = t.transaction_id
            LEFT JOIN payment_activity pa ON pa.transaction_id = t.transaction_id
            LEFT JOIN crypto_activity cra ON cra.transaction_id = t.transaction_id
            %s
            """;
    private static final RowMapper<CustomerActivity> ACTIVITY_ROW_MAPPER = new CustomerActivityRowMapper();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> RISK_INDICATORS_TYPE = new TypeReference<>() {
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcCustomerActivityQueryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CustomerActivityReport findActivityReport(
            UUID customerId,
            int limit,
            int offset,
            CustomerActivitySearchCriteria criteria
    ) {
        MapSqlParameterSource parameters = buildParameters(customerId, limit, offset, criteria);
        String filterSql = toFilterSql(criteria);
        String sortSql = toOrderBySql(criteria);

        List<CustomerActivity> activities = jdbcTemplate.query(
                CUSTOMER_ACTIVITIES_SQL.formatted(ACTIVITY_DETAIL_SQL, filterSql, sortSql),
                parameters,
                ACTIVITY_ROW_MAPPER
        );
        CustomerActivitySummary filteredSummary = jdbcTemplate.queryForObject(
                FILTERED_CUSTOMER_SUMMARY_SQL.formatted(filterSql),
                parameters,
                JdbcCustomerActivityQueryAdapter::mapSummary
        );
        CustomerActivitySummary summary = jdbcTemplate.queryForObject(
                CUSTOMER_SUMMARY_SQL,
                new MapSqlParameterSource("customerId", customerId),
                JdbcCustomerActivityQueryAdapter::mapSummary
        );
        CustomerActivityPage page = new CustomerActivityPage(
                limit,
                offset,
                activities.size(),
                offset + activities.size() < filteredSummary.totalActivities(),
                offset + activities.size()
        );
        return new CustomerActivityReport(customerId, summary, activities, page);
    }

    private static String toFilterSql(CustomerActivitySearchCriteria criteria) {
        var filter = criteria.filter();
        List<String> predicates = new ArrayList<>();
        predicates.add("t.customer_id = :customerId");

        if (filter.createdFrom() != null) {
            predicates.add("t.created_at >= :createdFrom");
        }

        if (filter.createdTo() != null) {
            predicates.add("t.created_at <= :createdTo");
        }

        if (filter.activityType() != null) {
            predicates.add("t.activity_type = CAST(:activityType AS activity_type)");
        }

        if (blankToNull(filter.status()) != null) {
            predicates.add("t.status = :status");
        }

        if (filter.amountMin() != null) {
            predicates.add("t.amount >= :amountMin");
        }

        if (filter.amountMax() != null) {
            predicates.add("t.amount <= :amountMax");
        }

        if (blankToNull(filter.currency()) != null) {
            predicates.add("t.currency = :currency");
        }

        if (containsPattern(filter.counterparty()) != null) {
            predicates.add("LOWER(COALESCE(ca.merchant_name, pa.receiver_account, cra.wallet_address_to)) LIKE :counterparty");
        }

        if (containsPattern(filter.channel()) != null) {
            predicates.add("LOWER(CASE t.activity_type WHEN 'CARD' THEN ca.card_type WHEN 'PAYMENT' THEN pa.payment_method ELSE cra.blockchain END) LIKE :channel");
        }

        if (containsPattern(filter.detail()) != null) {
            predicates.add("LOWER(" + ACTIVITY_DETAIL_SQL + ") LIKE :detail");
        }

        if (filter.riskOnly()) {
            predicates.add("t.risk_indicators <> '[]'::jsonb");
        }

        return "WHERE " + String.join("\n  AND ", predicates);
    }

    private static MapSqlParameterSource buildParameters(
            UUID customerId,
            int limit,
            int offset,
            CustomerActivitySearchCriteria criteria
    ) {
        var filter = criteria.filter();
        MapSqlParameterSource parameters = new MapSqlParameterSource("customerId", customerId)
                .addValue("limit", limit)
                .addValue("offset", offset);

        addOptional(parameters, "createdFrom", filter.createdFrom());
        addOptional(parameters, "createdTo", filter.createdTo());
        addOptional(parameters, "activityType", filter.activityType() == null ? null : filter.activityType().name());
        addOptional(parameters, "status", blankToNull(filter.status()));
        addOptional(parameters, "amountMin", filter.amountMin());
        addOptional(parameters, "amountMax", filter.amountMax());
        addOptional(parameters, "currency", blankToNull(filter.currency()));
        addOptional(parameters, "counterparty", containsPattern(filter.counterparty()));
        addOptional(parameters, "channel", containsPattern(filter.channel()));
        addOptional(parameters, "detail", containsPattern(filter.detail()));
        return parameters;
    }

    private static void addOptional(MapSqlParameterSource parameters, String name, Object value) {
        if (value != null) {
            parameters.addValue(name, value);
        }
    }

    private static String toOrderBySql(CustomerActivitySearchCriteria criteria) {
        String direction = criteria.sort().direction() == SortDirection.ASC ? "ASC" : "DESC";
        String sortColumn = switch (criteria.sort().sortBy()) {
            case "transactionId" -> "t.transaction_id";
            case "activityType" -> "t.activity_type";
            case "status" -> "t.status";
            case "amount" -> "t.amount";
            case "currency" -> "t.currency";
            case "counterparty" -> "counterparty";
            case "channel" -> "channel";
            case "detail" -> "detail";
            case "createdAt" -> "t.created_at";
            default -> "t.created_at";
        };

        return "ORDER BY " + sortColumn + " " + direction + ", t.transaction_id " + direction;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String containsPattern(String value) {
        String normalizedValue = blankToNull(value);
        return normalizedValue == null ? null : "%" + normalizedValue.toLowerCase() + "%";
    }

    private static CustomerActivitySummary mapSummary(ResultSet resultSet, int rowNumber) throws SQLException {
        return new CustomerActivitySummary(
                resultSet.getInt("total_activities"),
                resultSet.getInt("card_activities"),
                resultSet.getInt("payment_activities"),
                resultSet.getInt("crypto_activities"),
                resultSet.getInt("failed_activities"),
                resultSet.getInt("pending_activities")
        );
    }

    private static final class CustomerActivityRowMapper implements RowMapper<CustomerActivity> {

        @Override
        public CustomerActivity mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
            Timestamp createdAt = resultSet.getTimestamp("created_at");
            return new CustomerActivity(
                    resultSet.getObject("transaction_id", UUID.class),
                    ActivityType.valueOf(resultSet.getString("activity_type")),
                    resultSet.getBigDecimal("amount"),
                    resultSet.getString("currency"),
                    resultSet.getString("status"),
                    createdAt.toInstant(),
                    resultSet.getString("counterparty"),
                    resultSet.getString("channel"),
                    resultSet.getString("detail"),
                    parseRiskIndicators(resultSet.getString("risk_indicators"))
            );
        }
    }

    private static List<ActivityRiskIndicator> parseRiskIndicators(String value) {
        try {
            return OBJECT_MAPPER.readValue(value == null ? "[]" : value, RISK_INDICATORS_TYPE)
                    .stream()
                    .map(JdbcCustomerActivityQueryAdapter::toRiskIndicator)
                    .toList();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not parse transaction risk indicators", exception);
        }
    }

    private static ActivityRiskIndicator toRiskIndicator(Map<String, Object> values) {
        return new ActivityRiskIndicator(
                String.valueOf(values.get("ruleName")),
                String.valueOf(values.get("severity")),
                new BigDecimal(String.valueOf(values.get("scoreContribution")))
        );
    }
}
