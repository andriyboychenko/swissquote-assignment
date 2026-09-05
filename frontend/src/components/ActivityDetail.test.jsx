import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ActivityDetail } from "./ActivityDetail";

describe("ActivityDetail", () => {
  it("renders decline reason on a separate line", () => {
    render(<ActivityDetail detail="PAN ****1234, MCC 5411, Decline: Suspected fraud" />);

    expect(screen.getByText("PAN ****1234, MCC 5411")).toBeInTheDocument();
    expect(screen.getByText("Decline: Suspected fraud")).toHaveClass("decline-detail");
  });

  it("renders detail as one line when there is no decline reason", () => {
    render(<ActivityDetail detail="Receiver country DE" />);

    expect(screen.getByText("Receiver country DE")).toBeInTheDocument();
  });
});
