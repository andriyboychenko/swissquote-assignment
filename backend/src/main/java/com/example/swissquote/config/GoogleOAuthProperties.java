package com.example.swissquote.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.auth.google")
public record GoogleOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri
) {

    public boolean configured() {
        return StringUtils.hasText(clientId) && StringUtils.hasText(clientSecret);
    }
}
