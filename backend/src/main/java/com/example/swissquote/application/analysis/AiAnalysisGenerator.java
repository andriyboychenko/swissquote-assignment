package com.example.swissquote.application.analysis;

import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.analysis.AiAnalysisResult;
import com.example.swissquote.domain.analysis.RiskSignalSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AiAnalysisGenerator {

    AiAnalysisResult generate(
            UUID analysisRequestId,
            CustomerActivityReport activityReport,
            RiskSignalSummary riskSignalSummary,
            List<PolicyEvidence> policyEvidence,
            Instant generatedAt
    );
}
