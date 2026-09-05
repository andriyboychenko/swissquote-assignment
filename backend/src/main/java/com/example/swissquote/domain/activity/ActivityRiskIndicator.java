package com.example.swissquote.domain.activity;

import java.math.BigDecimal;
import java.util.Objects;

public record ActivityRiskIndicator(
        String ruleName,
        String severity,
        BigDecimal scoreContribution
) {

    public ActivityRiskIndicator {
        Objects.requireNonNull(ruleName, "ruleName must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(scoreContribution, "scoreContribution must not be null");
    }
}
