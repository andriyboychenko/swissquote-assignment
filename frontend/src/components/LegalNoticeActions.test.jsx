import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { LegalNoticeActions } from "./LegalNoticeActions";

describe("LegalNoticeActions", () => {
  it("opens the privacy and terms notices by key", () => {
    const onOpenNotice = vi.fn();

    render(<LegalNoticeActions onOpenNotice={onOpenNotice} />);

    fireEvent.click(screen.getByRole("button", { name: "Privacy & Data" }));
    fireEvent.click(screen.getByRole("button", { name: "Terms & Conditions" }));

    expect(onOpenNotice).toHaveBeenNthCalledWith(1, "privacy");
    expect(onOpenNotice).toHaveBeenNthCalledWith(2, "terms");
  });
});
