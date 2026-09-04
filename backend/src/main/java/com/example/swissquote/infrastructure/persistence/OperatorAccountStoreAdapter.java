package com.example.swissquote.infrastructure.persistence;

import com.example.swissquote.application.auth.OperatorAccountStore;
import com.example.swissquote.domain.auth.OperatorAccount;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OperatorAccountStoreAdapter implements OperatorAccountStore {

    private final JpaOperatorUserRepository repository;

    public OperatorAccountStoreAdapter(JpaOperatorUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<OperatorAccount> findByProviderAndSubjectHash(String provider, String providerSubjectHash) {
        return repository
                .findByProviderAndProviderSubjectHash(provider, providerSubjectHash)
                .map(OperatorUserEntity::toDomain);
    }

    @Override
    public OperatorAccount save(OperatorAccount operatorAccount) {
        return repository.save(OperatorUserEntity.fromDomain(operatorAccount)).toDomain();
    }
}
