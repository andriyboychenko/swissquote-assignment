export async function fetchCustomerActivities(customerId, { limit = 50, offset = 0, filters = {}, sort = {} } = {}) {
  const normalizedCustomerId = customerId.trim();

  if (!normalizedCustomerId) {
    throw new Error("Customer ID is required");
  }

  const searchParams = new URLSearchParams({
    limit: String(limit),
    offset: String(offset)
  });

  appendSearchParam(searchParams, "createdFrom", filters.createdFrom);
  appendSearchParam(searchParams, "createdTo", filters.createdTo);
  appendSearchParam(searchParams, "activityType", filters.activityType);
  appendSearchParam(searchParams, "status", filters.status);
  appendSearchParam(searchParams, "amountMin", filters.amountMin);
  appendSearchParam(searchParams, "amountMax", filters.amountMax);
  appendSearchParam(searchParams, "currency", filters.currency);
  appendSearchParam(searchParams, "counterparty", filters.counterparty);
  appendSearchParam(searchParams, "channel", filters.channel);
  appendSearchParam(searchParams, "detail", filters.detail);
  appendSearchParam(searchParams, "sortBy", sort.sortBy);
  appendSearchParam(searchParams, "sortDirection", sort.sortDirection);

  const response = await fetch(`/api/customers/${encodeURIComponent(normalizedCustomerId)}/activities?${searchParams.toString()}`, {
    credentials: "include"
  });

  if (response.status === 404) {
    throw new Error("Customer was not found");
  }

  if (!response.ok) {
    throw new Error("Could not load customer activity");
  }

  return response.json();
}

function appendSearchParam(searchParams, key, value) {
  if (value === undefined || value === null || String(value).trim() === "") {
    return;
  }

  searchParams.set(key, String(value).trim());
}

export async function fetchCustomerSuggestions(query) {
  const normalizedQuery = query.trim();

  if (normalizedQuery.length < 2) {
    return [];
  }

  const searchParams = new URLSearchParams({
    query: normalizedQuery,
    limit: "8"
  });
  const response = await fetch(`/api/customers?${searchParams.toString()}`, {
    credentials: "include"
  });

  if (!response.ok) {
    throw new Error("Could not load customer suggestions");
  }

  return response.json();
}
