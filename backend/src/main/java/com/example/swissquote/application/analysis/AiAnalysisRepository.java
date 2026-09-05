package com.example.swissquote.application.analysis;

import com.example.swissquote.domain.analysis.AiAnalysisRequest;

import java.util.List;
import java.util.UUID;

public interface AiAnalysisRepository {

    AiAnalysisRequest save(AiAnalysisRequest request);

    List<AiAnalysisRequest> findByCustomerId(UUID customerId);
}
