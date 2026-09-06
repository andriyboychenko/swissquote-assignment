package com.example.swissquote.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.mock")
public record MockAuthProperties(boolean enabled) {
}
