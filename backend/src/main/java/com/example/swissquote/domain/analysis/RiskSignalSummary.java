package com.example.swissquote.domain.analysis;

import java.math.BigDecimal;
import java.util.Objects;

public record RiskSignalSummary(
        int triggeredSignals,
        BigDecimal totalScoreContribution,
        BigDecimal maxScoreContribution
) {

    public RiskSignalSummary {
        Objects.requireNonNull(totalScoreContribution, "totalScoreContribution must not be null");
        Objects.requireNonNull(maxScoreContribution, "maxScoreContribution must not be null");
    }
}
