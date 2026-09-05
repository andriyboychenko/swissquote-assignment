package com.example.swissquote.application.analysis;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicyDocumentServiceTests {

    private final PolicyDocumentRepository repository = mock(PolicyDocumentRepository.class);
    private final PolicyDocumentService service = new PolicyDocumentService(repository);

    @Test
    void getSectionReturnsDocumentSection() {
        PolicyDocumentSection section = new PolicyDocumentSection(
                "policy://policies/risk-signal-handling-v1.md#triggered-rule-response",
                "Triggered Rule Response",
                "Triggered risk rules should be summarized."
        );
        when(repository.findSection("risk-signal-handling-v1.md", "triggered-rule-response"))
                .thenReturn(Optional.of(section));

        assertThat(service.getSection("risk-signal-handling-v1.md", "triggered-rule-response"))
                .isEqualTo(section);
    }

    @Test
    void getSectionThrowsWhenPolicySectionDoesNotExist() {
        when(repository.findSection("risk-signal-handling-v1.md", "missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSection("risk-signal-handling-v1.md", "missing"))
                .isInstanceOf(PolicyDocumentNotFoundException.class)
                .hasMessage("Policy section was not found for policy://policies/risk-signal-handling-v1.md#missing");
    }
}
