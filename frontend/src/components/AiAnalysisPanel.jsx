import React, { useState } from "react";
import { ActivityDateTime } from "./ActivityDateTime";
import { AiAnalysisResult } from "./AiAnalysisResult";

const PREVIOUS_ANALYSES_LIMIT = 5;

export function AiAnalysisPanel({
  analyses,
  error,
  isLoading,
  isRequesting,
  onApplyRecommendedFilters,
  onRequestAnalysis
}) {
  const [selectedAnalysisId, setSelectedAnalysisId] = useState("");
  const [isHistoryExpanded, setIsHistoryExpanded] = useState(false);
  const [isPanelCollapsed, setIsPanelCollapsed] = useState(false);
  const selectedAnalysis = selectedAnalysisId
    ? analyses.find((analysis) => analysis.analysisRequestId === selectedAnalysisId)
    : null;
  const displayedAnalysis = selectedAnalysis ?? analyses[0] ?? null;
  const previousAnalyses = analyses.filter((analysis) => (
    analysis.analysisRequestId !== displayedAnalysis?.analysisRequestId
  ));
  const hasMorePreviousAnalyses = previousAnalyses.length > PREVIOUS_ANALYSES_LIMIT;
  const visiblePreviousAnalyses = isHistoryExpanded
    ? previousAnalyses
    : previousAnalyses.slice(0, PREVIOUS_ANALYSES_LIMIT);

  function handleShowLatest() {
    setSelectedAnalysisId("");
  }

  function handleToggleHistoryExpanded() {
    setIsHistoryExpanded((currentValue) => !currentValue);
  }

  function handleTogglePanelCollapsed() {
    setIsPanelCollapsed((currentValue) => !currentValue);
  }

  return (
    <section className="ai-analysis-panel" aria-labelledby="ai-analysis-title">
      <div className="ai-analysis-header">
        <div>
          <p className="eyebrow">AI risk analysis</p>
          <h3 id="ai-analysis-title">Customer activity findings</h3>
        </div>
        <div className="ai-analysis-actions">
          {isPanelCollapsed ? null : (
            <button type="button" onClick={onRequestAnalysis} disabled={isRequesting}>
              {isRequesting ? "Analyzing" : "Request AI analysis"}
            </button>
          )}
          <button
            aria-controls="ai-analysis-content"
            aria-expanded={!isPanelCollapsed}
            aria-label={isPanelCollapsed ? "Show AI risk analysis" : "Hide AI risk analysis"}
            className="analysis-toggle"
            type="button"
            onClick={handleTogglePanelCollapsed}
          >
            {isPanelCollapsed ? "Show" : "Hide"}
          </button>
        </div>
      </div>

      {isPanelCollapsed ? null : (
        <div className="ai-analysis-content" id="ai-analysis-content">
          {isLoading ? <p className="analysis-state">Loading previous analyses...</p> : null}
          {error ? <p className="analysis-error">{error}</p> : null}
          {!isLoading && !error && !displayedAnalysis ? (
            <p className="analysis-state">No AI analysis has been requested for this customer yet.</p>
          ) : null}
          {displayedAnalysis ? (
            <AiAnalysisResult
              analysis={displayedAnalysis}
              onApplyRecommendedFilters={onApplyRecommendedFilters}
            />
          ) : null}
          {previousAnalyses.length > 0 ? (
            <div className="analysis-history">
              <div className="analysis-history-header">
                <h4>Previous analyses</h4>
                {selectedAnalysis ? (
                  <button type="button" onClick={handleShowLatest}>
                    Show latest analysis
                  </button>
                ) : null}
              </div>
              <ul>
                {visiblePreviousAnalyses.map((analysis) => (
                  <li key={analysis.analysisRequestId}>
                    <span className={`analysis-status status-${analysis.status.toLowerCase()}`}>
                      Status: {analysis.status}
                    </span>
                    <span>{analysis.requestedByOperatorDisplayName || "Unknown operator"}</span>
                    <ActivityDateTime value={analysis.requestedAt} />
                    <button type="button" onClick={() => setSelectedAnalysisId(analysis.analysisRequestId)}>
                      View
                    </button>
                  </li>
                ))}
              </ul>
              {hasMorePreviousAnalyses ? (
                <button type="button" onClick={handleToggleHistoryExpanded}>
                  {isHistoryExpanded ? "Show fewer analyses" : `Show ${previousAnalyses.length - PREVIOUS_ANALYSES_LIMIT} more`}
                </button>
              ) : null}
            </div>
          ) : null}
        </div>
      )}
    </section>
  );
}
