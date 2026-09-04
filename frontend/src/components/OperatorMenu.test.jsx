import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { OperatorMenu } from "./OperatorMenu";

describe("OperatorMenu", () => {
  it("shows the operator name and logout option", () => {
    render(<OperatorMenu operatorName="Demo Operator" />);

    expect(screen.getByRole("navigation", { name: "Operator menu" })).toBeInTheDocument();
    expect(screen.getByText("Demo Operator")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Logout" })).toHaveAttribute("href", "/logout");
  });
});
