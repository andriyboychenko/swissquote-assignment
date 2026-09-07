package com.example.swissquote.ai;

import com.example.swissquote.application.analysis.AiAnalysisSnapshot;
import com.example.swissquote.application.analysis.RemoteAiAnalysisRequest;
import com.example.swissquote.domain.activity.CustomerActivitySummary;
import com.example.swissquote.domain.analysis.AiAnalysisRequest;
import com.example.swissquote.domain.analysis.AiAnalysisStatus;
import com.example.swissquote.domain.analysis.RiskSignalSummary;
import com.example.swissquote.interfaces.rest.AiAnalysisResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAnalysisInternalControllerTests {

    private final AiAnalysisWorkerService worker = mock(AiAnalysisWorkerService.class);
    private final AiAnalysisInternalController controller = new AiAnalysisInternalController(
            worker,
            "test-token"
    );

    @Test
    void rejectsRequestsWithoutTheServiceToken() {
        assertThatThrownBy(() -> controller.customerAnalyses("wrong", UUID.randomUUID()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void forwardsAuthenticatedAnalysisRequestsToWorker() {
        UUID customerId = UUID.randomUUID();
        AiAnalysisSnapshot snapshot = new AiAnalysisSnapshot(
                customerId,
                new CustomerActivitySummary(1, 1, 0, 0, 0, 0),
                new RiskSignalSummary(0, BigDecimal.ZERO, BigDecimal.ZERO)
        );
        AiAnalysisRequest request = new AiAnalysisRequest(
                UUID.randomUUID(), customerId, null, "Sarah Connor", AiAnalysisStatus.COMPLETED,
                Instant.parse("2026-09-07T10:00:00Z"), Instant.parse("2026-09-07T10:00:00Z"),
                Instant.parse("2026-09-07T10:00:01Z"), null, null
        );
        when(worker.requestAnalysis(snapshot, "Sarah Connor")).thenReturn(request);

        controller.requestAnalysis("test-token", new RemoteAiAnalysisRequest(snapshot, "mock", "analyst-one", "Sarah Connor"));

        verify(worker).requestAnalysis(snapshot, "Sarah Connor");
    }
}
