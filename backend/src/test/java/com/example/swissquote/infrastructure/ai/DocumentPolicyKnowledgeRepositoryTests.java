package com.example.swissquote.infrastructure.ai;

import com.example.swissquote.domain.activity.CustomerActivityPage;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySummary;
import com.example.swissquote.domain.analysis.RiskSignalSummary;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentPolicyKnowledgeRepositoryTests {

    private final DocumentPolicyKnowledgeRepository repository = new DocumentPolicyKnowledgeRepository();

    @Test
    void retrieveRelevantPoliciesIncludesSourceReferencesForMatchingPolicyDocuments() {
        CustomerActivityReport report = new CustomerActivityReport(
                UUID.randomUUID(),
                new CustomerActivitySummary(10, 2, 3, 5, 1, 1),
                List.of(),
                new CustomerActivityPage(50, 0, 0, false, 0)
        );

        var evidence = repository.retrieveRelevantPolicies(
                report,
                new RiskSignalSummary(2, BigDecimal.TEN, BigDecimal.TEN)
        );

        assertThat(evidence).extracting("sourceReference")
                .contains(
                        "policy://policies/customer-activity-risk-review-v1.md#operator-review-triggers",
                        "policy://policies/customer-activity-risk-review-v1.md#activity-overview",
                        "policy://policies/crypto-asset-monitoring-v1.md#wallet-and-exchange-review",
                        "policy://policies/payment-cross-border-review-v1.md#counterparty-and-country-checks",
                        "policy://policies/risk-signal-handling-v1.md#triggered-rule-response"
                );
    }

    @Test
    void retrieveRelevantPoliciesUsesStandardMonitoringPolicyWhenSignalsAreAbsent() {
        CustomerActivityReport report = new CustomerActivityReport(
                UUID.randomUUID(),
                new CustomerActivitySummary(10, 3, 4, 3, 0, 0),
                List.of(),
                new CustomerActivityPage(50, 0, 0, false, 0)
        );

        var evidence = repository.retrieveRelevantPolicies(
                report,
                new RiskSignalSummary(0, BigDecimal.ZERO, BigDecimal.ZERO)
        );

        assertThat(evidence).extracting("sourceReference")
                .containsExactly(
                        "policy://policies/customer-activity-risk-review-v1.md#standard-monitoring"
                );
        assertThat(evidence.getFirst().excerpt()).contains("No triggered risk signals");
    }

    @Test
    void retrieveRelevantPoliciesReferencesBundledPolicySections() throws Exception {
        CustomerActivityReport report = new CustomerActivityReport(
                UUID.randomUUID(),
                new CustomerActivitySummary(10, 2, 3, 5, 1, 1),
                List.of(),
                new CustomerActivityPage(50, 0, 0, false, 0)
        );

        var evidence = repository.retrieveRelevantPolicies(
                report,
                new RiskSignalSummary(2, BigDecimal.TEN, BigDecimal.TEN)
        );

        for (var policyEvidence : evidence) {
            PolicyLocation location = PolicyLocation.fromReference(policyEvidence.sourceReference());
            ClassPathResource resource = new ClassPathResource(location.resourcePath());
            assertThat(resource.exists()).isTrue();
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            assertThat(content).contains("## " + location.sectionAnchor());
            assertThat(content).contains(policyEvidence.excerpt());
        }
    }

    private record PolicyLocation(String resourcePath, String sectionAnchor) {

        private static PolicyLocation fromReference(String sourceReference) {
            String normalizedReference = sourceReference.replace("policy://", "");
            String[] parts = normalizedReference.split("#", 2);
            return new PolicyLocation(parts[0], parts[1]);
        }
    }
}
