package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.analysis.PolicyDocumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policies")
public class PolicyDocumentController {

    private final PolicyDocumentService policyDocumentService;

    public PolicyDocumentController(PolicyDocumentService policyDocumentService) {
        this.policyDocumentService = policyDocumentService;
    }

    @GetMapping("/{documentName}/sections/{sectionAnchor}")
    public PolicyDocumentResponse section(
            @PathVariable String documentName,
            @PathVariable String sectionAnchor
    ) {
        return PolicyDocumentResponse.fromDomain(policyDocumentService.getSection(documentName, sectionAnchor));
    }
}
