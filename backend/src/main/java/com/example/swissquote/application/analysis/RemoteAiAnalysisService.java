package com.example.swissquote.application.analysis;

import com.example.swissquote.application.activity.CustomerActivityService;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.analysis.AiAnalysisRequest;
import com.example.swissquote.domain.analysis.RiskSignalSummary;
import com.example.swissquote.interfaces.rest.AiAnalysisResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "app.ai", name = "remote-enabled", havingValue = "true")
public class RemoteAiAnalysisService implements AiAnalysisUseCase {

    private final RestClient restClient;
    private final String serviceToken;
    private final CustomerActivityService customerActivityService;
    private final CustomerRiskSignalRepository customerRiskSignalRepository;

    public RemoteAiAnalysisService(
            RestClient.Builder restClientBuilder,
            AiAnalysisServiceProperties properties,
            CustomerActivityService customerActivityService,
            CustomerRiskSignalRepository customerRiskSignalRepository
    ) {
        this.restClient = restClientBuilder.baseUrl(properties.serviceUrl()).build();
        this.serviceToken = properties.serviceToken();
        this.customerActivityService = customerActivityService;
        this.customerRiskSignalRepository = customerRiskSignalRepository;
    }

    @Override
    public AiAnalysisRequest requestAnalysis(
            UUID customerId,
            String operatorProvider,
            String operatorSubject,
            String operatorDisplayName
    ) {
        CustomerActivityReport report = customerActivityService.getActivityReport(
                customerId,
                100,
                0,
                com.example.swissquote.domain.activity.CustomerActivitySearchCriteria.defaultCriteria()
        );
        AiAnalysisSnapshot snapshot = new AiAnalysisSnapshot(
                customerId,
                report.summary(),
                customerRiskSignalRepository.summarizeForCustomer(customerId)
        );
        AiAnalysisResponse response = restClient.post()
                .uri("/internal/ai-analyses")
                .header("X-AI-Service-Token", serviceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RemoteAiAnalysisRequest(snapshot, operatorProvider, operatorSubject, operatorDisplayName))
                .retrieve()
                .body(AiAnalysisResponse.class);
        return toDomain(response);
    }

    @Override
    public List<AiAnalysisRequest> findCustomerAnalyses(UUID customerId) {
        List<AiAnalysisResponse> responses = restClient.get()
                .uri("/internal/ai-analyses/{customerId}", customerId)
                .header("X-AI-Service-Token", serviceToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return responses == null ? List.of() : responses.stream().map(RemoteAiAnalysisService::toDomain).toList();
    }

    private static AiAnalysisRequest toDomain(AiAnalysisResponse response) {
        if (response == null) {
            throw new IllegalStateException("AI service returned an empty response");
        }
        AiAnalysisResponse.AiAnalysisResultResponse result = response.result();
        return new AiAnalysisRequest(
                response.analysisRequestId(),
                response.customerId(),
                null,
                response.requestedByOperatorDisplayName(),
                com.example.swissquote.domain.analysis.AiAnalysisStatus.valueOf(response.status()),
                response.requestedAt(),
                response.startedAt(),
                response.completedAt(),
                response.failureReason(),
                result == null ? null : new com.example.swissquote.domain.analysis.AiAnalysisResult(
                        result.analysisResultId(),
                        com.example.swissquote.domain.analysis.RiskLevel.valueOf(result.riskLevel()),
                        result.summary(),
                        result.recommendations(),
                        result.modelName(),
                        result.promptVersion(),
                        result.createdAt(),
                        result.evidence().stream().map(evidence -> new com.example.swissquote.domain.analysis.AiAnalysisEvidence(
                                evidence.evidenceId(),
                                evidence.sourceType(),
                                evidence.sourceReference(),
                                evidence.excerpt(),
                                evidence.relevanceScore()
                        )).toList()
                )
        );
    }
}
