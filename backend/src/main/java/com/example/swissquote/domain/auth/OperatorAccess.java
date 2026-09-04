package com.example.swissquote.domain.auth;

public record OperatorAccess(
        String provider,
        boolean blocked,
        String blockReason
) {
}
