package com.example.swissquote.domain.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record AiAnalysisResult(
        UUID analysisResultId,
        RiskLevel riskLevel,
        String summary,
        String recommendations,
        String modelName,
        String promptVersion,
        Instant createdAt,
        List<AiAnalysisEvidence> evidence
) {

    public AiAnalysisResult {
        Objects.requireNonNull(analysisResultId, "analysisResultId must not be null");
        Objects.requireNonNull(riskLevel, "riskLevel must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(recommendations, "recommendations must not be null");
        Objects.requireNonNull(modelName, "modelName must not be null");
        Objects.requireNonNull(promptVersion, "promptVersion must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence must not be null"));
    }
}
