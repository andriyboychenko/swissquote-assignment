package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.auth.OperatorAccessService;
import com.example.swissquote.config.GoogleOAuthProperties;
import com.example.swissquote.domain.auth.OperatorAccess;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String MOCK_PROVIDER = "mock";

    private final OperatorAccessService operatorAccessService;
    private final GoogleOAuthProperties googleOAuthProperties;

    public AuthController(OperatorAccessService operatorAccessService, GoogleOAuthProperties googleOAuthProperties) {
        this.operatorAccessService = operatorAccessService;
        this.googleOAuthProperties = googleOAuthProperties;
    }

    @GetMapping("/me")
    public AuthenticatedOperatorResponse currentOperator(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2User principal = token.getPrincipal();
            String provider = token.getAuthorizedClientRegistrationId();
            OperatorAccess access = operatorAccessService.recordLogin(provider, principal.getName());

            return new AuthenticatedOperatorResponse(
                    stringAttribute(principal, "name"),
                    stringAttribute(principal, "email"),
                    access.provider(),
                    true,
                    access.blocked(),
                    access.blockReason(),
                    googleOAuthProperties.configured()
            );
        }

        if (authentication != null && authentication.getPrincipal() instanceof MockOperatorPrincipal principal) {
            OperatorAccess access = operatorAccessService.recordLogin(MOCK_PROVIDER, principal.subject());

            return new AuthenticatedOperatorResponse(
                    principal.displayName(),
                    null,
                    access.provider(),
                    true,
                    access.blocked(),
                    access.blockReason(),
                    googleOAuthProperties.configured()
            );
        }

        return AuthenticatedOperatorResponse.anonymous(googleOAuthProperties.configured());
    }

    private static String stringAttribute(OAuth2User principal, String name) {
        Object value = principal.getAttribute(name);
        return value instanceof String stringValue ? stringValue : null;
    }
}
