package com.example.swissquote.config;

import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.Iterator;
import java.util.List;

public final class GoogleClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private static final String REGISTRATION_ID = "google";
    private final List<ClientRegistration> registrations;

    public GoogleClientRegistrationRepository(GoogleOAuthProperties properties) {
        this.registrations = properties.configured()
                ? List.of(buildGoogleRegistration(properties))
                : List.of();
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return registrations.stream()
                .filter(registration -> registration.getRegistrationId().equals(registrationId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Iterator<ClientRegistration> iterator() {
        return registrations.iterator();
    }

    private static ClientRegistration buildGoogleRegistration(GoogleOAuthProperties properties) {
        return CommonOAuth2Provider.GOOGLE
                .getBuilder(REGISTRATION_ID)
                .clientId(properties.clientId())
                .clientSecret(properties.clientSecret())
                .redirectUri(properties.redirectUri())
                .scope("openid", "profile", "email")
                .build();
    }
}
