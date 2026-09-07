package com.example.swissquote.ai;

import com.example.swissquote.application.analysis.AiAnalysisGenerator;
import com.example.swissquote.application.analysis.AiAnalysisRepository;
import com.example.swissquote.application.analysis.AiAnalysisSnapshot;
import com.example.swissquote.application.analysis.PolicyEvidence;
import com.example.swissquote.application.analysis.PolicyKnowledgeRepository;
import com.example.swissquote.domain.activity.CustomerActivityPage;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.analysis.AiAnalysisRequest;
import com.example.swissquote.domain.analysis.AiAnalysisResult;
import com.example.swissquote.domain.analysis.RiskSignalSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AiAnalysisWorkerService {

    private final AiAnalysisRepository repository;
    private final PolicyKnowledgeRepository policyKnowledgeRepository;
    private final AiAnalysisGenerator generator;
    private final Clock clock;

    public AiAnalysisWorkerService(
            AiAnalysisRepository repository,
            PolicyKnowledgeRepository policyKnowledgeRepository,
            AiAnalysisGenerator generator,
            Clock clock
    ) {
        this.repository = repository;
        this.policyKnowledgeRepository = policyKnowledgeRepository;
        this.generator = generator;
        this.clock = clock;
    }

    @Transactional
    public AiAnalysisRequest requestAnalysis(
            AiAnalysisSnapshot snapshot,
            String operatorDisplayName
    ) {
        Instant requestedAt = Instant.now(clock);
        AiAnalysisRequest pending = repository.save(AiAnalysisRequest.pending(
                snapshot.customerId(),
                null,
                operatorDisplayName == null || operatorDisplayName.isBlank() ? "Unknown operator" : operatorDisplayName,
                requestedAt
        ));
        AiAnalysisRequest running = repository.save(pending.running(requestedAt));
        try {
            CustomerActivityReport activityReport = new CustomerActivityReport(
                    snapshot.customerId(),
                    snapshot.activitySummary(),
                    List.of(),
                    new CustomerActivityPage(snapshot.activitySummary().totalActivities(), 0, 0, false, 0)
            );
            RiskSignalSummary signals = snapshot.riskSignalSummary();
            List<PolicyEvidence> evidence = policyKnowledgeRepository.retrieveRelevantPolicies(activityReport, signals);
            AiAnalysisResult result = generator.generate(
                    running.analysisRequestId(), activityReport, signals, evidence, Instant.now(clock)
            );
            return repository.save(running.completed(result, Instant.now(clock)));
        } catch (RuntimeException exception) {
            return repository.save(running.failed(exception.getMessage(), Instant.now(clock)));
        }
    }

    @Transactional(readOnly = true)
    public List<AiAnalysisRequest> findCustomerAnalyses(UUID customerId) {
        return repository.findByCustomerId(customerId);
    }
}
