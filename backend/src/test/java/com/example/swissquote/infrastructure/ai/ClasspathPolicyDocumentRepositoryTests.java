package com.example.swissquote.infrastructure.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClasspathPolicyDocumentRepositoryTests {

    private final ClasspathPolicyDocumentRepository repository = new ClasspathPolicyDocumentRepository();

    @Test
    void findSectionReadsAllowedPolicyDocumentSection() {
        var section = repository.findSection(
                "customer-activity-risk-review-v1.md",
                "operator-review-triggers"
        );

        assertThat(section).isPresent();
        assertThat(section.get().sourceReference())
                .isEqualTo("policy://policies/customer-activity-risk-review-v1.md#operator-review-triggers");
        assertThat(section.get().title()).isEqualTo("Operator Review Triggers");
        assertThat(section.get().content()).contains("Open an operator risk review");
    }

    @Test
    void findSectionRejectsUnknownDocumentsAndUnsafeAnchors() {
        assertThat(repository.findSection("unknown.md", "operator-review-triggers")).isEmpty();
        assertThat(repository.findSection(
                "customer-activity-risk-review-v1.md",
                "../operator-review-triggers"
        )).isEmpty();
    }
}
