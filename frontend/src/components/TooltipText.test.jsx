import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TooltipText } from "./TooltipText";

describe("TooltipText", () => {
  it("stores tooltip copy in a focusable tooltip anchor", () => {
    render(<TooltipText ariaLabel="Tooltip trigger" tooltip="Full value">Short value</TooltipText>);

    expect(screen.getByText("Short value")).toHaveAttribute("data-tooltip", "Full value");
    expect(screen.getByText("Short value")).toHaveAttribute("tabindex", "0");
    expect(screen.getByLabelText("Tooltip trigger")).toBeInTheDocument();
  });
});
