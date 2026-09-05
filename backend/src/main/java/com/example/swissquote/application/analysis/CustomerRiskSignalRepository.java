package com.example.swissquote.application.analysis;

import com.example.swissquote.domain.analysis.RiskSignalSummary;

import java.util.UUID;

public interface CustomerRiskSignalRepository {

    RiskSignalSummary summarizeForCustomer(UUID customerId);
}
