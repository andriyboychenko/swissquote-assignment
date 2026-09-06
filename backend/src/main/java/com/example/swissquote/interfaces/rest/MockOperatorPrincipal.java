package com.example.swissquote.interfaces.rest;

import java.security.Principal;
import java.util.Objects;

public record MockOperatorPrincipal(String subject, String displayName) implements Principal {

    public MockOperatorPrincipal {
        Objects.requireNonNull(subject, "subject must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
    }

    @Override
    public String getName() {
        return subject;
    }
}
