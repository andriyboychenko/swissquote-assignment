package com.example.swissquote;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class InfrastructureConfigurationTests {

    @Test
    void dockerComposeDefinesLoadBalancerAndApiGateway() throws IOException {
        String compose = Files.readString(findProjectRoot().resolve("docker-compose.yml"));

        assertThat(compose).contains("api-gateway:");
        assertThat(compose).contains("load-balancer:");
        assertThat(compose).contains("./docker/nginx/api-gateway.conf:/etc/nginx/conf.d/default.conf:ro");
        assertThat(compose).contains("./docker/nginx/load-balancer.conf:/etc/nginx/conf.d/default.conf:ro");
        assertThat(compose).contains("\"3000:80\"");
    }

    @Test
    void dockerComposeUsesOnlyGoogleOAuthClientSecret() throws IOException {
        String compose = Files.readString(findProjectRoot().resolve("docker-compose.yml"));

        assertThat(compose).contains("GOOGLE_OAUTH_CLIENT_SECRET: ${GOOGLE_OAUTH_CLIENT_SECRET:-}");
    }

    @Test
    void loadBalancerRoutesApiTrafficToApiGateway() throws IOException {
        String config = Files.readString(findProjectRoot().resolve("docker/nginx/load-balancer.conf"));

        assertThat(config).contains("resolver 127.0.0.11 valid=10s ipv6=off");
        assertThat(config).contains("set $frontend_upstream frontend:80");
        assertThat(config).contains("set $api_gateway_upstream api-gateway:8080");
        assertThat(config).contains("location /api/");
        assertThat(config).contains("proxy_pass http://$api_gateway_upstream");
        assertThat(config).contains("location /oauth2/");
        assertThat(config).contains("location /login/");
        assertThat(config).contains("location /mock-login");
        assertThat(config).contains("location /logout");
        assertThat(config).contains("proxy_set_header Host $http_host");
        assertThat(config).contains("proxy_set_header X-Forwarded-Host $http_host");
        assertThat(config).contains("proxy_set_header X-Forwarded-Port 3000");
    }

    @Test
    void apiGatewayRoutesApiTrafficToBackend() throws IOException {
        String config = Files.readString(findProjectRoot().resolve("docker/nginx/api-gateway.conf"));

        assertThat(config).contains("listen 8080");
        assertThat(config).contains("resolver 127.0.0.11 valid=10s ipv6=off");
        assertThat(config).contains("set $backend_upstream backend:8080");
        assertThat(config).contains("location /api/");
        assertThat(config).contains("proxy_pass http://$backend_upstream");
        assertThat(config).contains("location /actuator/");
        assertThat(config).contains("proxy_pass http://$backend_upstream");
        assertThat(config).contains("location /oauth2/");
        assertThat(config).contains("location /login/");
        assertThat(config).contains("location /mock-login");
        assertThat(config).contains("location /logout");
        assertThat(config).contains("proxy_set_header Host $http_host");
        assertThat(config).contains("proxy_set_header X-Forwarded-Host $http_host");
        assertThat(config).contains("proxy_set_header X-Forwarded-Port $http_x_forwarded_port");
    }

    @Test
    void frontendNginxDoesNotProxyApiTraffic() throws IOException {
        String config = Files.readString(findProjectRoot().resolve("docker/nginx/default.conf"));

        assertThat(config).doesNotContain("location /api/");
        assertThat(config).doesNotContain("proxy_pass http://backend");
        assertThat(config).contains("try_files $uri $uri/ /index.html");
    }

    @Test
    void springSecurityUsesCookieBackedCsrfProtection() throws IOException {
        String config = Files.readString(findProjectRoot().resolve("backend/src/main/java/com/example/swissquote/config/SecurityConfig.java"));

        assertThat(config).contains("CookieCsrfTokenRepository.withHttpOnlyFalse()");
        assertThat(config).contains("CsrfTokenRequestAttributeHandler");
        assertThat(config).contains("csrfTokenRepository");
        assertThat(config).contains("CsrfCookieFilter");
        assertThat(config).doesNotContain("csrf.disable()");
    }

    private static Path findProjectRoot() {
        Path current = Path.of("").toAbsolutePath();
        if (Files.exists(current.resolve("docker-compose.yml"))) {
            return current;
        }

        Path parent = current.getParent();
        if (parent != null && Files.exists(parent.resolve("docker-compose.yml"))) {
            return parent;
        }

        throw new IllegalStateException("Could not find project root containing docker-compose.yml");
    }
}
