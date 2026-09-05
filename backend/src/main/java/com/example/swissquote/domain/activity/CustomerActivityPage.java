package com.example.swissquote.domain.activity;

public record CustomerActivityPage(
        int limit,
        int offset,
        int returnedActivities,
        boolean hasMore,
        int nextOffset
) {
}
