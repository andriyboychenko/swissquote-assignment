package com.example.swissquote.interfaces.rest;

import com.example.swissquote.application.analysis.PolicyDocumentSection;
import com.example.swissquote.application.analysis.PolicyDocumentService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicyDocumentControllerTests {

    @Test
    void sectionReturnsMappedPolicyDocumentSection() {
        PolicyDocumentService service = mock(PolicyDocumentService.class);
        PolicyDocumentSection section = new PolicyDocumentSection(
                "policy://policies/customer-activity-risk-review-v1.md#activity-overview",
                "Activity Overview",
                "The overview should group activity."
        );
        when(service.getSection("customer-activity-risk-review-v1.md", "activity-overview"))
                .thenReturn(section);
        PolicyDocumentController controller = new PolicyDocumentController(service);

        PolicyDocumentResponse response = controller.section(
                "customer-activity-risk-review-v1.md",
                "activity-overview"
        );

        assertThat(response.sourceReference()).isEqualTo(section.sourceReference());
        assertThat(response.title()).isEqualTo("Activity Overview");
        assertThat(response.content()).isEqualTo("The overview should group activity.");
    }
}
