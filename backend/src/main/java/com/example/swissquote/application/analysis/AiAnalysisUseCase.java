package com.example.swissquote.application.analysis;

import com.example.swissquote.domain.analysis.AiAnalysisRequest;

import java.util.List;
import java.util.UUID;

public interface AiAnalysisUseCase {

    AiAnalysisRequest requestAnalysis(
            UUID customerId,
            String operatorProvider,
            String operatorSubject,
            String operatorDisplayName
    );

    List<AiAnalysisRequest> findCustomerAnalyses(UUID customerId);
}
