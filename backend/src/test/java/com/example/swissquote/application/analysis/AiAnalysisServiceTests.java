package com.example.swissquote.application.analysis;

import com.example.swissquote.application.activity.CustomerActivityService;
import com.example.swissquote.application.auth.OperatorAccountRepository;
import com.example.swissquote.application.auth.ProviderSubjectHasher;
import com.example.swissquote.domain.activity.CustomerActivityPage;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySearchCriteria;
import com.example.swissquote.domain.activity.CustomerActivitySummary;
import com.example.swissquote.domain.analysis.AiAnalysisRequest;
import com.example.swissquote.domain.analysis.AiAnalysisResult;
import com.example.swissquote.domain.analysis.AiAnalysisStatus;
import com.example.swissquote.domain.analysis.RiskLevel;
import com.example.swissquote.domain.analysis.RiskSignalSummary;
import com.example.swissquote.domain.auth.OperatorAccount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAnalysisServiceTests {

    private final AiAnalysisRepository aiAnalysisRepository = mock(AiAnalysisRepository.class);
    private final CustomerActivityService customerActivityService = mock(CustomerActivityService.class);
    private final CustomerRiskSignalRepository customerRiskSignalRepository = mock(CustomerRiskSignalRepository.class);
    private final PolicyKnowledgeRepository policyKnowledgeRepository = mock(PolicyKnowledgeRepository.class);
    private final AiAnalysisGenerator aiAnalysisGenerator = mock(AiAnalysisGenerator.class);
    private final OperatorAccountRepository operatorAccountRepository = mock(OperatorAccountRepository.class);
    private final ProviderSubjectHasher providerSubjectHasher = mock(ProviderSubjectHasher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-05T08:00:00Z"), ZoneOffset.UTC);
    private final AiAnalysisService service = new AiAnalysisService(
            aiAnalysisRepository,
            customerActivityService,
            customerRiskSignalRepository,
            policyKnowledgeRepository,
            aiAnalysisGenerator,
            operatorAccountRepository,
            providerSubjectHasher,
            clock
    );

    @Test
    void requestAnalysisPersistsCompletedAnalysis() {
        UUID customerId = UUID.randomUUID();
        UUID operatorId = UUID.randomUUID();
        CustomerActivityReport report = new CustomerActivityReport(
                customerId,
                new CustomerActivitySummary(1, 1, 0, 0, 0, 0),
                List.of(),
                new CustomerActivityPage(100, 0, 0, false, 0)
        );
        RiskSignalSummary riskSignalSummary = new RiskSignalSummary(1, BigDecimal.TEN, BigDecimal.TEN);
        List<PolicyEvidence> evidence = List.of(new PolicyEvidence("policy://one", "Policy", BigDecimal.ONE));
        AiAnalysisResult result = new AiAnalysisResult(
                UUID.randomUUID(),
                RiskLevel.MEDIUM,
                "Summary",
                "Recommendation",
                "model",
                "prompt",
                Instant.parse("2026-09-05T08:00:00Z"),
                List.of()
        );
        when(providerSubjectHasher.hash("google", "subject")).thenReturn("subject-hash");
        when(operatorAccountRepository.findByProviderAndSubjectHash("google", "subject-hash"))
                .thenReturn(Optional.of(new OperatorAccount(
                        operatorId,
                        "google",
                        "subject-hash",
                        false,
                        null,
                        Instant.parse("2026-09-01T08:00:00Z"),
                        Instant.parse("2026-09-05T08:00:00Z")
                )));
        when(aiAnalysisRepository.save(any(AiAnalysisRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(customerActivityService.getActivityReport(customerId, 100, 0, CustomerActivitySearchCriteria.defaultCriteria()))
                .thenReturn(report);
        when(customerRiskSignalRepository.summarizeForCustomer(customerId)).thenReturn(riskSignalSummary);
        when(policyKnowledgeRepository.retrieveRelevantPolicies(report, riskSignalSummary)).thenReturn(evidence);
        when(aiAnalysisGenerator.generate(any(), any(), any(), any(), any())).thenReturn(result);

        AiAnalysisRequest request = service.requestAnalysis(customerId, "google", "subject", "Demo Operator");

        assertThat(request.status()).isEqualTo(AiAnalysisStatus.COMPLETED);
        assertThat(request.requestedByOperatorId()).isEqualTo(operatorId);
        assertThat(request.requestedByOperatorDisplayName()).isEqualTo("Demo Operator");
        assertThat(request.result()).isEqualTo(result);
        verify(aiAnalysisGenerator).generate(
                request.analysisRequestId(),
                report,
                riskSignalSummary,
                evidence,
                Instant.parse("2026-09-05T08:00:00Z")
        );
    }

    @Test
    void findCustomerAnalysesDelegatesToRepository() {
        UUID customerId = UUID.randomUUID();
        when(aiAnalysisRepository.findByCustomerId(customerId)).thenReturn(List.of());

        assertThat(service.findCustomerAnalyses(customerId)).isEmpty();
    }
}
