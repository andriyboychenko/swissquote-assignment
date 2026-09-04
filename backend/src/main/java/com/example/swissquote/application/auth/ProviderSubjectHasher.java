package com.example.swissquote.application.auth;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Component
public class ProviderSubjectHasher {

    public String hash(String provider, String subject) {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(subject, "subject must not be null");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((provider + ":" + subject).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 hashing is not available", exception);
        }
    }
}
