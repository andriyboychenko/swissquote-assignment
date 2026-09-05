import { describe, expect, it, vi } from "vitest";
import { fetchPolicySection } from "./policiesApi";

describe("policiesApi", () => {
  it("fetches a policy section from a policy reference", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({ title: "Operator Review Triggers" })
    });

    const section = await fetchPolicySection(
      "policy://policies/customer-activity-risk-review-v1.md#operator-review-triggers"
    );

    expect(global.fetch).toHaveBeenCalledWith(
      "/api/policies/customer-activity-risk-review-v1.md/sections/operator-review-triggers"
    );
    expect(section).toEqual({ title: "Operator Review Triggers" });
  });

  it("rejects unsupported policy references before making a network request", async () => {
    global.fetch = vi.fn();

    await expect(fetchPolicySection("external://unknown")).rejects.toThrow("Unsupported policy reference");

    expect(global.fetch).not.toHaveBeenCalled();
  });
});
