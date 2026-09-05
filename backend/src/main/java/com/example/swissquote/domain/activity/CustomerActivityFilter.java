package com.example.swissquote.domain.activity;

import java.math.BigDecimal;
import java.time.Instant;

public record CustomerActivityFilter(
        Instant createdFrom,
        Instant createdTo,
        ActivityType activityType,
        String status,
        BigDecimal amountMin,
        BigDecimal amountMax,
        String currency,
        String counterparty,
        String channel,
        String detail
) {
}
