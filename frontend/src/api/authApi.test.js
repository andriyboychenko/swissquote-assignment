import { afterEach, describe, expect, it, vi } from "vitest";
import { fetchCurrentOperator } from "./authApi";

describe("fetchCurrentOperator", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("loads the current operator with credentials", async () => {
    const json = vi.fn().mockResolvedValue({ authenticated: true, name: "Demo Operator" });
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, json });
    vi.stubGlobal("fetch", fetchMock);

    await expect(fetchCurrentOperator()).resolves.toEqual({
      authenticated: true,
      name: "Demo Operator"
    });
    expect(fetchMock).toHaveBeenCalledWith("/api/auth/me", { credentials: "include" });
  });

  it("throws when the current operator request fails", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false }));

    await expect(fetchCurrentOperator()).rejects.toThrow("Could not load current operator");
  });
});
