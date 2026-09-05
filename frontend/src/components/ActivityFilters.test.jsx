import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ActivityFilters } from "./ActivityFilters";

const filters = {
  createdFrom: "",
  createdTo: "",
  activityType: "",
  status: "",
  amountMin: "",
  amountMax: "",
  currency: "",
  counterparty: "",
  channel: "",
  detail: ""
};

describe("ActivityFilters", () => {
  it("renders filter controls adapted to activity data types", () => {
    render(
      <ActivityFilters
        filters={filters}
        isCollapsed={false}
        onToggleCollapsed={vi.fn()}
        onFilterChange={vi.fn()}
        onReset={vi.fn()}
      />
    );

    expect(screen.getByRole("heading", { name: "Filters" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Hide activity filters/ })).toHaveAttribute("aria-expanded", "true");
    expect(screen.getByLabelText("From")).toHaveAttribute("type", "datetime-local");
    expect(screen.getByLabelText("To")).toHaveAttribute("type", "datetime-local");
    expect(screen.getByLabelText("Type").tagName).toBe("SELECT");
    expect(screen.getByLabelText("Status").tagName).toBe("SELECT");
    expect(screen.getByLabelText("Min amount")).toHaveAttribute("type", "number");
    expect(screen.getByLabelText("Max amount")).toHaveAttribute("type", "number");
    expect(screen.getByLabelText("Currency").tagName).toBe("SELECT");
  });

  it("reports filter changes and reset requests", () => {
    const onFilterChange = vi.fn();
    const onReset = vi.fn();
    render(
      <ActivityFilters
        filters={filters}
        isCollapsed={false}
        onToggleCollapsed={vi.fn()}
        onFilterChange={onFilterChange}
        onReset={onReset}
      />
    );

    fireEvent.change(screen.getByLabelText("Type"), { target: { value: "CARD" } });
    fireEvent.change(screen.getByLabelText("Status"), { target: { value: "Completed" } });
    fireEvent.change(screen.getByLabelText("Counterparty"), { target: { value: "Merchant" } });
    fireEvent.click(screen.getByRole("button", { name: "Reset" }));

    expect(onFilterChange).toHaveBeenCalledWith("activityType", "CARD");
    expect(onFilterChange).toHaveBeenCalledWith("status", "Completed");
    expect(onFilterChange).toHaveBeenCalledWith("counterparty", "Merchant");
    expect(onReset).toHaveBeenCalledOnce();
  });

  it("can render minimized with only the header visible", () => {
    const onToggleCollapsed = vi.fn();

    render(
      <ActivityFilters
        filters={filters}
        isCollapsed={true}
        onToggleCollapsed={onToggleCollapsed}
        onFilterChange={vi.fn()}
        onReset={vi.fn()}
      />
    );

    fireEvent.click(screen.getByRole("button", { name: /Show activity filters/ }));

    expect(screen.queryByLabelText("From")).not.toBeInTheDocument();
    expect(onToggleCollapsed).toHaveBeenCalledOnce();
  });
});
