package com.example.swissquote.domain.activity;

public record CustomerActivitySummary(
        int totalActivities,
        int cardActivities,
        int paymentActivities,
        int cryptoActivities,
        int failedActivities,
        int pendingActivities
) {
}
