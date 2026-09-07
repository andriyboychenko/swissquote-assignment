package com.example.swissquote.ai;

import com.example.swissquote.application.analysis.RemoteAiAnalysisRequest;
import com.example.swissquote.domain.analysis.AiAnalysisRequest;
import com.example.swissquote.interfaces.rest.AiAnalysisResponse;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/ai-analyses")
public class AiAnalysisInternalController {

    private static final String TOKEN_HEADER = "X-AI-Service-Token";

    private final AiAnalysisWorkerService workerService;
    private final String internalToken;

    public AiAnalysisInternalController(
            AiAnalysisWorkerService workerService,
            @Value("${ai.service.internal-token:local-ai-service-token}") String internalToken
    ) {
        this.workerService = workerService;
        this.internalToken = internalToken;
    }

    @PostMapping
    public AiAnalysisResponse requestAnalysis(
            @RequestHeader(TOKEN_HEADER) String token,
            @RequestBody RemoteAiAnalysisRequest request
    ) {
        checkToken(token);
        return AiAnalysisResponse.fromDomain(workerService.requestAnalysis(
                request.snapshot(), request.operatorDisplayName()
        ));
    }

    @GetMapping("/{customerId}")
    public List<AiAnalysisResponse> customerAnalyses(
            @RequestHeader(TOKEN_HEADER) String token,
            @PathVariable UUID customerId
    ) {
        checkToken(token);
        return workerService.findCustomerAnalyses(customerId).stream()
                .map(AiAnalysisResponse::fromDomain)
                .toList();
    }

    private void checkToken(String token) {
        if (internalToken == null || internalToken.isBlank() || !internalToken.equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal service token");
        }
    }
}
