import React from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { fetchCustomerAiAnalyses, requestCustomerAiAnalysis } from "../api/aiAnalysesApi";
import { fetchCustomerActivities } from "../api/customerActivitiesApi";
import { fetchCustomerSuggestions } from "../api/customerActivitiesApi";
import { CustomerActivityDashboard } from "./CustomerActivityDashboard";

vi.mock("../api/aiAnalysesApi", () => ({
  fetchCustomerAiAnalyses: vi.fn(),
  requestCustomerAiAnalysis: vi.fn()
}));

vi.mock("../api/customerActivitiesApi", () => ({
  fetchCustomerActivities: vi.fn(),
  fetchCustomerSuggestions: vi.fn()
}));

const activityReport = {
  customerId: "1b884e7f-5d26-3f5f-98bc-94ea1018b02f",
  summary: {
    totalActivities: 1,
    cardActivities: 1,
    paymentActivities: 0,
    cryptoActivities: 0,
    failedActivities: 0,
    pendingActivities: 0
  },
  activities: [
    {
      transactionId: "transaction-1",
      activityType: "CARD",
      amount: 125.5,
      currency: "CHF",
      status: "Completed",
      createdAt: "2026-09-04T12:00:00Z",
      counterparty: "Merchant 001",
      channel: "Credit",
      detail: "PAN ****1234, MCC 5411"
    }
  ],
  page: {
    limit: 50,
    offset: 0,
    returnedActivities: 1,
    hasMore: false,
    nextOffset: 1
  }
};

