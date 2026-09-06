package com.example.swissquote.domain.analysis;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AiAnalysisRequest(
        UUID analysisRequestId,
        UUID customerId,
        UUID requestedByOperatorId,
        String requestedByOperatorDisplayName,
        AiAnalysisStatus status,
        Instant requestedAt,
        Instant startedAt,
        Instant completedAt,
        String failureReason,
        AiAnalysisResult result
) {

    public AiAnalysisRequest {
        Objects.requireNonNull(analysisRequestId, "analysisRequestId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(requestedAt, "requestedAt must not be null");
    }

    public static AiAnalysisRequest pending(
            UUID customerId,
            UUID requestedByOperatorId,
            String requestedByOperatorDisplayName,
            Instant requestedAt
    ) {
        return new AiAnalysisRequest(
                UUID.randomUUID(),
                customerId,
                requestedByOperatorId,
                requestedByOperatorDisplayName,
                AiAnalysisStatus.PENDING,
                requestedAt,
                null,
                null,
                null,
                null
        );
    }

    public AiAnalysisRequest running(Instant startedAt) {
        return new AiAnalysisRequest(
                analysisRequestId,
                customerId,
                requestedByOperatorId,
                requestedByOperatorDisplayName,
                AiAnalysisStatus.RUNNING,
                requestedAt,
                startedAt,
                null,
                null,
                null
        );
    }

    public AiAnalysisRequest completed(AiAnalysisResult completedResult, Instant completedAt) {
        return new AiAnalysisRequest(
                analysisRequestId,
                customerId,
                requestedByOperatorId,
                requestedByOperatorDisplayName,
                AiAnalysisStatus.COMPLETED,
                requestedAt,
                startedAt,
                completedAt,
                null,
                completedResult
        );
    }

    public AiAnalysisRequest failed(String reason, Instant failedAt) {
        return new AiAnalysisRequest(
                analysisRequestId,
                customerId,
                requestedByOperatorId,
                requestedByOperatorDisplayName,
                AiAnalysisStatus.FAILED,
                requestedAt,
                startedAt,
                failedAt,
                reason,
                null
        );
    }
}
