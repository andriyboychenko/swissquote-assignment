import { beforeEach, describe, expect, it, vi } from "vitest";
import { fetchCustomerAiAnalyses, requestCustomerAiAnalysis } from "./aiAnalysesApi";

describe("aiAnalysesApi", () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  it("loads customer AI analyses", async () => {
    fetch.mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue([{ analysisRequestId: "analysis-1" }])
    });

    const analyses = await fetchCustomerAiAnalyses(" customer-1 ");

    expect(fetch).toHaveBeenCalledWith("/api/customers/customer-1/ai-analyses", {
      credentials: "include"
    });
    expect(analyses).toEqual([{ analysisRequestId: "analysis-1" }]);
  });

  it("requests customer AI analysis", async () => {
    document.cookie = "XSRF-TOKEN=token-123; path=/";
    fetch.mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({ analysisRequestId: "analysis-1" })
    });

    const analysis = await requestCustomerAiAnalysis("customer-1");

    expect(fetch).toHaveBeenCalledWith("/api/customers/customer-1/ai-analyses", {
      credentials: "include",
      headers: {
        "X-XSRF-TOKEN": "token-123"
      },
      method: "POST"
    });
    expect(analysis).toEqual({ analysisRequestId: "analysis-1" });
  });

  it("rejects empty customer ids", async () => {
    await expect(fetchCustomerAiAnalyses(" ")).rejects.toThrow("Customer ID is required");
    await expect(requestCustomerAiAnalysis(" ")).rejects.toThrow("Customer ID is required");
  });
});
