package com.example.swissquote.application.analysis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiAnalysisServiceProperties(
        boolean remoteEnabled,
        String serviceUrl,
        String serviceToken
) {
}
