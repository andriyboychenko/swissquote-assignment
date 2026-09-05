import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthenticationNotice } from "./AuthenticationNotice";

describe("AuthenticationNotice", () => {
  const storage = new Map();

  beforeEach(() => {
    storage.clear();
    Object.defineProperty(window, "localStorage", {
      configurable: true,
      value: {
        getItem: vi.fn((key) => storage.get(key) ?? null),
        setItem: vi.fn((key, value) => storage.set(key, value)),
        clear: vi.fn(() => storage.clear())
      }
    });
  });

  it("renders the full authentication notice by default", () => {
    render(<AuthenticationNotice operatorName="Demo Operator" />);

    expect(screen.getByRole("heading", { name: "Note" })).toBeInTheDocument();
    expect(screen.getByText("Welcome, Demo Operator")).toBeInTheDocument();
    expect(screen.getByText(/Since this is a demo/)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Hide" })).toBeInTheDocument();
  });

  it("minimizes the notice and remembers that state", () => {
    render(<AuthenticationNotice operatorName="Demo Operator" />);

    fireEvent.click(screen.getByRole("button", { name: "Hide" }));

    expect(screen.queryByText("Welcome, Demo Operator")).not.toBeInTheDocument();
    expect(screen.queryByText(/Since this is a demo/)).not.toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Note" })).toBeInTheDocument();
    expect(storage.get("swissquote-authentication-notice-minimized")).toBe("true");
  });

  it("starts minimized when the operator minimized it before", () => {
    storage.set("swissquote-authentication-notice-minimized", "true");

    render(<AuthenticationNotice operatorName="Demo Operator" />);

    expect(screen.getByRole("heading", { name: "Note" })).toBeInTheDocument();
    expect(screen.queryByText("Welcome, Demo Operator")).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Show" })).toBeInTheDocument();
  });
});
