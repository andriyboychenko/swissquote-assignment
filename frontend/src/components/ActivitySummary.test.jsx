import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ActivitySummary } from "./ActivitySummary";

describe("ActivitySummary", () => {
  it("renders customer activity totals by category", () => {
    render(
      <ActivitySummary
        summary={{
          totalActivities: 100,
          cardActivities: 33,
          paymentActivities: 34,
          cryptoActivities: 33,
          failedActivities: 10,
          pendingActivities: 10
        }}
      />
    );

    expect(screen.getByLabelText("Customer activity summary")).toBeInTheDocument();
    expect(screen.getByText("Total")).toBeInTheDocument();
    expect(screen.getByText("100")).toBeInTheDocument();
    expect(screen.getByText("Payments")).toBeInTheDocument();
    expect(screen.getByText("34")).toBeInTheDocument();
  });
});
