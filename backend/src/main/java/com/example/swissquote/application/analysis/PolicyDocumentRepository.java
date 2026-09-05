package com.example.swissquote.application.analysis;

import java.util.Optional;

public interface PolicyDocumentRepository {

    Optional<PolicyDocumentSection> findSection(String documentName, String sectionAnchor);
}
