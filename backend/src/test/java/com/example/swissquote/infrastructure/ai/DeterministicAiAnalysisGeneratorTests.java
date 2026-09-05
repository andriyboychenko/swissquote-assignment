package com.example.swissquote.infrastructure.ai;

import com.example.swissquote.application.analysis.PolicyEvidence;
import com.example.swissquote.domain.activity.CustomerActivityPage;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySummary;
import com.example.swissquote.domain.analysis.RiskLevel;
import com.example.swissquote.domain.analysis.RiskSignalSummary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicAiAnalysisGeneratorTests {

    private final DeterministicAiAnalysisGenerator generator = new DeterministicAiAnalysisGenerator();

    @Test
    void generateReturnsStructuredHighRiskAnalysisWithEvidence() {
        CustomerActivityReport report = new CustomerActivityReport(
                UUID.randomUUID(),
                new CustomerActivitySummary(100, 30, 30, 40, 8, 7),
                List.of(),
                new CustomerActivityPage(100, 0, 0, false, 0)
        );

        var result = generator.generate(
                UUID.randomUUID(),
                report,
                new RiskSignalSummary(9, BigDecimal.valueOf(130), BigDecimal.valueOf(25)),
                List.of(new PolicyEvidence("policy://one", "Review high risk activity.", BigDecimal.valueOf(0.9))),
                Instant.parse("2026-09-05T08:00:00Z")
        );

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(result.summary()).contains("RISK ALERT SUMMARY");
        assertThat(result.summary()).doesNotContain("[RISK ALERT SUMMARY]");
        assertThat(result.summary()).contains("Review Window: Latest 100 loaded activities");
        assertThat(result.summary()).contains("1. CONTRIBUTING SIGNALS:");
        assertThat(result.summary()).contains("Activity mix: 30 card, 30 payment, 40 crypto activities reviewed");
        assertThat(result.summary()).contains("Found 9 triggered risk signals with total risk score 130.");
        assertThat(result.summary()).contains("2. CUSTOMER IMPACT:");
        assertThat(result.summary()).contains("Risk level classified as HIGH.");
        assertThat(result.summary()).contains("Consider a temporary manual review hold on channels connected to highlighted risk signals.");
        assertThat(result.summary()).doesNotContain("No automated channel hold was applied by this demo workflow.");
        assertThat(result.summary()).contains("3. RECOMMENDED OPERATOR ACTION:");
        assertThat(result.recommendations()).contains("Escalate");
        assertThat(result.modelName()).isEqualTo("local-deterministic-risk-analyzer-v1");
        assertThat(result.evidence()).hasSize(1);
        assertThat(result.evidence().getFirst().sourceReference()).isEqualTo("policy://one");
    }
}
