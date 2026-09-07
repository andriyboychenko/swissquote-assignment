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
    expect(screen.getByText("These customer IDs may be useful for a demo:")).toBeInTheDocument();
    expect(screen.getByText("005514e6-1ebe-8010-de91-aff66d1d9484")).toBeInTheDocument();
    expect(screen.getAllByRole("listitem").map((item) => item.textContent)).toEqual([
      "0ddc4d69-0dcf-fba9-15c2-88a68e6665de",
      "0abe215d-4832-1215-fe7a-264bfb844be9",
      "0c534877-7dee-ed33-5278-68e39c8fe785",
      "005514e6-1ebe-8010-de91-aff66d1d9484",
      "0a3ab26d-12b1-0efc-65d4-a2d6cc72ec67"
    ]);
    expect(screen.getByRole("button", { name: "Hide" })).toBeInTheDocument();
  });

  it("minimizes the notice and remembers that state", () => {
    render(<AuthenticationNotice operatorName="Demo Operator" />);

    fireEvent.click(screen.getByRole("button", { name: "Hide" }));

    expect(screen.queryByText("Welcome, Demo Operator")).not.toBeInTheDocument();
    expect(screen.queryByText(/Since this is a demo/)).not.toBeInTheDocument();
    expect(screen.queryByText("These customer IDs may be useful for a demo:")).not.toBeInTheDocument();
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
