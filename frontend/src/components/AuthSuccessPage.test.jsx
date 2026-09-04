import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { AuthSuccessPage } from "./AuthSuccessPage";

describe("AuthSuccessPage", () => {
  it("renders the demo access disclaimer and operator menu", () => {
    render(<AuthSuccessPage operatorName="Demo Operator" />);

    expect(screen.getByRole("heading", { name: "Welcome, Demo Operator" })).toBeInTheDocument();
    expect(screen.getByText(/Since this is a demo/)).toBeInTheDocument();
    expect(screen.getByText(/accepted by a moderator/)).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Logout" })).toHaveAttribute("href", "/logout");
  });
});
