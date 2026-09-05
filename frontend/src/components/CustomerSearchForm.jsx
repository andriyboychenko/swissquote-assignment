import React from "react";

export function CustomerSearchForm({
  customerId,
  isLoading,
  suggestions,
  highlightedSuggestionIndex,
  suggestionStatus,
  onCustomerIdChange,
  onSuggestionSelect,
  onSuggestionKeyDown,
  onSuggestionMouseEnter,
  onSubmit
}) {
  const hasSuggestions = suggestions.length > 0;
  const activeSuggestion = highlightedSuggestionIndex >= 0 ? suggestions[highlightedSuggestionIndex] : null;

  return (
    <form className="customer-search-form" onSubmit={onSubmit}>
      <label className="sr-only" htmlFor="customer-id">Customer ID</label>
      <div className="customer-search-controls">
        <div className="customer-autocomplete">
          <input
            id="customer-id"
            name="customerId"
            value={customerId}
            autoComplete="off"
            aria-autocomplete="list"
            aria-controls="customer-id-suggestions"
            aria-activedescendant={activeSuggestion ? `customer-suggestion-${activeSuggestion.customerId}` : undefined}
            aria-expanded={hasSuggestions}
            onChange={(event) => onCustomerIdChange(event.target.value)}
            onKeyDown={onSuggestionKeyDown}
          />
          {suggestionStatus === "loading" ? (
            <p className="suggestion-state">Loading suggestions...</p>
          ) : null}
          {suggestionStatus === "empty" ? (
            <p className="suggestion-state">No matching customers</p>
          ) : null}
          {suggestionStatus === "error" ? (
            <p className="suggestion-error">Could not load suggestions</p>
          ) : null}
          {hasSuggestions ? (
            <ul className="suggestion-list" id="customer-id-suggestions" role="listbox">
              {suggestions.map((suggestion, index) => (
                <li key={suggestion.customerId} role="presentation">
                  <button
                    type="button"
                    id={`customer-suggestion-${suggestion.customerId}`}
                    className={index === highlightedSuggestionIndex ? "selected" : ""}
                    role="option"
                    aria-selected={index === highlightedSuggestionIndex}
                    onMouseEnter={() => onSuggestionMouseEnter(index)}
                    onClick={() => onSuggestionSelect(suggestion.customerId)}
                  >
                    {suggestion.customerId}
                  </button>
                </li>
              ))}
            </ul>
          ) : null}
        </div>
        <button type="submit" disabled={isLoading}>
          {isLoading ? "Searching" : "Search"}
        </button>
      </div>
    </form>
  );
}
