import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ActivityMovementIndicator, getActivityMovement } from "./ActivityMovementIndicator";

describe("ActivityMovementIndicator", () => {
  it("maps pending activity to an unknown movement indicator", () => {
    const movement = getActivityMovement({
      activityType: "PAYMENT",
      channel: "SWIFT",
      status: "Pending"
    });

    render(<ActivityMovementIndicator movement={movement} />);

    expect(screen.getByLabelText("Pending movement")).toHaveClass("movement-unknown");
    expect(screen.getByLabelText("Pending movement")).toHaveAttribute(
      "data-tooltip",
      "This activity is pending, so the final account movement is not confirmed yet."
    );
  });

  it("maps completed debit card activity to outgoing movement", () => {
    const movement = getActivityMovement({
      activityType: "CARD",
      channel: "Debit",
      status: "Completed"
    });

    render(<ActivityMovementIndicator movement={movement} />);

    expect(screen.getByLabelText("Removed from account")).toHaveClass("movement-down");
  });
});
