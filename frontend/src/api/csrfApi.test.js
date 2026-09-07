import { afterEach, describe, expect, it, vi } from "vitest";
import { csrfFetch, readCsrfToken } from "./csrfApi";

describe("csrfApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/";
  });

  it("reads the CSRF token from the XSRF-TOKEN cookie", () => {
    document.cookie = "XSRF-TOKEN=token-123; path=/";

    expect(readCsrfToken()).toBe("token-123");
  });

  it("adds the CSRF header for mutating requests", async () => {
    document.cookie = "XSRF-TOKEN=token-123; path=/";
    const fetchMock = vi.fn().mockResolvedValue({ ok: true });
    vi.stubGlobal("fetch", fetchMock);

    await csrfFetch("/api/example", { method: "POST" });

    expect(fetchMock).toHaveBeenCalledWith("/api/example", {
      credentials: "include",
      headers: {
        "X-XSRF-TOKEN": "token-123"
      },
      method: "POST"
    });
  });

  it("does not add a CSRF header for safe requests", async () => {
    document.cookie = "XSRF-TOKEN=token-123; path=/";
    const fetchMock = vi.fn().mockResolvedValue({ ok: true });
    vi.stubGlobal("fetch", fetchMock);

    await csrfFetch("/api/example");

    expect(fetchMock).toHaveBeenCalledWith("/api/example", {
      credentials: "include"
    });
  });
});