describe("CustomerActivityDashboard", () => {
  beforeEach(() => {
    vi.useRealTimers();
    fetchCustomerActivities.mockReset();
    fetchCustomerSuggestions.mockReset();
    fetchCustomerAiAnalyses.mockReset();
    requestCustomerAiAnalysis.mockReset();
    fetchCustomerActivities.mockResolvedValue(activityReport);
    fetchCustomerSuggestions.mockResolvedValue([{ customerId: activityReport.customerId }]);
    fetchCustomerAiAnalyses.mockResolvedValue([]);
    requestCustomerAiAnalysis.mockResolvedValue({
      analysisRequestId: "analysis-1",
      customerId: activityReport.customerId,
      status: "COMPLETED",
      requestedAt: "2026-09-05T08:00:00Z",
      result: {
        riskLevel: "MEDIUM",
        summary: "Reviewed 1 activity.",
        recommendations: "Review the highlighted risk signals.",
        evidence: []
      }
    });
  });

  it("loads and renders customer activity after search", async () => {
    render(<CustomerActivityDashboard />);

    fireEvent.change(screen.getByLabelText("Customer ID"), {
      target: { value: activityReport.customerId }
    });
    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    await waitFor(() => expect(fetchCustomerActivities).toHaveBeenCalledWith(activityReport.customerId, {
      limit: 50,
      offset: 0,
      filters: expect.any(Object),
      sort: {
        sortBy: "createdAt",
        sortDirection: "DESC"
      }
    }));
    expect(await screen.findByText(activityReport.customerId)).toBeInTheDocument();
    expect(screen.getByText("Merchant 001")).toBeInTheDocument();
    expect(fetchCustomerAiAnalyses).toHaveBeenCalledWith(activityReport.customerId);
  });

  it("does not call the API without a customer id", () => {
    render(<CustomerActivityDashboard />);

    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    expect(fetchCustomerActivities).not.toHaveBeenCalled();
    expect(screen.getByText("Enter a customer UUID to review activity.")).toBeInTheDocument();
  });

  it("renders a readable error when loading fails", async () => {
    fetchCustomerActivities.mockRejectedValue(new Error("Customer was not found"));

    render(<CustomerActivityDashboard />);

    fireEvent.change(screen.getByLabelText("Customer ID"), {
      target: { value: "missing-customer" }
    });
    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    expect(await screen.findByText("Customer was not found")).toBeInTheDocument();
  });

  it("loads autocomplete suggestions and applies a selected customer id", async () => {
    render(<CustomerActivityDashboard />);

    fireEvent.change(screen.getByLabelText("Customer ID"), {
      target: { value: "00" }
    });

    await waitFor(() => expect(fetchCustomerSuggestions).toHaveBeenCalledWith("00"));
    fireEvent.click(await screen.findByRole("option", { name: activityReport.customerId }));

    expect(screen.getByLabelText("Customer ID")).toHaveValue(activityReport.customerId);
  });

  it("selects an autocomplete suggestion with arrow keys and enter", async () => {
    const secondCustomerId = "9f4d9d67-d821-4b31-a883-ff6fa4c3a111";
    fetchCustomerSuggestions.mockResolvedValue([
      { customerId: activityReport.customerId },
      { customerId: secondCustomerId }
    ]);

    render(<CustomerActivityDashboard />);

    const input = screen.getByLabelText("Customer ID");
    fireEvent.change(input, { target: { value: "00" } });

    await screen.findByRole("option", { name: activityReport.customerId });
    fireEvent.keyDown(input, { key: "ArrowDown" });
    fireEvent.keyDown(input, { key: "Enter" });

    expect(input).toHaveValue(secondCustomerId);
  });

  it("loads the next activity page when the table is scrolled", async () => {
    const firstPage = {
      ...activityReport,
      page: {
        limit: 50,
        offset: 0,
        returnedActivities: 1,
        hasMore: true,
        nextOffset: 50
      }
    };
    const secondPage = {
      ...activityReport,
      activities: [
        {
          transactionId: "transaction-2",
          activityType: "PAYMENT",
          amount: 500,
          currency: "EUR",
          status: "Pending",
          createdAt: "2026-09-04T13:00:00Z",
          counterparty: "DE00000000000000000001",
          channel: "SWIFT",
          detail: "Receiver country DE"
        }
      ],
      page: {
        limit: 50,
        offset: 50,
        returnedActivities: 1,
        hasMore: false,
        nextOffset: 101
      }
    };
    fetchCustomerActivities
      .mockResolvedValueOnce(firstPage)
      .mockResolvedValueOnce(secondPage);

    render(<CustomerActivityDashboard />);

    fireEvent.change(screen.getByLabelText("Customer ID"), {
      target: { value: activityReport.customerId }
    });
    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    const table = await screen.findByRole("table");
    const tableWrap = table.parentElement;
    Object.defineProperty(tableWrap, "clientHeight", { configurable: true, value: 100 });
    Object.defineProperty(tableWrap, "scrollHeight", { configurable: true, value: 180 });
    Object.defineProperty(tableWrap, "scrollTop", { configurable: true, value: 40 });

    fireEvent.scroll(tableWrap);

    await waitFor(() => expect(fetchCustomerActivities).toHaveBeenCalledWith(activityReport.customerId, {
      limit: 50,
      offset: 50,
      filters: expect.any(Object),
      sort: {
        sortBy: "createdAt",
        sortDirection: "DESC"
      }
    }));
    expect(await screen.findByText("DE00000000000000000001")).toBeInTheDocument();
    expect(fetchCustomerAiAnalyses).toHaveBeenCalledOnce();
  });

  it("requests AI analysis for the loaded customer", async () => {
    render(<CustomerActivityDashboard />);

    fireEvent.change(screen.getByLabelText("Customer ID"), {
      target: { value: activityReport.customerId }
    });
    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    await screen.findByText("No AI analysis has been requested for this customer yet.");
    fireEvent.click(screen.getByRole("button", { name: "Request AI analysis" }));

    await waitFor(() => expect(requestCustomerAiAnalysis).toHaveBeenCalledWith(activityReport.customerId));
    expect(await screen.findByText("Risk: MEDIUM")).toBeInTheDocument();
    expect(screen.getByText("Reviewed 1 activity.")).toBeInTheDocument();
  });

  it("applies recommended flagged activity filters from the AI review", async () => {
    render(<CustomerActivityDashboard />);

    fireEvent.change(screen.getByLabelText("Customer ID"), {
      target: { value: activityReport.customerId }
    });
    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    await screen.findByText("No AI analysis has been requested for this customer yet.");
    fireEvent.click(screen.getByRole("button", { name: "Request AI analysis" }));
    await screen.findByText("Risk: MEDIUM");
    fireEvent.click(screen.getByRole("button", { name: /Review flagged activity/ }));

    await waitFor(() => expect(fetchCustomerActivities).toHaveBeenCalledWith(activityReport.customerId, {
      limit: 50,
      offset: 0,
      filters: expect.objectContaining({
        riskOnly: true
      }),
      sort: {
        sortBy: "amount",
        sortDirection: "DESC"
      }
    }));
    expect(screen.getByLabelText("Flagged activity only")).toBeChecked();
  });

  it("applies recommended flagged activity filters with the keyboard shortcut", async () => {
    requestCustomerAiAnalysis.mockResolvedValue({
      analysisRequestId: "analysis-1",
      customerId: activityReport.customerId,
      status: "COMPLETED",
      requestedAt: "2026-09-05T08:00:00Z",
      result: {
        riskLevel: "HIGH",
        summary: "Reviewed 1 activity.",
        recommendations: "Escalate and review flagged activity.",
        evidence: []
      }
    });
    render(<CustomerActivityDashboard />);

    fireEvent.change(screen.getByLabelText("Customer ID"), {
      target: { value: activityReport.customerId }
    });
    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    await screen.findByText("No AI analysis has been requested for this customer yet.");
    fireEvent.click(screen.getByRole("button", { name: "Request AI analysis" }));
    await screen.findByText("Risk: HIGH");
    fireEvent.keyDown(window, { key: "r", altKey: true });

    await waitFor(() => expect(fetchCustomerActivities).toHaveBeenCalledWith(activityReport.customerId, {
      limit: 50,
      offset: 0,
      filters: expect.objectContaining({
        riskOnly: true
      }),
      sort: {
        sortBy: "amount",
        sortDirection: "DESC"
      }
    }));
  });

  it("reloads the first page when filters or sorting change", async () => {
    fetchCustomerActivities.mockResolvedValue(activityReport);

    render(<CustomerActivityDashboard />);

    fireEvent.change(screen.getByLabelText("Customer ID"), {
      target: { value: activityReport.customerId }
    });
    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    await screen.findByText("Merchant 001");
    fireEvent.click(screen.getByRole("button", { name: /Show activity filters/ }));
    fireEvent.change(screen.getByLabelText("Type"), { target: { value: "CARD" } });

    await waitFor(() => expect(fetchCustomerActivities).toHaveBeenCalledWith(activityReport.customerId, {
      limit: 50,
      offset: 0,
      filters: expect.objectContaining({
        activityType: "CARD"
      }),
      sort: {
        sortBy: "createdAt",
        sortDirection: "DESC"
      }
    }));

    fireEvent.click(screen.getByRole("button", { name: "Amount" }));

    await waitFor(() => expect(fetchCustomerActivities).toHaveBeenCalledWith(activityReport.customerId, {
      limit: 50,
      offset: 0,
      filters: expect.any(Object),
      sort: {
        sortBy: "amount",
        sortDirection: "ASC"
      }
    }));
  });

  it("renders the filter panel minimized by default and restores it", async () => {
    render(<CustomerActivityDashboard />);

    fireEvent.change(screen.getByLabelText("Customer ID"), {
      target: { value: activityReport.customerId }
    });
    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    await screen.findByText("Merchant 001");
    expect(screen.queryByLabelText("Type")).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: /Show activity filters/ }));

    expect(screen.getByLabelText("Type")).toBeInTheDocument();
  });
});
