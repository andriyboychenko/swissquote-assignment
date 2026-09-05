import React from "react";
import { AlertTriangle } from "lucide-react";
import { TooltipText } from "./TooltipText";

export function ActivityRiskIndicator({ riskIndicators }) {
  if (riskIndicators.length === 0) {
    return null;
  }

  const highestSeverity = riskIndicators.some((indicator) => indicator.severity === "HIGH") ? "high" : "medium";
  const tooltip = riskIndicators
    .map((indicator) => [
      `${indicator.ruleName}: risk score ${Number(indicator.scoreContribution).toFixed(2)}`,
      `Recommendation: ${recommendationFor(indicator)}`
    ].join("\n"))
    .join("\n");

  return (
    <TooltipText className="risk-indicator-tooltip" tooltip={tooltip}>
      <span className={`row-risk-indicator row-risk-${highestSeverity}`} aria-label="Suspicious activity">
        <AlertTriangle size={14} aria-hidden="true" />
        {riskIndicators.length}
      </span>
    </TooltipText>
  );
}

function recommendationFor(indicator) {
  const ruleName = indicator.ruleName.toLowerCase();

  if (ruleName.includes("card-not-present") || ruleName.includes("card")) {
    return "Verify card-not-present context and check recent failed authorization attempts.";
  }

  if (ruleName.includes("crypto") || ruleName.includes("wallet")) {
    return "Review wallet history, exchange context, and recent fiat-to-crypto movement.";
  }

  if (ruleName.includes("cross-border") || ruleName.includes("wire") || ruleName.includes("payment")) {
    return "Validate beneficiary details, bank country, and source-of-funds context.";
  }

  if (indicator.severity === "HIGH") {
    return "Escalate to a senior operator before approving related follow-up activity.";
  }

  return "Review this activity together with nearby transactions and policy evidence.";
}
