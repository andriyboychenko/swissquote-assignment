package com.example.swissquote.application.analysis;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class PolicyDocumentService {

    private final PolicyDocumentRepository policyDocumentRepository;

    public PolicyDocumentService(PolicyDocumentRepository policyDocumentRepository) {
        this.policyDocumentRepository = policyDocumentRepository;
    }

    @Transactional(readOnly = true)
    public PolicyDocumentSection getSection(String documentName, String sectionAnchor) {
        Objects.requireNonNull(documentName, "documentName must not be null");
        Objects.requireNonNull(sectionAnchor, "sectionAnchor must not be null");
        return policyDocumentRepository.findSection(documentName, sectionAnchor)
                .orElseThrow(() -> new PolicyDocumentNotFoundException(
                        "policy://policies/" + documentName + "#" + sectionAnchor
                ));
    }
}
