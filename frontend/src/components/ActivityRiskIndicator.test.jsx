import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ActivityRiskIndicator } from "./ActivityRiskIndicator";

describe("ActivityRiskIndicator", () => {
  it("renders nothing when the row has no risk indicators", () => {
    const { container } = render(<ActivityRiskIndicator riskIndicators={[]} />);

    expect(container).toBeEmptyDOMElement();
  });

  it("renders suspicious activity count and tooltip", () => {
    render(
      <ActivityRiskIndicator
        riskIndicators={[
          {
            ruleName: "High-value payment",
            severity: "HIGH",
            scoreContribution: 25
          },
          {
            ruleName: "Cross-border payment",
            severity: "MEDIUM",
            scoreContribution: 15
          }
        ]}
      />
    );

    expect(screen.getByLabelText("Suspicious activity")).toHaveClass("row-risk-high");
    expect(screen.getByText("2")).toBeInTheDocument();
    expect(screen.getByLabelText("Suspicious activity").closest(".risk-indicator-tooltip")).toHaveAttribute(
      "data-tooltip",
      [
        "High-value payment: risk score 25.00",
        "Recommendation: Validate beneficiary details, bank country, and source-of-funds context.",
        "Cross-border payment: risk score 15.00",
        "Recommendation: Validate beneficiary details, bank country, and source-of-funds context."
      ].join("\n")
    );
  });

  it("uses card-specific and crypto-specific tooltip recommendations", () => {
    render(
      <ActivityRiskIndicator
        riskIndicators={[
          {
            ruleName: "Card-not-present high-value activity",
            severity: "HIGH",
            scoreContribution: 20
          },
          {
            ruleName: "High-value crypto transfer",
            severity: "HIGH",
            scoreContribution: 20
          }
        ]}
      />
    );

    expect(screen.getByLabelText("Suspicious activity").closest(".risk-indicator-tooltip")).toHaveAttribute(
      "data-tooltip",
      [
        "Card-not-present high-value activity: risk score 20.00",
        "Recommendation: Verify card-not-present context and check recent failed authorization attempts.",
        "High-value crypto transfer: risk score 20.00",
        "Recommendation: Review wallet history, exchange context, and recent fiat-to-crypto movement."
      ].join("\n")
    );
  });
});
