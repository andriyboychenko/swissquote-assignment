import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { DemoOperatorModal } from "./DemoOperatorModal";

describe("DemoOperatorModal", () => {
  it("renders nothing when closed", () => {
    const { container } = render(<DemoOperatorModal isOpen={false} onClose={vi.fn()} />);

    expect(container).toBeEmptyDOMElement();
  });

  it("renders available mock operators and closes on request", () => {
    const onClose = vi.fn();

    render(<DemoOperatorModal isOpen={true} onClose={onClose} />);

    expect(screen.getByRole("dialog", { name: "Choose demo operator" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /Sarah Connor/ })).toHaveAttribute(
      "href",
      "/mock-login?operator=analyst-one"
    );
    expect(screen.getByRole("link", { name: /Lisbeth Salander/ })).toHaveAttribute(
      "href",
      "/mock-login?operator=analyst-two"
    );
    expect(screen.getByRole("link", { name: /John McClane/ })).toHaveAttribute(
      "href",
      "/mock-login?operator=risk-reviewer"
    );

    fireEvent.click(screen.getByRole("button", { name: "Close demo operator chooser" }));

    expect(onClose).toHaveBeenCalledOnce();
  });
});
