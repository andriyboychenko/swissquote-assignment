package com.example.swissquote;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AgentHarnessTests {

    @Test
    void agentHarnessDocumentsProjectVerificationWorkflow() throws IOException {
        Path root = findProjectRoot();

        String harness = Files.readString(root.resolve("docs/agent-harness/README.md"));

        assertThat(harness).contains("Agent Harness");
        assertThat(harness).contains("./scripts/agent-harness.sh");
        assertThat(harness).contains("./gradlew :backend:test");
        assertThat(harness).contains("npm test");
        assertThat(harness).contains("docker compose config --quiet");
        assertThat(harness).contains("AI Feature Rules");
        assertThat(harness).contains("Version and test prompts");
        assertThat(harness).contains("Require human review for high-impact decisions");
        assertThat(harness).contains("Git Handoff Commands");
        assertThat(harness).contains("git add <changed-files>");
        assertThat(harness).contains("git commit -m");
        assertThat(harness).contains("git push");
        assertThat(harness).contains("Auth Routes");
        assertThat(harness).contains("/oauth2/authorization/google");
        assertThat(harness).contains("Google login must be disabled and shown as unavailable");
        assertThat(harness).contains("GOOGLE_OAUTH_CLIENT_SECRET");
        assertThat(harness).contains("/mock-login?operator={operator}");
        assertThat(harness).contains("/api/auth/me");
        assertThat(harness).contains("CSRF Protection");
        assertThat(harness).contains("XSRF-TOKEN");
        assertThat(harness).contains("X-XSRF-TOKEN");
        assertThat(harness).contains("ordered from highest risk to lowest risk");
        assertThat(harness).contains("Update this harness whenever");
    }

    @Test
    void agentHarnessScriptRunsBackendFrontendAndComposeChecks() throws IOException {
        Path root = findProjectRoot();

        String script = Files.readString(root.resolve("scripts/agent-harness.sh"));

        assertThat(script).startsWith("#!/usr/bin/env sh");
        assertThat(script).contains("set -eu");
        assertThat(script).contains("./gradlew :backend:test");
        assertThat(script).contains("npm test");
        assertThat(script).contains("docker compose config --quiet");
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
