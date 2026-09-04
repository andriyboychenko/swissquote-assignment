package com.example.swissquote.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleClientRegistrationRepositoryTests {

    @Test
    void configuredReturnsFalseWhenCredentialsAreMissing() {
        GoogleOAuthProperties properties = new GoogleOAuthProperties("", "", "{baseUrl}/login/oauth2/code/{registrationId}");

        assertThat(properties.configured()).isFalse();
    }

    @Test
    void findByRegistrationIdReturnsNullWhenGoogleCredentialsAreMissing() {
        GoogleOAuthProperties properties = new GoogleOAuthProperties("", "", "{baseUrl}/login/oauth2/code/{registrationId}");
        GoogleClientRegistrationRepository repository = new GoogleClientRegistrationRepository(properties);

        assertThat(repository.findByRegistrationId("google")).isNull();
        assertThat(repository).isEmpty();
    }

    @Test
    void findByRegistrationIdReturnsGoogleRegistrationWhenCredentialsExist() {
        GoogleOAuthProperties properties = new GoogleOAuthProperties(
                "google-client-id",
                "google-client-secret",
                "{baseUrl}/login/oauth2/code/{registrationId}"
        );
        GoogleClientRegistrationRepository repository = new GoogleClientRegistrationRepository(properties);

        ClientRegistration registration = repository.findByRegistrationId("google");

        assertThat(registration).isNotNull();
        assertThat(registration.getRegistrationId()).isEqualTo("google");
        assertThat(registration.getClientId()).isEqualTo("google-client-id");
        assertThat(registration.getClientSecret()).isEqualTo("google-client-secret");
        assertThat(registration.getScopes()).contains("openid", "profile", "email");
    }
}
