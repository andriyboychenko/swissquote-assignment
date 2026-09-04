package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.auth.OperatorAccessService;
import com.example.swissquote.domain.auth.OperatorAccess;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTests {

    @Test
    void currentOperatorReturnsAnonymousResponseWithoutOAuthAuthentication() {
        OperatorAccessService accessService = mock(OperatorAccessService.class);
        AuthController controller = new AuthController(accessService);

        AuthenticatedOperatorResponse response = controller.currentOperator(null);

        assertThat(response.authenticated()).isFalse();
        assertThat(response.name()).isNull();
        assertThat(response.email()).isNull();
        assertThat(response.provider()).isNull();
        assertThat(response.blocked()).isFalse();
        assertThat(response.blockReason()).isNull();
    }

    @Test
    void currentOperatorReturnsOAuthOperatorDetailsAndRecordsPseudonymousLogin() {
        OperatorAccessService accessService = mock(OperatorAccessService.class);
        when(accessService.recordLogin("google", "google-subject"))
                .thenReturn(new OperatorAccess("google", false, null));
        AuthController controller = new AuthController(accessService);
        OAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "google-subject", "name", "Demo Operator", "email", "operator@example.com"),
                "sub"
        );
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "google"
        );

        AuthenticatedOperatorResponse response = controller.currentOperator(authentication);

        assertThat(response.authenticated()).isTrue();
        assertThat(response.name()).isEqualTo("Demo Operator");
        assertThat(response.email()).isEqualTo("operator@example.com");
        assertThat(response.provider()).isEqualTo("google");
        assertThat(response.blocked()).isFalse();
        assertThat(response.blockReason()).isNull();
        verify(accessService).recordLogin("google", "google-subject");
    }

    @Test
    void currentOperatorReturnsBlockedAccessState() {
        OperatorAccessService accessService = mock(OperatorAccessService.class);
        when(accessService.recordLogin("google", "google-subject"))
                .thenReturn(new OperatorAccess("google", true, "Pending moderator approval"));
        AuthController controller = new AuthController(accessService);
        OAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "google-subject", "name", "Demo Operator", "email", "operator@example.com"),
                "sub"
        );
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "google"
        );

        AuthenticatedOperatorResponse response = controller.currentOperator(authentication);

        assertThat(response.authenticated()).isTrue();
        assertThat(response.blocked()).isTrue();
        assertThat(response.blockReason()).isEqualTo("Pending moderator approval");
    }
}
