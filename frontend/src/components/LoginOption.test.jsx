import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { LoginOption } from "./LoginOption";

describe("LoginOption", () => {
  it("renders a named provider login button", () => {
    render(
      <LoginOption
        provider="Google"
        description="Continue with your Google account"
        href="/oauth2/authorization/google"
        symbol="G"
        variant="google"
      />
    );

    expect(screen.getByRole("link", { name: /Continue with Google/ })).toHaveAttribute(
      "href",
      "/oauth2/authorization/google"
    );
    expect(screen.getByText("Continue with your Google account")).toBeInTheDocument();
  });

  it("renders a disabled provider option when auth is not configured", () => {
    render(
      <LoginOption
        provider="Provider"
        description="Provider login is not configured"
        symbol="M"
        variant="provider"
        disabled
      />
    );

    expect(screen.getByRole("button", { name: /Continue with Provider/ })).toBeDisabled();
    expect(screen.getByText("Provider login is not configured")).toBeInTheDocument();
  });

  it("renders an actionable provider button", () => {
    const onClick = vi.fn();

    render(
      <LoginOption
        provider="Demo operator"
        description="Choose a local mock operator session"
        symbol="D"
        variant="mock"
        onClick={onClick}
      />
    );

    fireEvent.click(screen.getByRole("button", { name: /Continue with Demo operator/ }));

    expect(onClick).toHaveBeenCalledOnce();
  });
});
