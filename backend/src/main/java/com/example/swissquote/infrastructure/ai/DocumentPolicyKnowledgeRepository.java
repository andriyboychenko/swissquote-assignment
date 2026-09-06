package com.example.swissquote.infrastructure.ai;

import com.example.swissquote.application.analysis.PolicyEvidence;
import com.example.swissquote.application.analysis.PolicyKnowledgeRepository;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySummary;
import com.example.swissquote.domain.analysis.RiskSignalSummary;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DocumentPolicyKnowledgeRepository implements PolicyKnowledgeRepository {

    private static final PolicySection OPERATOR_REVIEW = new PolicySection(
            "policy://policies/customer-activity-risk-review-v1.md#operator-review-triggers",
            "Open an operator risk review when a customer shows failed, reversed, high-value, or unusual cross-channel activity in the same review window.",
            BigDecimal.valueOf(0.9400)
    );
    private static final PolicySection STANDARD_MONITORING = new PolicySection(
            "policy://policies/customer-activity-risk-review-v1.md#standard-monitoring",
            "No triggered risk signals means the analysis should avoid escalation language and recommend standard monitoring unless new activity changes the risk context.",
            BigDecimal.valueOf(0.9100)
    );
    private static final PolicySection ACTIVITY_OVERVIEW = new PolicySection(
            "policy://policies/customer-activity-risk-review-v1.md#activity-overview",
            "The overview should group activity by card, payment, and crypto channels and highlight status, amount, counterparty, date, and known decline details.",
            BigDecimal.valueOf(0.9000)
    );
    private static final PolicySection CRYPTO_MONITORING = new PolicySection(
            "policy://policies/crypto-asset-monitoring-v1.md#wallet-and-exchange-review",
            "Crypto activity should be reviewed for wallet destination, exchange involvement, transaction velocity, and links to failed or reversed fiat activity.",
            BigDecimal.valueOf(0.8800)
    );
    private static final PolicySection PAYMENT_REVIEW = new PolicySection(
            "policy://policies/payment-cross-border-review-v1.md#counterparty-and-country-checks",
            "Payment activity should be checked for unfamiliar counterparties, repeated receiver accounts, cross-border bank countries, and value spikes.",
            BigDecimal.valueOf(0.8600)
    );
    private static final PolicySection RISK_SIGNAL_HANDLING = new PolicySection(
            "policy://policies/risk-signal-handling-v1.md#triggered-rule-response",
            "Triggered risk rules should be summarized with the contributing signals, customer impact, and the next recommended operator action.",
            BigDecimal.valueOf(0.9200)
    );

    @Override
    public List<PolicyEvidence> retrieveRelevantPolicies(
            CustomerActivityReport activityReport,
            RiskSignalSummary riskSignalSummary
    ) {
        CustomerActivitySummary summary = activityReport.summary();
        if (riskSignalSummary.triggeredSignals() == 0) {
            return List.of(STANDARD_MONITORING.toEvidence());
        }

        List<PolicySection> sections = new ArrayList<>();
        sections.add(OPERATOR_REVIEW);
        sections.add(ACTIVITY_OVERVIEW);

        if (summary.cryptoActivities() > 0) {
            sections.add(CRYPTO_MONITORING);
        }

        if (summary.paymentActivities() > 0) {
            sections.add(PAYMENT_REVIEW);
        }

        if (riskSignalSummary.triggeredSignals() > 0) {
            sections.add(RISK_SIGNAL_HANDLING);
        }

        return sections.stream()
                .map(PolicySection::toEvidence)
                .toList();
    }

    private record PolicySection(
            String sourceReference,
            String excerpt,
            BigDecimal relevanceScore
    ) {

        private PolicyEvidence toEvidence() {
            return new PolicyEvidence(sourceReference, excerpt, relevanceScore);
        }
    }
}
