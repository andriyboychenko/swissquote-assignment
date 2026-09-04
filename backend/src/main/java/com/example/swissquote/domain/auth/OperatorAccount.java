package com.example.swissquote.domain.auth;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OperatorAccount(
        UUID operatorId,
        String provider,
        String providerSubjectHash,
        boolean blocked,
        String blockReason,
        Instant createdAt,
        Instant lastLoginAt
) {

    public OperatorAccount {
        Objects.requireNonNull(operatorId, "operatorId must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(providerSubjectHash, "providerSubjectHash must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(lastLoginAt, "lastLoginAt must not be null");
    }

    public static OperatorAccount firstLogin(String provider, String providerSubjectHash, Instant now) {
        return new OperatorAccount(UUID.randomUUID(), provider, providerSubjectHash, false, null, now, now);
    }

    public OperatorAccount recordLoginAt(Instant loginAt) {
        return new OperatorAccount(
                operatorId,
                provider,
                providerSubjectHash,
                blocked,
                blockReason,
                createdAt,
                loginAt
        );
    }
}
