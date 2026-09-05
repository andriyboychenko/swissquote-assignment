package com.example.swissquote.application.analysis;

public class PolicyDocumentNotFoundException extends RuntimeException {

    public PolicyDocumentNotFoundException(String sourceReference) {
        super("Policy section was not found for " + sourceReference);
    }
}
