import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { AiAnalysisSummary } from "./AiAnalysisSummary";

describe("AiAnalysisSummary", () => {
  it("highlights triggered risk signals and total risk score", () => {
    render(
      <AiAnalysisSummary
        summary={`RISK ALERT SUMMARY
Customer ID: customer-1
Review Window: Latest 100 loaded activities

1. CONTRIBUTING SIGNALS:
   - Activity mix: 32 card, 38 payment, 30 crypto activities reviewed
   - Risk model: Found 53 triggered risk signals with total risk score 985.`}
      />
    );

    expect(screen.getByText("32")).toHaveClass("analysis-highlight");
    expect(screen.getByText("38")).toHaveClass("analysis-highlight");
    expect(screen.getByText("30")).toHaveClass("analysis-highlight");
    expect(screen.getByText("53")).toHaveClass("analysis-highlight");
    expect(screen.getByText("985")).toHaveClass("analysis-highlight");
    expect(screen.getByText(/card,/)).toBeInTheDocument();
    expect(screen.getByText(/payment,/)).toBeInTheDocument();
    expect(screen.getByText(/crypto activities reviewed/)).toBeInTheDocument();
    expect(screen.getByText(/triggered risk signals with total risk score/)).toBeInTheDocument();
    expect(screen.getByText(/RISK ALERT SUMMARY/)).toHaveClass("analysis-summary-text");
  });

  it("renders summaries without risk score metrics as plain text", () => {
    render(<AiAnalysisSummary summary="No suspicious activity was found." />);

    expect(screen.getByText("No suspicious activity was found.")).toBeInTheDocument();
  });
});
