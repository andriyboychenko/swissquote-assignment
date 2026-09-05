import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ActivityTable } from "./ActivityTable";

describe("ActivityTable", () => {
  it("renders activity rows with type, status, amount, and details", () => {
    render(
      <ActivityTable
        activities={[
          {
            transactionId: "transaction-1",
            activityType: "CARD",
            amount: 125.5,
            currency: "CHF",
            status: "Completed",
            createdAt: "2026-09-04T12:00:00Z",
            counterparty: "Merchant 001",
            channel: "Credit",
            detail: "PAN ****1234, MCC 5411, Decline: Insufficient funds",
            riskIndicators: [
              {
                ruleName: "High-value card transaction",
                severity: "HIGH",
                scoreContribution: 20
              }
            ]
          },
          {
            transactionId: "transaction-2",
            activityType: "PAYMENT",
            amount: 500,
            currency: "EUR",
            status: "Pending",
            createdAt: "2026-09-04T13:00:00Z",
            counterparty: "DE00000000000000000001",
            channel: "SWIFT",
            detail: "Receiver country DE",
            riskIndicators: []
          },
          {
            transactionId: "transaction-3",
            activityType: "CARD",
            amount: 25,
            currency: "CHF",
            status: "Reversed",
            createdAt: "2026-09-04T14:00:00Z",
            counterparty: "Merchant 002",
            channel: "Debit",
            detail: "PAN ****4321, MCC 5411",
            riskIndicators: []
          }
        ]}
        hasMore={false}
        isLoadingMore={false}
        sort={{ sortBy: "createdAt", sortDirection: "DESC" }}
        onSortChange={vi.fn()}
        onLoadMore={vi.fn()}
      />
    );

    expect(screen.getByRole("columnheader", { name: "Created" })).toBeInTheDocument();
    expect(screen.queryByRole("columnheader", { name: "Currency" })).not.toBeInTheDocument();
    expect(screen.getAllByText("CARD")).toHaveLength(2);
    expect(screen.getByText("Completed")).toBeInTheDocument();
    expect(screen.getByText("125.50 CHF").closest("td")).toHaveClass("amount-incoming");
    expect(screen.getByText("500.00 EUR").closest("td")).toHaveClass("amount-pending");
    expect(screen.getByText("25.00 CHF").closest("td")).toHaveClass("amount-incoming");
    expect(screen.getByLabelText("Added to account")).toHaveClass("movement-up");
    expect(screen.getByLabelText("Pending movement")).toHaveClass("movement-unknown");
    expect(screen.getByLabelText("Added back to account")).toHaveClass("movement-up");
    expect(screen.getByLabelText("Pending movement")).toHaveAttribute(
      "data-tooltip",
      "This activity is pending, so the final account movement is not confirmed yet."
    );
    expect(screen.getByText("Merchant 001").closest(".counterparty-tooltip")).toHaveAttribute(
      "data-tooltip",
      "Merchant 001"
    );
    expect(screen.getByText("PAN ****1234, MCC 5411")).toBeInTheDocument();
    expect(screen.getByText("Decline: Insufficient funds")).toHaveClass("decline-detail");
    expect(screen.getByLabelText("Suspicious activity")).toBeInTheDocument();
    expect(screen.getByText("Merchant 001").closest("tr")).toHaveClass("activity-row-suspicious");
  });

  it("marks completed debit card activity as outgoing", () => {
    render(
      <ActivityTable
        activities={[
          {
            transactionId: "transaction-1",
            activityType: "CARD",
            amount: 44.2,
            currency: "CHF",
            status: "Completed",
            createdAt: "2026-09-04T12:00:00Z",
            counterparty: "Merchant 001",
            channel: "Debit",
            detail: "PAN ****1234, MCC 5411",
            riskIndicators: []
          }
        ]}
        hasMore={false}
        isLoadingMore={false}
        sort={{ sortBy: "createdAt", sortDirection: "DESC" }}
        onSortChange={vi.fn()}
        onLoadMore={vi.fn()}
      />
    );

    expect(screen.getByText("44.20 CHF").closest("td")).toHaveClass("amount-outgoing");
    expect(screen.getByLabelText("Removed from account")).toHaveClass("movement-down");
    expect(screen.getByLabelText("Removed from account")).toHaveAttribute(
      "data-tooltip",
      "This settled activity is displayed as value leaving the account."
    );
  });

  it("marks failed activity as no movement with a help tooltip", () => {
    render(
      <ActivityTable
        activities={[
          {
            transactionId: "transaction-1",
            activityType: "PAYMENT",
            amount: 44.2,
            currency: "CHF",
            status: "Failed",
            createdAt: "2026-09-04T12:00:00Z",
            counterparty: "CH00000000000000000001",
            channel: "SWIFT",
            detail: "Receiver country CH",
            riskIndicators: []
          }
        ]}
        hasMore={false}
        isLoadingMore={false}
        sort={{ sortBy: "createdAt", sortDirection: "DESC" }}
        onSortChange={vi.fn()}
        onLoadMore={vi.fn()}
      />
    );

    expect(screen.getByText("44.20 CHF").closest("td")).toHaveClass("amount-neutral");
    expect(screen.getByLabelText("No account movement")).toHaveClass("movement-unknown");
    expect(screen.getByLabelText("No account movement")).toHaveAttribute(
      "data-tooltip",
      "This activity failed, so no account movement is expected."
    );
  });

  it("reports sort changes from each column header", () => {
    const onSortChange = vi.fn();

    render(
      <ActivityTable
        activities={[]}
        hasMore={false}
        isLoadingMore={false}
        sort={{ sortBy: "createdAt", sortDirection: "DESC" }}
        onSortChange={onSortChange}
        onLoadMore={vi.fn()}
      />
    );

    fireEvent.click(screen.getByRole("button", { name: "Amount" }));

    expect(onSortChange).toHaveBeenCalledWith("amount");
    expect(screen.getByText("▼")).toHaveClass("sort-indicator");
  });

  it("loads more rows when scrolled near the bottom", () => {
    const onLoadMore = vi.fn();

    render(
      <ActivityTable
        activities={[]}
        hasMore={true}
        isLoadingMore={false}
        sort={{ sortBy: "createdAt", sortDirection: "DESC" }}
        onSortChange={vi.fn()}
        onLoadMore={onLoadMore}
      />
    );

    const tableWrap = screen.getByRole("table").parentElement;
    Object.defineProperty(tableWrap, "clientHeight", { configurable: true, value: 100 });
    Object.defineProperty(tableWrap, "scrollHeight", { configurable: true, value: 180 });
    Object.defineProperty(tableWrap, "scrollTop", { configurable: true, value: 40 });

    fireEvent.scroll(tableWrap);

    expect(onLoadMore).toHaveBeenCalledOnce();
    expect(screen.getByText("Scroll to load more")).toBeInTheDocument();
  });
});
