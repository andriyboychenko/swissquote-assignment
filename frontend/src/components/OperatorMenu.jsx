import React from "react";
import { csrfFetch } from "../api/csrfApi";

export function OperatorMenu({ operatorName }) {
  async function handleLogout() {
    const response = await csrfFetch("/logout", { method: "POST" });

    if (response.ok) {
      window.location.assign("/");
    }
  }

  return (
    <nav className="operator-menu" aria-label="Operator menu">
      <span className="operator-name">{operatorName}</span>
      <button className="logout-link" type="button" onClick={handleLogout}>
        Logout
      </button>
    </nav>
  );
}
