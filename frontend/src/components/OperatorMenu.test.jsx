import React from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { OperatorMenu } from "./OperatorMenu";

describe("OperatorMenu", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/";
  });

  it("shows the operator name and posts logout with CSRF protection", async () => {
    document.cookie = "XSRF-TOKEN=token-123; path=/";
    const fetchMock = vi.fn().mockResolvedValue({ ok: true });
    vi.stubGlobal("fetch", fetchMock);
    vi.stubGlobal("location", { assign: vi.fn() });

    render(<OperatorMenu operatorName="Demo Operator" />);

    expect(screen.getByRole("navigation", { name: "Operator menu" })).toBeInTheDocument();
    expect(screen.getByText("Demo Operator")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Logout" }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith("/logout", {
      credentials: "include",
      headers: {
        "X-XSRF-TOKEN": "token-123"
      },
      method: "POST"
    }));
    expect(window.location.assign).toHaveBeenCalledWith("/");
  });
});
