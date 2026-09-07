package com.example.swissquote.application.analysis;

import java.util.Objects;

public record RemoteAiAnalysisRequest(
        AiAnalysisSnapshot snapshot,
        String operatorProvider,
        String operatorSubject,
        String operatorDisplayName
) {

    public RemoteAiAnalysisRequest {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
    }
}
