import React from "react";
import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthSuccessPage } from "./AuthSuccessPage";

describe("AuthSuccessPage", () => {
  beforeEach(() => {
    Object.defineProperty(window, "localStorage", {
      configurable: true,
      value: {
        getItem: vi.fn(() => null),
        setItem: vi.fn(),
        clear: vi.fn()
      }
    });
  });

  it("renders the demo access disclaimer, operator menu, and customer dashboard", () => {
    render(<AuthSuccessPage operatorName="Demo Operator" />);

    expect(screen.getByRole("heading", { name: "Disclaimer" })).toBeInTheDocument();
    expect(screen.getByText("Welcome, Demo Operator")).toBeInTheDocument();
    expect(screen.getByText(/Since this is a demo/)).toBeInTheDocument();
    expect(screen.getByText(/accepted by a moderator/)).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Logout" })).toHaveAttribute("href", "/logout");
    expect(screen.getByRole("heading", { name: "Search activity by Customer ID" })).toBeInTheDocument();
  });
});
