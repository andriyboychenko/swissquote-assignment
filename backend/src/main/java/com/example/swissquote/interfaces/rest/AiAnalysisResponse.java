package com.example.swissquote.interfaces.rest;

import com.example.swissquote.domain.analysis.AiAnalysisEvidence;
import com.example.swissquote.domain.analysis.AiAnalysisRequest;
import com.example.swissquote.domain.analysis.AiAnalysisResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AiAnalysisResponse(
        UUID analysisRequestId,
        UUID customerId,
        String status,
        Instant requestedAt,
        Instant startedAt,
        Instant completedAt,
        String failureReason,
        AiAnalysisResultResponse result
) {

    public static AiAnalysisResponse fromDomain(AiAnalysisRequest request) {
        return new AiAnalysisResponse(
                request.analysisRequestId(),
                request.customerId(),
                request.status().name(),
                request.requestedAt(),
                request.startedAt(),
                request.completedAt(),
                request.failureReason(),
                request.result() == null ? null : AiAnalysisResultResponse.fromDomain(request.result())
        );
    }

    public record AiAnalysisResultResponse(
            UUID analysisResultId,
            String riskLevel,
            String summary,
            String recommendations,
            String modelName,
            String promptVersion,
            Instant createdAt,
            List<AiAnalysisEvidenceResponse> evidence
    ) {

        public static AiAnalysisResultResponse fromDomain(AiAnalysisResult result) {
            return new AiAnalysisResultResponse(
                    result.analysisResultId(),
                    result.riskLevel().name(),
                    result.summary(),
                    result.recommendations(),
                    result.modelName(),
                    result.promptVersion(),
                    result.createdAt(),
                    result.evidence().stream()
                            .map(AiAnalysisEvidenceResponse::fromDomain)
                            .toList()
            );
        }
    }

    public record AiAnalysisEvidenceResponse(
            UUID evidenceId,
            String sourceType,
            String sourceReference,
            String excerpt,
            BigDecimal relevanceScore
    ) {

        public static AiAnalysisEvidenceResponse fromDomain(AiAnalysisEvidence evidence) {
            return new AiAnalysisEvidenceResponse(
                    evidence.evidenceId(),
                    evidence.sourceType(),
                    evidence.sourceReference(),
                    evidence.excerpt(),
                    evidence.relevanceScore()
            );
        }
    }
}
