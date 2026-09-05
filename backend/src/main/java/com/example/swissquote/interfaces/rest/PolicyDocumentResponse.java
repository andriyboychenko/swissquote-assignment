package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.analysis.PolicyDocumentSection;

public record PolicyDocumentResponse(
        String sourceReference,
        String title,
        String content
) {

    static PolicyDocumentResponse fromDomain(PolicyDocumentSection section) {
        return new PolicyDocumentResponse(
                section.sourceReference(),
                section.title(),
                section.content()
        );
    }
}
