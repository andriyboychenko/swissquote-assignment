package com.example.swissquote.application.analysis;

import com.example.swissquote.application.activity.CustomerActivityService;
import com.example.swissquote.application.auth.OperatorAccountRepository;
import com.example.swissquote.application.auth.ProviderSubjectHasher;
import com.example.swissquote.domain.activity.CustomerActivityReport;
import com.example.swissquote.domain.activity.CustomerActivitySearchCriteria;
import com.example.swissquote.domain.analysis.AiAnalysisRequest;
import com.example.swissquote.domain.analysis.AiAnalysisResult;
import com.example.swissquote.domain.analysis.RiskSignalSummary;
import com.example.swissquote.domain.auth.OperatorAccount;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "app.ai", name = "remote-enabled", havingValue = "false", matchIfMissing = true)
public class AiAnalysisService implements AiAnalysisUseCase {

    private static final int ANALYSIS_ACTIVITY_LIMIT = 100;

    private final AiAnalysisRepository aiAnalysisRepository;
    private final CustomerActivityService customerActivityService;
    private final CustomerRiskSignalRepository customerRiskSignalRepository;
    private final PolicyKnowledgeRepository policyKnowledgeRepository;
    private final AiAnalysisGenerator aiAnalysisGenerator;
    private final OperatorAccountRepository operatorAccountRepository;
    private final ProviderSubjectHasher providerSubjectHasher;
    private final Clock clock;

    public AiAnalysisService(
            AiAnalysisRepository aiAnalysisRepository,
            CustomerActivityService customerActivityService,
            CustomerRiskSignalRepository customerRiskSignalRepository,
            PolicyKnowledgeRepository policyKnowledgeRepository,
            AiAnalysisGenerator aiAnalysisGenerator,
            OperatorAccountRepository operatorAccountRepository,
            ProviderSubjectHasher providerSubjectHasher,
            Clock clock
    ) {
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.customerActivityService = customerActivityService;
        this.customerRiskSignalRepository = customerRiskSignalRepository;
        this.policyKnowledgeRepository = policyKnowledgeRepository;
        this.aiAnalysisGenerator = aiAnalysisGenerator;
        this.operatorAccountRepository = operatorAccountRepository;
        this.providerSubjectHasher = providerSubjectHasher;
        this.clock = clock;
    }

    @Transactional
    public AiAnalysisRequest requestAnalysis(
            UUID customerId,
            String operatorProvider,
            String operatorSubject,
            String operatorDisplayName
    ) {
        Objects.requireNonNull(customerId, "customerId must not be null");
        UUID operatorId = resolveOperatorId(operatorProvider, operatorSubject);
        Instant requestedAt = Instant.now(clock);
        AiAnalysisRequest pendingRequest = aiAnalysisRepository.save(
                AiAnalysisRequest.pending(customerId, operatorId, normalizedDisplayName(operatorDisplayName), requestedAt)
        );
        AiAnalysisRequest runningRequest = aiAnalysisRepository.save(pendingRequest.running(requestedAt));

        try {
            CustomerActivityReport activityReport = customerActivityService.getActivityReport(
                    customerId,
                    ANALYSIS_ACTIVITY_LIMIT,
                    0,
                    CustomerActivitySearchCriteria.defaultCriteria()
            );
            RiskSignalSummary riskSignalSummary = customerRiskSignalRepository.summarizeForCustomer(customerId);
            List<PolicyEvidence> policyEvidence = policyKnowledgeRepository.retrieveRelevantPolicies(
                    activityReport,
                    riskSignalSummary
            );
            Instant completedAt = Instant.now(clock);
            AiAnalysisResult result = aiAnalysisGenerator.generate(
                    runningRequest.analysisRequestId(),
                    activityReport,
                    riskSignalSummary,
                    policyEvidence,
                    completedAt
            );
            return aiAnalysisRepository.save(runningRequest.completed(result, completedAt));
        } catch (RuntimeException exception) {
            return aiAnalysisRepository.save(runningRequest.failed(exception.getMessage(), Instant.now(clock)));
        }
    }

    @Transactional(readOnly = true)
    public List<AiAnalysisRequest> findCustomerAnalyses(UUID customerId) {
        Objects.requireNonNull(customerId, "customerId must not be null");
        return aiAnalysisRepository.findByCustomerId(customerId);
    }

    private UUID resolveOperatorId(String provider, String subject) {
        if (provider == null || subject == null) {
            return null;
        }

        String subjectHash = providerSubjectHasher.hash(provider, subject);
        return operatorAccountRepository.findByProviderAndSubjectHash(provider, subjectHash)
                .map(OperatorAccount::operatorId)
                .orElse(null);
    }

    private static String normalizedDisplayName(String operatorDisplayName) {
        return operatorDisplayName == null || operatorDisplayName.isBlank() ? "Unknown operator" : operatorDisplayName.trim();
    }
}
