package com.example.swissquote.application.auth;

import com.example.swissquote.domain.auth.OperatorAccess;
import com.example.swissquote.domain.auth.OperatorAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
public class OperatorAccessService {

    private final OperatorAccountRepository operatorAccountRepository;
    private final ProviderSubjectHasher providerSubjectHasher;
    private final Clock clock;

    public OperatorAccessService(
            OperatorAccountRepository operatorAccountRepository,
            ProviderSubjectHasher providerSubjectHasher,
            Clock clock
    ) {
        this.operatorAccountRepository = operatorAccountRepository;
        this.providerSubjectHasher = providerSubjectHasher;
        this.clock = clock;
    }

    @Transactional
    public OperatorAccess recordLogin(String provider, String subject) {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(subject, "subject must not be null");

        String subjectHash = providerSubjectHasher.hash(provider, subject);
        Instant now = Instant.now(clock);
        OperatorAccount account = operatorAccountRepository
                .findByProviderAndSubjectHash(provider, subjectHash)
                .map(existingAccount -> existingAccount.recordLoginAt(now))
                .orElseGet(() -> OperatorAccount.firstLogin(provider, subjectHash, now));

        OperatorAccount savedAccount = operatorAccountRepository.save(account);
        return new OperatorAccess(savedAccount.provider(), savedAccount.blocked(), savedAccount.blockReason());
    }
}
