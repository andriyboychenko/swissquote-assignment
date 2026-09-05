import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { CustomerSearchForm } from "./CustomerSearchForm";

describe("CustomerSearchForm", () => {
  it("submits the current search and reports input changes", () => {
    const onCustomerIdChange = vi.fn();
    const onSubmit = vi.fn((event) => event.preventDefault());

    render(
      <CustomerSearchForm
        customerId="customer-1"
        isLoading={false}
        suggestions={[]}
        highlightedSuggestionIndex={-1}
        suggestionStatus="idle"
        onCustomerIdChange={onCustomerIdChange}
        onSuggestionSelect={vi.fn()}
        onSuggestionKeyDown={vi.fn()}
        onSuggestionMouseEnter={vi.fn()}
        onSubmit={onSubmit}
      />
    );

    fireEvent.change(screen.getByLabelText("Customer ID"), { target: { value: "customer-2" } });
    fireEvent.click(screen.getByRole("button", { name: "Search" }));

    expect(onCustomerIdChange).toHaveBeenCalledWith("customer-2");
    expect(onSubmit).toHaveBeenCalledOnce();
  });

  it("disables search while loading", () => {
    render(
      <CustomerSearchForm
        customerId="customer-1"
        isLoading={true}
        suggestions={[]}
        highlightedSuggestionIndex={-1}
        suggestionStatus="idle"
        onCustomerIdChange={vi.fn()}
        onSuggestionSelect={vi.fn()}
        onSuggestionKeyDown={vi.fn()}
        onSuggestionMouseEnter={vi.fn()}
        onSubmit={vi.fn()}
      />
    );

    expect(screen.getByRole("button", { name: "Searching" })).toBeDisabled();
  });

  it("renders and selects customer suggestions", () => {
    const onSuggestionSelect = vi.fn();

    render(
      <CustomerSearchForm
        customerId="00"
        isLoading={false}
        suggestions={[{ customerId: "005514e6-1ebe-8010-de91-aff66d1d9484" }]}
        highlightedSuggestionIndex={0}
        suggestionStatus="loaded"
        onCustomerIdChange={vi.fn()}
        onSuggestionSelect={onSuggestionSelect}
        onSuggestionKeyDown={vi.fn()}
        onSuggestionMouseEnter={vi.fn()}
        onSubmit={vi.fn()}
      />
    );

    const suggestion = screen.getByRole("option", { name: "005514e6-1ebe-8010-de91-aff66d1d9484" });
    fireEvent.click(suggestion);

    expect(onSuggestionSelect).toHaveBeenCalledWith("005514e6-1ebe-8010-de91-aff66d1d9484");
    expect(suggestion).toHaveAttribute("aria-selected", "true");
  });

  it("passes arrow key events back to the dashboard", () => {
    const onSuggestionKeyDown = vi.fn();

    render(
      <CustomerSearchForm
        customerId="00"
        isLoading={false}
        suggestions={[{ customerId: "005514e6-1ebe-8010-de91-aff66d1d9484" }]}
        highlightedSuggestionIndex={0}
        suggestionStatus="loaded"
        onCustomerIdChange={vi.fn()}
        onSuggestionSelect={vi.fn()}
        onSuggestionKeyDown={onSuggestionKeyDown}
        onSuggestionMouseEnter={vi.fn()}
        onSubmit={vi.fn()}
      />
    );

    fireEvent.keyDown(screen.getByLabelText("Customer ID"), { key: "ArrowDown" });

    expect(onSuggestionKeyDown).toHaveBeenCalledOnce();
  });

  it("renders suggestion loading and empty states", () => {
    const { rerender } = render(
      <CustomerSearchForm
        customerId="00"
        isLoading={false}
        suggestions={[]}
        highlightedSuggestionIndex={-1}
        suggestionStatus="loading"
        onCustomerIdChange={vi.fn()}
        onSuggestionSelect={vi.fn()}
        onSuggestionKeyDown={vi.fn()}
        onSuggestionMouseEnter={vi.fn()}
        onSubmit={vi.fn()}
      />
    );

    expect(screen.getByText("Loading suggestions...")).toBeInTheDocument();

    rerender(
      <CustomerSearchForm
        customerId="xx"
        isLoading={false}
        suggestions={[]}
        highlightedSuggestionIndex={-1}
        suggestionStatus="empty"
        onCustomerIdChange={vi.fn()}
        onSuggestionSelect={vi.fn()}
        onSuggestionKeyDown={vi.fn()}
        onSuggestionMouseEnter={vi.fn()}
        onSubmit={vi.fn()}
      />
    );

    expect(screen.getByText("No matching customers")).toBeInTheDocument();
  });
});
