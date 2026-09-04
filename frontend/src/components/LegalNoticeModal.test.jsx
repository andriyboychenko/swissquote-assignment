import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { LegalNoticeModal } from "./LegalNoticeModal";

describe("LegalNoticeModal", () => {
  it("does not render without notice content", () => {
    render(<LegalNoticeModal notice={null} onClose={vi.fn()} />);

    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("renders the selected notice and closes it", () => {
    const onClose = vi.fn();
    const notice = {
      title: "Privacy & Data",
      body: "Only pseudonymous operator identifiers are persisted."
    };

    render(<LegalNoticeModal notice={notice} onClose={onClose} />);

    expect(screen.getByRole("dialog", { name: "Privacy & Data" })).toBeInTheDocument();
    expect(screen.getByText(notice.body)).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Close legal notice" }));

    expect(onClose).toHaveBeenCalledOnce();
  });
});
