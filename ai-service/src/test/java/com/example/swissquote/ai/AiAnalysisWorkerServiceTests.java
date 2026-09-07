package com.example.swissquote.ai;

import com.example.swissquote.application.analysis.AiAnalysisGenerator;
import com.example.swissquote.application.analysis.AiAnalysisRepository;
import com.example.swissquote.application.analysis.AiAnalysisSnapshot;
import com.example.swissquote.application.analysis.PolicyEvidence;
import com.example.swissquote.application.analysis.PolicyKnowledgeRepository;
import com.example.swissquote.domain.activity.CustomerActivitySummary;
import com.example.swissquote.domain.analysis.AiAnalysisRequest;
import com.example.swissquote.domain.analysis.AiAnalysisResult;
import com.example.swissquote.domain.analysis.AiAnalysisStatus;
import com.example.swissquote.domain.analysis.RiskLevel;
import com.example.swissquote.domain.analysis.RiskSignalSummary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiAnalysisWorkerServiceTests {

    private final AiAnalysisRepository repository = mock(AiAnalysisRepository.class);
    private final PolicyKnowledgeRepository policies = mock(PolicyKnowledgeRepository.class);
    private final AiAnalysisGenerator generator = mock(AiAnalysisGenerator.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-07T10:00:00Z"), ZoneOffset.UTC);
    private final AiAnalysisWorkerService service = new AiAnalysisWorkerService(repository, policies, generator, clock);

    @Test
    void generatesAndPersistsAnalysisFromCoreSnapshot() {
        UUID customerId = UUID.randomUUID();
        AiAnalysisSnapshot snapshot = new AiAnalysisSnapshot(
                customerId,
                new CustomerActivitySummary(4, 1, 2, 1, 0, 1),
                new RiskSignalSummary(1, BigDecimal.TEN, BigDecimal.TEN)
        );
        AiAnalysisResult result = new AiAnalysisResult(
                UUID.randomUUID(), RiskLevel.MEDIUM, "Summary", "Recommendation", "model", "prompt",
                clock.instant(), List.of()
        );
        when(repository.save(any(AiAnalysisRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(policies.retrieveRelevantPolicies(any(), any())).thenReturn(List.of(
                new PolicyEvidence("policy://review", "Review", BigDecimal.ONE)
        ));
        when(generator.generate(any(), any(), any(), any(), any())).thenReturn(result);

        AiAnalysisRequest request = service.requestAnalysis(snapshot, "Sarah Connor");

        assertThat(request.status()).isEqualTo(AiAnalysisStatus.COMPLETED);
        assertThat(request.customerId()).isEqualTo(customerId);
        assertThat(request.requestedByOperatorDisplayName()).isEqualTo("Sarah Connor");
        assertThat(request.result()).isEqualTo(result);
    }
}
