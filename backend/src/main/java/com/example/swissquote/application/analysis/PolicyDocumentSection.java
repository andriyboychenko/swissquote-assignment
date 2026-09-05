package com.example.swissquote.application.analysis;

import java.util.Objects;

public record PolicyDocumentSection(
        String sourceReference,
        String title,
        String content
) {

    public PolicyDocumentSection {
        Objects.requireNonNull(sourceReference, "sourceReference must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(content, "content must not be null");
    }
}
