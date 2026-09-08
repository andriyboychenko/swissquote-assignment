package com.example.swissquote.infrastructure.ai;

import com.example.swissquote.application.analysis.AiAnalysisGenerator;
import com.example.swissquote.application.analysis.PolicyEvidence;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.analysis.AiAnalysisEvidence;
import com.example.swissquote.domain.analysis.AiAnalysisResult;
import com.example.swissquote.domain.analysis.RiskLevel;
import com.example.swissquote.domain.analysis.RiskSignalSummary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class DeterministicAiAnalysisGenerator implements AiAnalysisGenerator {

    private static final String MODEL_NAME = "local-deterministic-risk-analyzer-v1";
    private static final String PROMPT_VERSION = "risk-analysis-baseline-v1";

    @Override
    public AiAnalysisResult generate(
            UUID analysisRequestId,
            CustomerActivityReport activityReport,
            RiskSignalSummary riskSignalSummary,
            List<PolicyEvidence> policyEvidence,
            Instant generatedAt
    ) {
        RiskLevel riskLevel = toRiskLevel(riskSignalSummary.averageScoreContribution());
        String recommendations = switch (riskLevel) {
            case HIGH -> "Escalate to a senior operator, review recent counterparties, and verify policy evidence before customer contact.";
            case MEDIUM -> "Review the highlighted risk signals, compare with prior activity, and monitor the next customer actions.";
            case LOW -> "No immediate escalation is suggested; keep standard monitoring active.";
        };
        String summary = buildRiskAlertSummary(activityReport, riskSignalSummary, riskLevel, recommendations);
        List<AiAnalysisEvidence> evidence = policyEvidence.stream()
                .map(policy -> new AiAnalysisEvidence(
                        UUID.randomUUID(),
                        "POLICY",
                        policy.sourceReference(),
                        policy.excerpt(),
                        policy.relevanceScore()
                ))
                .toList();

        return new AiAnalysisResult(
                UUID.randomUUID(),
                riskLevel,
                summary,
                recommendations,
                MODEL_NAME,
                PROMPT_VERSION,
                generatedAt,
                evidence
        );
    }

    private static String buildRiskAlertSummary(
            CustomerActivityReport activityReport,
            RiskSignalSummary riskSignalSummary,
            RiskLevel riskLevel,
            String recommendations
    ) {
        String averageRiskScore = riskSignalSummary.averageScoreContribution().setScale(2).toPlainString();

        return """
                RISK ALERT SUMMARY
                Customer ID: %s
                Review Window: Latest %d loaded activities

                1. CONTRIBUTING SIGNALS:
                   - Activity mix: %d card, %d payment, %d crypto activities reviewed
                   - Risk model: Found %d triggered risk signals with average risk score %s.
                   - Highest signal contribution: %s risk score

                2. CUSTOMER IMPACT:
                   - Risk level classified as %s.
                   - %s

                3. RECOMMENDED OPERATOR ACTION:
                   - %s
                """.formatted(
                activityReport.customerId(),
                activityReport.summary().totalActivities(),
                activityReport.summary().cardActivities(),
                activityReport.summary().paymentActivities(),
                activityReport.summary().cryptoActivities(),
                riskSignalSummary.triggeredSignals(),
                averageRiskScore,
                riskSignalSummary.maxScoreContribution().stripTrailingZeros().toPlainString(),
                riskLevel,
                customerImpact(riskLevel),
                recommendations
        ).strip();
    }

    private static String customerImpact(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case HIGH -> "Consider a temporary manual review hold on channels connected to highlighted risk signals.";
            case MEDIUM -> "Continue customer access, but prioritize the highlighted rows before approving unusual follow-up activity.";
            case LOW -> "No customer restriction is suggested; standard monitoring remains sufficient.";
        };
    }

    private static RiskLevel toRiskLevel(BigDecimal averageScore) {
        if (averageScore.compareTo(BigDecimal.valueOf(20)) >= 0) {
            return RiskLevel.HIGH;
        }

        if (averageScore.compareTo(BigDecimal.valueOf(12)) >= 0) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }
}
