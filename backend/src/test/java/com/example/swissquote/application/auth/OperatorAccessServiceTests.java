package com.example.swissquote.application.auth;

import com.example.swissquote.domain.auth.OperatorAccess;
import com.example.swissquote.domain.auth.OperatorAccount;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperatorAccessServiceTests {

    private final OperatorAccountRepository repository = mock(OperatorAccountRepository.class);
    private final ProviderSubjectHasher hasher = new ProviderSubjectHasher();
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-04T10:15:30Z"), ZoneOffset.UTC);
    private final OperatorAccessService service = new OperatorAccessService(repository, hasher, clock);

    @Test
    void recordLoginCreatesPseudonymousOperatorForFirstLogin() {
        when(repository.findByProviderAndSubjectHash("google", hasher.hash("google", "subject-123")))
                .thenReturn(Optional.empty());
        when(repository.save(any(OperatorAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OperatorAccess access = service.recordLogin("google", "subject-123");

        assertThat(access.provider()).isEqualTo("google");
        assertThat(access.blocked()).isFalse();
        assertThat(access.blockReason()).isNull();
        verify(repository).save(any(OperatorAccount.class));
    }

    @Test
    void recordLoginKeepsBlockedStateForExistingOperator() {
        String subjectHash = hasher.hash("google", "subject-123");
        OperatorAccount existingAccount = new OperatorAccount(
                java.util.UUID.randomUUID(),
                "google",
                subjectHash,
                true,
                "Pending moderator approval",
                Instant.parse("2026-09-03T10:15:30Z"),
                Instant.parse("2026-09-03T10:15:30Z")
        );
        when(repository.findByProviderAndSubjectHash("google", subjectHash)).thenReturn(Optional.of(existingAccount));
        when(repository.save(any(OperatorAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OperatorAccess access = service.recordLogin("google", "subject-123");

        assertThat(access.blocked()).isTrue();
        assertThat(access.blockReason()).isEqualTo("Pending moderator approval");
        verify(repository).save(existingAccount.recordLoginAt(Instant.parse("2026-09-04T10:15:30Z")));
    }

    @Test
    void recordLoginRequiresProviderAndSubject() {
        assertThatNullPointerException()
                .isThrownBy(() -> service.recordLogin(null, "subject-123"))
                .withMessage("provider must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> service.recordLogin("google", null))
                .withMessage("subject must not be null");
    }
}
