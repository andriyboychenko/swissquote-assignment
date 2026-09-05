package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.analysis.AiAnalysisService;
import com.example.swissquote.domain.analysis.AiAnalysisRequest;
import com.example.swissquote.domain.analysis.AiAnalysisResult;
import com.example.swissquote.domain.analysis.AiAnalysisStatus;
import com.example.swissquote.domain.analysis.RiskLevel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAnalysisControllerTests {

    private final AiAnalysisService service = mock(AiAnalysisService.class);
    private final AiAnalysisController controller = new AiAnalysisController(service);

    @Test
    void requestAnalysisPassesCustomerAndOAuthOperator() {
        UUID customerId = UUID.randomUUID();
        AiAnalysisRequest request = completedRequest(customerId);
        when(service.requestAnalysis(
                org.mockito.ArgumentMatchers.eq(customerId),
                org.mockito.ArgumentMatchers.eq("google"),
                org.mockito.ArgumentMatchers.eq("subject-1")
        )).thenReturn(request);

        AiAnalysisResponse response = controller.requestAnalysis(customerId, oauthToken());

        assertThat(response.analysisRequestId()).isEqualTo(request.analysisRequestId());
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.result().riskLevel()).isEqualTo("HIGH");
        ArgumentCaptor<String> providerCaptor = ArgumentCaptor.forClass(String.class);
        verify(service).requestAnalysis(
                org.mockito.ArgumentMatchers.eq(customerId),
                providerCaptor.capture(),
                org.mockito.ArgumentMatchers.eq("subject-1")
        );
        assertThat(providerCaptor.getValue()).isEqualTo("google");
    }

    @Test
    void customerAnalysesReturnsMappedList() {
        UUID customerId = UUID.randomUUID();
        when(service.findCustomerAnalyses(customerId)).thenReturn(List.of(completedRequest(customerId)));

        List<AiAnalysisResponse> response = controller.customerAnalyses(customerId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().customerId()).isEqualTo(customerId);
    }

    private static OAuth2AuthenticationToken oauthToken() {
        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(() -> "ROLE_USER"),
                Map.of("sub", "subject-1"),
                "sub"
        );
        return new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google");
    }

    private static AiAnalysisRequest completedRequest(UUID customerId) {
        Instant now = Instant.parse("2026-09-05T08:00:00Z");
        AiAnalysisResult result = new AiAnalysisResult(
                UUID.randomUUID(),
                RiskLevel.HIGH,
                "Summary",
                "Recommendation",
                "model",
                "prompt",
                now,
                List.of()
        );
        return new AiAnalysisRequest(
                UUID.randomUUID(),
                customerId,
                UUID.randomUUID(),
                AiAnalysisStatus.COMPLETED,
                now,
                now,
                now,
                null,
                result
        );
    }
}
