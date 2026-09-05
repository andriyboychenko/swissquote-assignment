package com.example.swissquote.interfaces.rest;

import com.example.swissquote.domain.activity.CustomerActivity;
import com.example.swissquote.domain.activity.CustomerActivityPage;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySummary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CustomerActivityReportResponse(
        UUID customerId,
        CustomerActivitySummaryResponse summary,
        List<CustomerActivityResponse> activities,
        CustomerActivityPageResponse page
) {

    static CustomerActivityReportResponse fromDomain(CustomerActivityReport report) {
        return new CustomerActivityReportResponse(
                report.customerId(),
                CustomerActivitySummaryResponse.fromDomain(report.summary()),
                report.activities().stream().map(CustomerActivityResponse::fromDomain).toList(),
                CustomerActivityPageResponse.fromDomain(report.page())
        );
    }

    public record CustomerActivitySummaryResponse(
            int totalActivities,
            int cardActivities,
            int paymentActivities,
            int cryptoActivities,
            int failedActivities,
            int pendingActivities
    ) {

        static CustomerActivitySummaryResponse fromDomain(CustomerActivitySummary summary) {
            return new CustomerActivitySummaryResponse(
                    summary.totalActivities(),
                    summary.cardActivities(),
                    summary.paymentActivities(),
                    summary.cryptoActivities(),
                    summary.failedActivities(),
                    summary.pendingActivities()
            );
        }
    }

    public record CustomerActivityPageResponse(
            int limit,
            int offset,
            int returnedActivities,
            boolean hasMore,
            int nextOffset
    ) {

        static CustomerActivityPageResponse fromDomain(CustomerActivityPage page) {
            return new CustomerActivityPageResponse(
                    page.limit(),
                    page.offset(),
                    page.returnedActivities(),
                    page.hasMore(),
                    page.nextOffset()
            );
        }
    }

    public record CustomerActivityResponse(
            UUID transactionId,
            String activityType,
            BigDecimal amount,
            String currency,
            String status,
            Instant createdAt,
            String counterparty,
            String channel,
            String detail,
            List<ActivityRiskIndicatorResponse> riskIndicators
    ) {

        static CustomerActivityResponse fromDomain(CustomerActivity activity) {
            return new CustomerActivityResponse(
                    activity.transactionId(),
                    activity.activityType().name(),
                    activity.amount(),
                    activity.currency(),
                    activity.status(),
                    activity.createdAt(),
                    activity.counterparty(),
                    activity.channel(),
                    activity.detail(),
                    activity.riskIndicators().stream()
                            .map(ActivityRiskIndicatorResponse::fromDomain)
                            .toList()
            );
        }
    }

    public record ActivityRiskIndicatorResponse(
            String ruleName,
            String severity,
            BigDecimal scoreContribution
    ) {

        static ActivityRiskIndicatorResponse fromDomain(
                com.example.swissquote.domain.activity.ActivityRiskIndicator indicator
        ) {
            return new ActivityRiskIndicatorResponse(
                    indicator.ruleName(),
                    indicator.severity(),
                    indicator.scoreContribution()
            );
        }
    }
}
