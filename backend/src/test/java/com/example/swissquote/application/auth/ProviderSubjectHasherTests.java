package com.example.swissquote.application.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ProviderSubjectHasherTests {

    private final ProviderSubjectHasher hasher = new ProviderSubjectHasher();

    @Test
    void hashReturnsStableSha256HexValue() {
        String firstHash = hasher.hash("google", "subject-123");
        String secondHash = hasher.hash("google", "subject-123");

        assertThat(firstHash).hasSize(64);
        assertThat(firstHash).isEqualTo(secondHash);
        assertThat(firstHash).doesNotContain("google");
        assertThat(firstHash).doesNotContain("subject-123");
    }

    @Test
    void hashSeparatesProviders() {
        String googleHash = hasher.hash("google", "subject-123");
        String metaHash = hasher.hash("meta", "subject-123");

        assertThat(googleHash).isNotEqualTo(metaHash);
    }

    @Test
    void hashRequiresProviderAndSubject() {
        assertThatNullPointerException()
                .isThrownBy(() -> hasher.hash(null, "subject-123"))
                .withMessage("provider must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> hasher.hash("google", null))
                .withMessage("subject must not be null");
    }
}
