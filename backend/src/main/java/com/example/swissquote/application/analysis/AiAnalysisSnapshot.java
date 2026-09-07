package com.example.swissquote.application.analysis;

import com.example.swissquote.domain.activity.CustomerActivitySummary;
import com.example.swissquote.domain.analysis.RiskSignalSummary;

import java.util.Objects;
import java.util.UUID;

public record AiAnalysisSnapshot(
        UUID customerId,
        CustomerActivitySummary activitySummary,
        RiskSignalSummary riskSignalSummary
) {

    public AiAnalysisSnapshot {
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(activitySummary, "activitySummary must not be null");
        Objects.requireNonNull(riskSignalSummary, "riskSignalSummary must not be null");
    }
}
