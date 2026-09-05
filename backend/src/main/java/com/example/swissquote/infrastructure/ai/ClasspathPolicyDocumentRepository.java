package com.example.swissquote.infrastructure.ai;

import com.example.swissquote.application.analysis.PolicyDocumentRepository;
import com.example.swissquote.application.analysis.PolicyDocumentSection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

@Repository
public class ClasspathPolicyDocumentRepository implements PolicyDocumentRepository {

    private static final Set<String> ALLOWED_DOCUMENTS = Set.of(
            "customer-activity-risk-review-v1.md",
            "crypto-asset-monitoring-v1.md",
            "payment-cross-border-review-v1.md",
            "risk-signal-handling-v1.md"
    );

    @Override
    public Optional<PolicyDocumentSection> findSection(String documentName, String sectionAnchor) {
        if (!ALLOWED_DOCUMENTS.contains(documentName) || !isSafeAnchor(sectionAnchor)) {
            return Optional.empty();
        }

        ClassPathResource resource = new ClassPathResource("policies/" + documentName);
        if (!resource.exists()) {
            return Optional.empty();
        }

        try {
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            return extractSection(documentName, sectionAnchor, content);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read policy document " + documentName, exception);
        }
    }

    private static Optional<PolicyDocumentSection> extractSection(
            String documentName,
            String sectionAnchor,
            String content
    ) {
        String heading = "## " + sectionAnchor;
        int headingStart = content.indexOf(heading);
        if (headingStart < 0) {
            return Optional.empty();
        }

        int bodyStart = content.indexOf('\n', headingStart);
        int nextHeading = content.indexOf("\n## ", bodyStart + 1);
        String sectionContent = content.substring(
                bodyStart + 1,
                nextHeading < 0 ? content.length() : nextHeading
        ).trim();
        return Optional.of(new PolicyDocumentSection(
                "policy://policies/" + documentName + "#" + sectionAnchor,
                toTitle(sectionAnchor),
                sectionContent
        ));
    }

    private static boolean isSafeAnchor(String sectionAnchor) {
        return sectionAnchor.matches("[a-z0-9-]+");
    }

    private static String toTitle(String sectionAnchor) {
        String[] words = sectionAnchor.split("-");
        StringBuilder title = new StringBuilder();
        for (String word : words) {
            if (!title.isEmpty()) {
                title.append(' ');
            }
            title.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return title.toString();
    }
}
