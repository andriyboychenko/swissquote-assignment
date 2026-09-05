package com.example.swissquote.domain.activity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CustomerActivity(
        UUID transactionId,
        ActivityType activityType,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt,
        String counterparty,
        String channel,
        String detail
) {

    public CustomerActivity {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(activityType, "activityType must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
