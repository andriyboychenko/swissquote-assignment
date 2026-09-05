package com.example.swissquote.application.analysis;

import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.analysis.RiskSignalSummary;

import java.util.List;

public interface PolicyKnowledgeRepository {

    List<PolicyEvidence> retrieveRelevantPolicies(
            CustomerActivityReport activityReport,
            RiskSignalSummary riskSignalSummary
    );
}
