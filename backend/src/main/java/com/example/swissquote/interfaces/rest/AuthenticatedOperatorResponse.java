package com.example.swissquote.interfaces.rest;

public record AuthenticatedOperatorResponse(
        String name,
        String email,
        String provider,
        boolean authenticated,
        boolean blocked,
        String blockReason,
        boolean googleLoginEnabled
) {

    public static AuthenticatedOperatorResponse anonymous(boolean googleLoginEnabled) {
        return new AuthenticatedOperatorResponse(null, null, null, false, false, null, googleLoginEnabled);
    }
}
