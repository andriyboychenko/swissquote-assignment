import { afterEach, describe, expect, it, vi } from "vitest";
import { fetchCustomerActivities, fetchCustomerSuggestions } from "./customerActivitiesApi";

describe("fetchCustomerActivities", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("loads customer activities with credentials", async () => {
    const report = { customerId: "customer-1", activities: [] };
    const json = vi.fn().mockResolvedValue(report);
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json });
    vi.stubGlobal("fetch", fetchMock);

    await expect(fetchCustomerActivities(" customer-1 ")).resolves.toEqual(report);

    expect(fetchMock).toHaveBeenCalledWith("/api/customers/customer-1/activities?limit=50&offset=0", {
      credentials: "include"
    });
  });

  it("loads a requested customer activity page", async () => {
    const report = { customerId: "customer-1", activities: [] };
    const json = vi.fn().mockResolvedValue(report);
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json });
    vi.stubGlobal("fetch", fetchMock);

    await expect(fetchCustomerActivities("customer-1", { limit: 50, offset: 100 })).resolves.toEqual(report);

    expect(fetchMock).toHaveBeenCalledWith("/api/customers/customer-1/activities?limit=50&offset=100", {
      credentials: "include"
    });
  });

  it("adds typed filters and sorting to activity requests", async () => {
    const report = { customerId: "customer-1", activities: [] };
    const json = vi.fn().mockResolvedValue(report);
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json });
    vi.stubGlobal("fetch", fetchMock);

    await fetchCustomerActivities("customer-1", {
      limit: 50,
      offset: 0,
      filters: {
        createdFrom: "2026-09-01T00:00:00.000Z",
        createdTo: "2026-09-05T00:00:00.000Z",
        activityType: "CARD",
        status: "Completed",
        amountMin: "10",
        amountMax: "200",
        currency: "CHF",
        counterparty: "Merchant",
        channel: "Credit",
        detail: "PAN",
        riskOnly: true
      },
      sort: {
        sortBy: "amount",
        sortDirection: "ASC"
      }
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/customers/customer-1/activities?limit=50&offset=0&createdFrom=2026-09-01T00%3A00%3A00.000Z&createdTo=2026-09-05T00%3A00%3A00.000Z&activityType=CARD&status=Completed&amountMin=10&amountMax=200&currency=CHF&counterparty=Merchant&channel=Credit&detail=PAN&riskOnly=true&sortBy=amount&sortDirection=ASC",
      {
        credentials: "include"
      }
    );
  });

  it("requires a customer id before making a request", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    await expect(fetchCustomerActivities(" ")).rejects.toThrow("Customer ID is required");

    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("throws a readable not found error", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 404 }));

    await expect(fetchCustomerActivities("missing-customer")).rejects.toThrow("Customer was not found");
  });

  it("throws when the request fails", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 500 }));

    await expect(fetchCustomerActivities("customer-1")).rejects.toThrow("Could not load customer activity");
  });

  it("loads customer autocomplete suggestions with credentials", async () => {
    const suggestions = [{ customerId: "customer-1" }];
    const json = vi.fn().mockResolvedValue(suggestions);
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json });
    vi.stubGlobal("fetch", fetchMock);

    await expect(fetchCustomerSuggestions(" ab ")).resolves.toEqual(suggestions);

    expect(fetchMock).toHaveBeenCalledWith("/api/customers?query=ab&limit=8", {
      credentials: "include"
    });
  });

  it("does not fetch autocomplete suggestions for short queries", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    await expect(fetchCustomerSuggestions("a")).resolves.toEqual([]);

    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("throws when autocomplete suggestions fail", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 500 }));

    await expect(fetchCustomerSuggestions("abc")).rejects.toThrow("Could not load customer suggestions");
  });
});
