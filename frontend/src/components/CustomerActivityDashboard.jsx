import React, { useRef, useState } from "react";
import { fetchCustomerActivities, fetchCustomerSuggestions } from "../api/customerActivitiesApi";
import { ActivityFilters } from "./ActivityFilters";
import { ActivitySummary } from "./ActivitySummary";
import { ActivityTable } from "./ActivityTable";
import { CustomerSearchForm } from "./CustomerSearchForm";

const ACTIVITY_PAGE_SIZE = 50;
const EMPTY_FILTERS = {
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
const DEFAULT_SORT = {
  sortBy: "createdAt",
  sortDirection: "DESC"
};

export function CustomerActivityDashboard() {
  const [customerId, setCustomerId] = useState("");
  const [report, setReport] = useState(null);
  const [status, setStatus] = useState("idle");
  const [paginationStatus, setPaginationStatus] = useState("idle");
  const [error, setError] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [isFilterPanelCollapsed, setIsFilterPanelCollapsed] = useState(true);
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [highlightedSuggestionIndex, setHighlightedSuggestionIndex] = useState(-1);
  const [suggestionStatus, setSuggestionStatus] = useState("idle");
  const suggestionRequestRef = useRef(0);
  const suggestionTimerRef = useRef(null);

  function handleCustomerIdChange(nextCustomerId) {
    setCustomerId(nextCustomerId);
    setSuggestions([]);
    setHighlightedSuggestionIndex(-1);

    if (suggestionTimerRef.current) {
      window.clearTimeout(suggestionTimerRef.current);
    }

    if (nextCustomerId.trim().length < 2) {
      setSuggestionStatus("idle");
      return;
    }

    setSuggestionStatus("loading");
    const requestId = suggestionRequestRef.current + 1;
    suggestionRequestRef.current = requestId;

    suggestionTimerRef.current = window.setTimeout(async () => {
      try {
        const nextSuggestions = await fetchCustomerSuggestions(nextCustomerId);

        if (suggestionRequestRef.current !== requestId) {
          return;
        }

        setSuggestions(nextSuggestions);
        setHighlightedSuggestionIndex(nextSuggestions.length > 0 ? 0 : -1);
        setSuggestionStatus(nextSuggestions.length > 0 ? "loaded" : "empty");
      } catch {
        if (suggestionRequestRef.current === requestId) {
          setSuggestionStatus("error");
        }
      }
    }, 180);
  }

  function handleSuggestionSelect(nextCustomerId) {
    setCustomerId(nextCustomerId);
    setSuggestions([]);
    setHighlightedSuggestionIndex(-1);
    setSuggestionStatus("idle");
  }

  function handleSuggestionKeyDown(event) {
    if (event.key === "Escape") {
      setSuggestions([]);
      setHighlightedSuggestionIndex(-1);
      setSuggestionStatus("idle");
      return;
    }

    if (suggestions.length === 0) {
      return;
    }

    if (event.key === "ArrowDown") {
      event.preventDefault();
      setHighlightedSuggestionIndex((currentIndex) => (currentIndex + 1) % suggestions.length);
      return;
    }

    if (event.key === "ArrowUp") {
      event.preventDefault();
      setHighlightedSuggestionIndex((currentIndex) => (
        currentIndex <= 0 ? suggestions.length - 1 : currentIndex - 1
      ));
      return;
    }

    if (event.key === "Enter" && highlightedSuggestionIndex >= 0) {
      event.preventDefault();
      handleSuggestionSelect(suggestions[highlightedSuggestionIndex].customerId);
    }
  }

  async function loadActivityPage({ nextOffset, nextFilters, nextSort, mode }) {
    const activeFilters = nextFilters ?? filters;
    const activeSort = nextSort ?? sort;

    if (!customerId.trim()) {
      setError("Enter a customer UUID to review activity.");
      setStatus("error");
      setReport(null);
      return;
    }

    if (mode === "replace") {
      setStatus("loading");
      setPaginationStatus("idle");
    } else {
      setPaginationStatus("loading");
    }
    setError("");

    try {
      const activityReport = await fetchCustomerActivities(customerId, {
        limit: ACTIVITY_PAGE_SIZE,
        offset: nextOffset,
        filters: toApiFilters(activeFilters),
        sort: activeSort
      });
      setReport((currentReport) => (
        mode === "append"
          ? {
              ...activityReport,
              activities: [...(currentReport?.activities ?? []), ...activityReport.activities]
            }
          : activityReport
      ));
      setStatus("loaded");
      setPaginationStatus("idle");
    } catch (requestError) {
      if (mode === "replace") {
        setReport(null);
      }
      setError(requestError.message);
      if (mode === "replace") {
        setStatus("error");
      }
      setPaginationStatus(mode === "append" ? "error" : "idle");
    }
  }

  async function handleSearch(event) {
    event.preventDefault();
    await loadActivityPage({
      nextOffset: 0,
      mode: "replace"
    });
  }

  async function handleLoadMore() {
    if (!report?.page?.hasMore || paginationStatus === "loading") {
      return;
    }

    await loadActivityPage({
      nextOffset: report.page.nextOffset,
      mode: "append"
    });
  }

  async function handleFilterChange(key, value) {
    const nextFilters = {
      ...filters,
      [key]: value
    };
    setFilters(nextFilters);

    if (report) {
      await loadActivityPage({
        nextOffset: 0,
        nextFilters,
        mode: "replace"
      });
    }
  }

  async function handleResetFilters() {
    setFilters(EMPTY_FILTERS);

    if (report) {
      await loadActivityPage({
        nextOffset: 0,
        nextFilters: EMPTY_FILTERS,
        mode: "replace"
      });
    }
  }

  async function handleSortChange(sortBy) {
    const nextSort = {
      sortBy,
      sortDirection: sort.sortBy === sortBy && sort.sortDirection === "ASC" ? "DESC" : "ASC"
    };
    setSort(nextSort);

    if (report) {
      await loadActivityPage({
        nextOffset: 0,
        nextSort,
        mode: "replace"
      });
    }
  }

  return (
    <section className="dashboard-panel" aria-labelledby="dashboard-title">
      <div className="dashboard-header">
        <div>
          <p className="eyebrow">Customer review</p>
          <h2 id="dashboard-title">Search activity by Customer ID</h2>
        </div>
      </div>

      <CustomerSearchForm
        customerId={customerId}
        isLoading={status === "loading"}
        suggestions={suggestions}
        highlightedSuggestionIndex={highlightedSuggestionIndex}
        suggestionStatus={suggestionStatus}
        onCustomerIdChange={handleCustomerIdChange}
        onSuggestionSelect={handleSuggestionSelect}
        onSuggestionKeyDown={handleSuggestionKeyDown}
        onSuggestionMouseEnter={setHighlightedSuggestionIndex}
        onSubmit={handleSearch}
      />

      {status === "idle" ? (
        <p className="dashboard-state">Paste a customer UUID to load card, payment, and crypto activity.</p>
      ) : null}

      {status === "loading" ? <p className="dashboard-state">Loading customer activity...</p> : null}

      {status === "error" ? <p className="dashboard-error">{error}</p> : null}

      {report ? (
        <div className="activity-results">
          <div className="activity-results-header">
            <span>Customer</span>
            <strong>{report.customerId}</strong>
          </div>
          <ActivitySummary summary={report.summary} />
          <ActivityFilters
            filters={filters}
            isCollapsed={isFilterPanelCollapsed}
            onToggleCollapsed={() => setIsFilterPanelCollapsed((isCollapsed) => !isCollapsed)}
            onFilterChange={handleFilterChange}
            onReset={handleResetFilters}
          />
          <ActivityTable
            activities={report.activities}
            hasMore={report.page.hasMore}
            isLoadingMore={paginationStatus === "loading"}
            sort={sort}
            onSortChange={handleSortChange}
            onLoadMore={handleLoadMore}
          />
          {paginationStatus === "error" ? <p className="dashboard-error">{error}</p> : null}
        </div>
      ) : null}
    </section>
  );
}

function toApiFilters(filters) {
  return {
    ...filters,
    createdFrom: toIsoDateTime(filters.createdFrom),
    createdTo: toIsoDateTime(filters.createdTo)
  };
}

function toIsoDateTime(value) {
  return value ? new Date(value).toISOString() : "";
}
