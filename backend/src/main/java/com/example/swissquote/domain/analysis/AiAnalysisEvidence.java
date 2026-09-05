package com.example.swissquote.domain.analysis;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record AiAnalysisEvidence(
        UUID evidenceId,
        String sourceType,
        String sourceReference,
        String excerpt,
        BigDecimal relevanceScore
) {

    public AiAnalysisEvidence {
        Objects.requireNonNull(evidenceId, "evidenceId must not be null");
        Objects.requireNonNull(sourceType, "sourceType must not be null");
        Objects.requireNonNull(sourceReference, "sourceReference must not be null");
        Objects.requireNonNull(excerpt, "excerpt must not be null");
        Objects.requireNonNull(relevanceScore, "relevanceScore must not be null");
    }
}
