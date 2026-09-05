package com.example.swissquote.infrastructure.persistence;

import com.example.swissquote.infrastructure.persistence.entity.OperatorUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaOperatorUserRepository extends JpaRepository<OperatorUserEntity, UUID> {

    Optional<OperatorUserEntity> findByProviderAndProviderSubjectHash(String provider, String providerSubjectHash);
}
