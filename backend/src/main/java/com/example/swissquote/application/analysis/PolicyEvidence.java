package com.example.swissquote.application.analysis;

import java.math.BigDecimal;
import java.util.Objects;

public record PolicyEvidence(
        String sourceReference,
        String excerpt,
        BigDecimal relevanceScore
) {

    public PolicyEvidence {
        Objects.requireNonNull(sourceReference, "sourceReference must not be null");
        Objects.requireNonNull(excerpt, "excerpt must not be null");
        Objects.requireNonNull(relevanceScore, "relevanceScore must not be null");
    }
}
