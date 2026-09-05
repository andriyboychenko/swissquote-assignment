import React from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { PolicyEvidenceLink } from "./PolicyEvidenceLink";
import { fetchPolicySection } from "../api/policiesApi";

vi.mock("../api/policiesApi", () => ({
  fetchPolicySection: vi.fn()
}));

describe("PolicyEvidenceLink", () => {
  beforeEach(() => {
    fetchPolicySection.mockReset();
  });

  it("opens a policy section modal from the evidence reference", async () => {
    fetchPolicySection.mockResolvedValue({
      sourceReference: "policy://policies/customer-activity-risk-review-v1.md#operator-review-triggers",
      title: "Operator Review Triggers",
      content: "Open an operator risk review when suspicious activity appears."
    });

    render(
      <PolicyEvidenceLink sourceReference="policy://policies/customer-activity-risk-review-v1.md#operator-review-triggers" />
    );

    fireEvent.click(screen.getByRole("button", {
      name: "policy://policies/customer-activity-risk-review-v1.md#operator-review-triggers"
    }));

    await waitFor(() => expect(fetchPolicySection).toHaveBeenCalledWith(
      "policy://policies/customer-activity-risk-review-v1.md#operator-review-triggers"
    ));
    expect(screen.getByRole("dialog", { name: "Operator Review Triggers" })).toBeInTheDocument();
    expect(screen.getByText("Open an operator risk review when suspicious activity appears.")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Close" }));

    expect(screen.queryByRole("dialog", { name: "Operator Review Triggers" })).not.toBeInTheDocument();
  });

  it("shows an error when the policy section cannot be loaded", async () => {
    fetchPolicySection.mockRejectedValue(new Error("Could not load policy section"));

    render(
      <PolicyEvidenceLink sourceReference="policy://policies/customer-activity-risk-review-v1.md#operator-review-triggers" />
    );

    fireEvent.click(screen.getByRole("button", {
      name: "policy://policies/customer-activity-risk-review-v1.md#operator-review-triggers"
    }));

    expect(await screen.findByText("Could not load policy section")).toBeInTheDocument();
  });
});
