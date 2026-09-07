import React from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { fetchCurrentOperator } from "./api/authApi";
import App from "./App";

vi.mock("./api/authApi", () => ({
  fetchCurrentOperator: vi.fn()
}));

vi.mock("./api/customerActivitiesApi", () => ({
  fetchCustomerActivities: vi.fn()
}));

describe("App", () => {
  beforeEach(() => {
    fetchCurrentOperator.mockResolvedValue({ authenticated: false, googleLoginEnabled: true });
  });

  it("renders the welcome landing page for anonymous users", async () => {
    render(<App />);

    expect(await screen.findByRole("heading", { name: "Operator dashboard for client activity and risk" })).toBeInTheDocument();
    expect(screen.getByText(/review card, payment, and crypto behavior/)).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Sign in to monitor customers" })).toBeInTheDocument();
  });

  it("does not render capability tiles", async () => {
    render(<App />);

    await waitFor(() => expect(fetchCurrentOperator).toHaveBeenCalled());
    expect(screen.queryByText("Search customers by Customer ID")).not.toBeInTheDocument();
  });

  it("renders Google and mock operator login options", async () => {
    render(<App />);

    expect(await screen.findByRole("link", { name: /Continue with Google/ })).toHaveAttribute(
      "href",
      "/oauth2/authorization/google"
    );
    expect(screen.getByRole("button", { name: /Continue with Demo operator/ })).toBeInTheDocument();
  });

  it("shows disabled Google login below mock login when the backend has no Google client secret", async () => {
    fetchCurrentOperator.mockResolvedValue({ authenticated: false, googleLoginEnabled: false });

    render(<App />);

    await waitFor(() => expect(fetchCurrentOperator).toHaveBeenCalled());
    const demoOperatorOption = screen.getByRole("button", { name: /Continue with Demo operator/ });
    const googleOption = screen.getByRole("button", { name: /Continue with Google/ });

    expect(googleOption).toBeDisabled();
    expect(demoOperatorOption.compareDocumentPosition(googleOption)).toBe(Node.DOCUMENT_POSITION_FOLLOWING);
    expect(screen.getByText(/Google login is disabled because GOOGLE_OAUTH_CLIENT_SECRET is not configured/))
      .toBeInTheDocument();
  });

  it("opens the demo operator chooser from the mock login option", async () => {
    render(<App />);

    fireEvent.click(await screen.findByRole("button", { name: /Continue with Demo operator/ }));

    expect(screen.getByRole("dialog", { name: "Choose demo operator" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /Sarah Connor/ })).toHaveAttribute(
      "href",
      "/mock-login?operator=analyst-one"
    );
    expect(screen.getByRole("link", { name: /John McClane/ })).toHaveAttribute(
      "href",
      "/mock-login?operator=risk-reviewer"
    );
  });

  it("opens privacy and terms notices from the landing page", async () => {
    render(<App />);

    fireEvent.click(await screen.findByRole("button", { name: "Privacy & Data" }));

    expect(screen.getByRole("dialog", { name: "Privacy & Data" })).toBeInTheDocument();
    expect(screen.getByText(/operator display name on AI analysis records/)).toBeInTheDocument();
    expect(screen.getByText(/does not persist the operator's email/)).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Close legal notice" }));
    fireEvent.click(screen.getByRole("button", { name: "Terms & Conditions" }));

    expect(screen.getByRole("dialog", { name: "Terms & Conditions" })).toBeInTheDocument();
    expect(screen.getByText(/moderator approval, role checks/)).toBeInTheDocument();
  });

  it("renders the success page for authenticated operators", async () => {
    fetchCurrentOperator.mockResolvedValue({
      authenticated: true,
      name: "Demo Operator",
      email: "operator@example.com",
      provider: "google"
    });

    render(<App />);

    expect(await screen.findByRole("heading", { name: "Note" })).toBeInTheDocument();
    expect(screen.getByText("Welcome, Demo Operator")).toBeInTheDocument();
    expect(screen.getByText(/all authenticated users have access/)).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Search activity by Customer ID" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Logout" })).toBeInTheDocument();
  });
});
