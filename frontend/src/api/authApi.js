export async function fetchCurrentOperator() {
  const response = await fetch("/api/auth/me", {
    credentials: "include"
  });

  if (!response.ok) {
    throw new Error("Could not load current operator");
  }

  return response.json();
}
