import React from "react";
import { AiAnalysisSummary, isRiskAlertSummary } from "./AiAnalysisSummary";
import { PolicyEvidenceLink } from "./PolicyEvidenceLink";

export function AiAnalysisResult({ analysis, onApplyRecommendedFilters = undefined }) {
  if (!analysis.result) {
    return (
      <div className="analysis-result">
        <span className={`analysis-status status-${analysis.status.toLowerCase()}`}>Status: {analysis.status}</span>
        {analysis.failureReason ? <p className="analysis-error">{analysis.failureReason}</p> : null}
      </div>
    );
  }

  const hasStructuredSummary = isRiskAlertSummary(analysis.result.summary);

  return (
    <div className="analysis-result">
      <div className="analysis-risk-row">
        <span className={`risk-level risk-${analysis.result.riskLevel.toLowerCase()}`}>
          Risk: {analysis.result.riskLevel}
        </span>
        <span className={`analysis-status status-${analysis.status.toLowerCase()}`}>Status: {analysis.status}</span>
      </div>
      <AiAnalysisSummary summary={analysis.result.summary} />
      {hasStructuredSummary ? null : <p>{analysis.result.recommendations}</p>}
      {onApplyRecommendedFilters ? (
        <button className="recommended-filter-button" type="button" onClick={onApplyRecommendedFilters}>
          Review flagged activity
          <span>Alt+R</span>
        </button>
      ) : null}
      {analysis.result.evidence.length > 0 ? (
        <ul className="analysis-evidence">
          {analysis.result.evidence.map((evidence) => (
            <li key={evidence.evidenceId}>
              <PolicyEvidenceLink sourceReference={evidence.sourceReference} />
              <span>{evidence.excerpt}</span>
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
