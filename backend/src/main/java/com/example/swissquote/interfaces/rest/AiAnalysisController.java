package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.analysis.AiAnalysisService;
import com.example.swissquote.domain.analysis.AiAnalysisRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/ai-analyses")
public class AiAnalysisController {

    private static final String MOCK_PROVIDER = "mock";

    private final AiAnalysisService aiAnalysisService;

    public AiAnalysisController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping
    public AiAnalysisResponse requestAnalysis(
            @PathVariable UUID customerId,
            Authentication authentication
    ) {
        return AiAnalysisResponse.fromDomain(aiAnalysisService.requestAnalysis(
                customerId,
                operatorProvider(authentication),
                operatorSubject(authentication),
                operatorDisplayName(authentication)
        ));
    }

    @GetMapping
    public List<AiAnalysisResponse> customerAnalyses(@PathVariable UUID customerId) {
        return aiAnalysisService.findCustomerAnalyses(customerId).stream()
                .map(AiAnalysisResponse::fromDomain)
                .toList();
    }

    private static String operatorProvider(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            return token.getAuthorizedClientRegistrationId();
        }

        if (authentication != null && authentication.getPrincipal() instanceof MockOperatorPrincipal) {
            return MOCK_PROVIDER;
        }

        return null;
    }

    private static String operatorSubject(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            return token.getPrincipal().getName();
        }

        if (authentication != null && authentication.getPrincipal() instanceof MockOperatorPrincipal principal) {
            return principal.subject();
        }

        return null;
    }

    private static String operatorDisplayName(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            Object name = token.getPrincipal().getAttribute("name");
            return name instanceof String stringName ? stringName : token.getPrincipal().getName();
        }

        if (authentication != null && authentication.getPrincipal() instanceof MockOperatorPrincipal principal) {
            return principal.displayName();
        }

        return null;
    }
}
