import React from "react";

const ACTIVITY_TYPES = ["CARD", "PAYMENT", "CRYPTO"];
const STATUSES = ["Completed", "Pending", "Failed", "Reversed"];
const CURRENCIES = ["CHF", "EUR", "USD", "GBP", "JPY", "BTC", "ETH", "USDC", "SOL"];

export function ActivityFilters({ filters, isCollapsed, onToggleCollapsed, onFilterChange, onReset }) {
  return (
    <section className="activity-filter-panel" aria-label="Activity filters">
      <div className="filter-panel-header">
        <h3>Filters</h3>
        <button
          className="filter-toggle"
          type="button"
          aria-expanded={!isCollapsed}
          aria-controls="activity-filter-fields"
          onClick={onToggleCollapsed}
        >
          <span aria-hidden="true">{isCollapsed ? "Show" : "Hide"}</span>
          <span className="sr-only">{isCollapsed ? "Show activity filters" : "Hide activity filters"}</span>
        </button>
      </div>
      {isCollapsed ? null : (
        <form className="activity-filters" id="activity-filter-fields">
          <div className="filter-field">
            <label htmlFor="created-from">From</label>
            <input
              id="created-from"
              type="datetime-local"
              value={filters.createdFrom}
              onChange={(event) => onFilterChange("createdFrom", event.target.value)}
            />
          </div>
          <div className="filter-field">
            <label htmlFor="created-to">To</label>
            <input
              id="created-to"
              type="datetime-local"
              value={filters.createdTo}
              onChange={(event) => onFilterChange("createdTo", event.target.value)}
            />
          </div>
          <div className="filter-field">
            <label htmlFor="activity-type">Type</label>
            <select
              id="activity-type"
              value={filters.activityType}
              onChange={(event) => onFilterChange("activityType", event.target.value)}
            >
              <option value="">All</option>
              {ACTIVITY_TYPES.map((activityType) => (
                <option key={activityType} value={activityType}>{activityType}</option>
              ))}
            </select>
          </div>
          <div className="filter-field">
            <label htmlFor="activity-status">Status</label>
            <select
              id="activity-status"
              value={filters.status}
              onChange={(event) => onFilterChange("status", event.target.value)}
            >
              <option value="">All</option>
              {STATUSES.map((status) => (
                <option key={status} value={status}>{status}</option>
              ))}
            </select>
          </div>
          <div className="filter-field">
            <label htmlFor="amount-min">Min amount</label>
            <input
              id="amount-min"
              type="number"
              inputMode="decimal"
              min="0"
              step="0.01"
              value={filters.amountMin}
              onChange={(event) => onFilterChange("amountMin", event.target.value)}
            />
          </div>
          <div className="filter-field">
            <label htmlFor="amount-max">Max amount</label>
            <input
              id="amount-max"
              type="number"
              inputMode="decimal"
              min="0"
              step="0.01"
              value={filters.amountMax}
              onChange={(event) => onFilterChange("amountMax", event.target.value)}
            />
          </div>
          <div className="filter-field">
            <label htmlFor="activity-currency">Currency</label>
            <select
              id="activity-currency"
              value={filters.currency}
              onChange={(event) => onFilterChange("currency", event.target.value)}
            >
              <option value="">All</option>
              {CURRENCIES.map((currency) => (
                <option key={currency} value={currency}>{currency}</option>
              ))}
            </select>
          </div>
          <div className="filter-field">
            <label htmlFor="counterparty">Counterparty</label>
            <input
              id="counterparty"
              value={filters.counterparty}
              onChange={(event) => onFilterChange("counterparty", event.target.value)}
            />
          </div>
          <div className="filter-field">
            <label htmlFor="channel">Channel</label>
            <input
              id="channel"
              value={filters.channel}
              onChange={(event) => onFilterChange("channel", event.target.value)}
            />
          </div>
          <div className="filter-field">
            <label htmlFor="detail">Details</label>
            <input
              id="detail"
              value={filters.detail}
              onChange={(event) => onFilterChange("detail", event.target.value)}
            />
          </div>
          <button className="filter-reset" type="button" onClick={onReset}>
            Reset
          </button>
        </form>
      )}
    </section>
  );
}
