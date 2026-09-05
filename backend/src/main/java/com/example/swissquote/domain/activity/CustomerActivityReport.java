package com.example.swissquote.domain.activity;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CustomerActivityReport(
        UUID customerId,
        CustomerActivitySummary summary,
        List<CustomerActivity> activities,
        CustomerActivityPage page
) {

    public CustomerActivityReport {
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        activities = List.copyOf(Objects.requireNonNull(activities, "activities must not be null"));
        Objects.requireNonNull(page, "page must not be null");
    }
}
