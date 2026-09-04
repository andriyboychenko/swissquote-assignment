import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
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
        provider="Meta"
        description="Meta login will be added after Google"
        symbol="M"
        variant="meta"
        disabled
      />
    );

    expect(screen.getByRole("button", { name: /Continue with Meta/ })).toBeDisabled();
    expect(screen.getByText("Meta login will be added after Google")).toBeInTheDocument();
  });
});
