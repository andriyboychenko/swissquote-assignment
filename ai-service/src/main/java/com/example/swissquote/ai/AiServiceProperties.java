package com.example.swissquote.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.service")
public record AiServiceProperties(String internalToken) {
}
