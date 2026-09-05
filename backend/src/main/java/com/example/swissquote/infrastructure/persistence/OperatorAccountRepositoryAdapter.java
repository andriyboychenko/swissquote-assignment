package com.example.swissquote.infrastructure.persistence;

import com.example.swissquote.application.auth.OperatorAccountRepository;
import com.example.swissquote.domain.auth.OperatorAccount;
import com.example.swissquote.infrastructure.persistence.entity.OperatorUserEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OperatorAccountRepositoryAdapter implements OperatorAccountRepository {

    private final JpaOperatorUserRepository repository;

    public OperatorAccountRepositoryAdapter(JpaOperatorUserRepository repository) {
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
