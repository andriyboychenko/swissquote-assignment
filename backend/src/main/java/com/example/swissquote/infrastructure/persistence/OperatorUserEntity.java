package com.example.swissquote.infrastructure.persistence;

import com.example.swissquote.domain.auth.OperatorAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "operator_users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_operator_users_provider_subject_hash",
                columnNames = {"provider", "provider_subject_hash"}
        )
)
public class OperatorUserEntity {

    @Id
    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(name = "provider", nullable = false, length = 40)
    private String provider;

    @Column(name = "provider_subject_hash", nullable = false, length = 64)
    private String providerSubjectHash;

    @Column(name = "blocked", nullable = false)
    private boolean blocked;

    @Column(name = "block_reason")
    private String blockReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_login_at", nullable = false)
    private Instant lastLoginAt;

    protected OperatorUserEntity() {
    }

    private OperatorUserEntity(OperatorAccount account) {
        this.operatorId = account.operatorId();
        this.provider = account.provider();
        this.providerSubjectHash = account.providerSubjectHash();
        this.blocked = account.blocked();
        this.blockReason = account.blockReason();
        this.createdAt = account.createdAt();
        this.lastLoginAt = account.lastLoginAt();
    }

    static OperatorUserEntity fromDomain(OperatorAccount account) {
        return new OperatorUserEntity(account);
    }

    OperatorAccount toDomain() {
        return new OperatorAccount(
                operatorId,
                provider,
                providerSubjectHash,
                blocked,
                blockReason,
                createdAt,
                lastLoginAt
        );
    }
}
