import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ActivityDateTime } from "./ActivityDateTime";

describe("ActivityDateTime", () => {
  it("renders date and time as separate readable values", () => {
    render(<ActivityDateTime value="2026-09-04T12:00:00Z" />);

    expect(screen.getByText(/2026/)).toBeInTheDocument();
    expect(screen.getByText(/\d{2}:\d{2}/)).toBeInTheDocument();
  });
});
