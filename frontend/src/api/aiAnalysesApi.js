export async function fetchCustomerAiAnalyses(customerId) {
  const normalizedCustomerId = customerId.trim();

  if (!normalizedCustomerId) {
    throw new Error("Customer ID is required");
  }

  const response = await fetch(`/api/customers/${encodeURIComponent(normalizedCustomerId)}/ai-analyses`, {
    credentials: "include"
  });

  if (!response.ok) {
    throw new Error("Could not load AI analyses");
  }

  return response.json();
}

export async function requestCustomerAiAnalysis(customerId) {
  const normalizedCustomerId = customerId.trim();

  if (!normalizedCustomerId) {
    throw new Error("Customer ID is required");
  }

  const response = await fetch(`/api/customers/${encodeURIComponent(normalizedCustomerId)}/ai-analyses`, {
    credentials: "include",
    method: "POST"
  });

  if (!response.ok) {
    throw new Error("Could not request AI analysis");
  }

  return response.json();
}
