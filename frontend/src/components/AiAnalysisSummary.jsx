import React from "react";

const ALERT_SUMMARY_TITLES = ["RISK ALERT SUMMARY", "[RISK ALERT SUMMARY]"];
const ACTIVITY_MIX_PATTERN = /(\d+)( card, )(\d+)( payment, )(\d+)( crypto activities reviewed)/g;
const RISK_SIGNAL_SUMMARY_PATTERN = /(Found )(\d+)( triggered risk signals with average risk score )([\d.]+)(\.)/g;

export function AiAnalysisSummary({ summary }) {
  return <p className="analysis-summary-text">{renderHighlightedSummary(summary)}</p>;
}

export function isRiskAlertSummary(summary) {
  return ALERT_SUMMARY_TITLES.some((title) => summary.startsWith(title));
}

function renderHighlightedSummary(summary) {
  return renderActivityMixHighlights(summary).flatMap((part, index) => (
    typeof part === "string" ? renderRiskScoreHighlights(part, index) : part
  ));
}

function renderActivityMixHighlights(text) {
  return renderPatternHighlights(
    text,
    ACTIVITY_MIX_PATTERN,
    (match, key) => ([
      <strong className="analysis-highlight" key={`${key}-card`}>{match[1]}</strong>,
      match[2],
      <strong className="analysis-highlight" key={`${key}-payment`}>{match[3]}</strong>,
      match[4],
      <strong className="analysis-highlight" key={`${key}-crypto`}>{match[5]}</strong>,
      match[6]
    ])
  );
}

function renderRiskScoreHighlights(text, keyPrefix) {
  return renderPatternHighlights(
    text,
    RISK_SIGNAL_SUMMARY_PATTERN,
    (match, key) => ([
      match[1],
      <strong className="analysis-highlight" key={`${keyPrefix}-${key}-signals`}>{match[2]}</strong>,
      match[3],
      <strong className="analysis-highlight" key={`${keyPrefix}-${key}-score`}>{match[4]}</strong>,
      match[5]
    ])
  );
}

function renderPatternHighlights(text, pattern, renderMatch) {
  const parts = [];
  let lastIndex = 0;

  for (const match of text.matchAll(pattern)) {
    if (match.index > lastIndex) {
      parts.push(text.slice(lastIndex, match.index));
    }

    parts.push(...renderMatch(match, parts.length));
    lastIndex = match.index + match[0].length;
  }

  if (lastIndex < text.length) {
    parts.push(text.slice(lastIndex));
  }

  return parts.length > 0 ? parts : [text];
}
