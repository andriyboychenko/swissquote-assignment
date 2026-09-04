import React from "react";

export function OperatorMenu({ operatorName }) {
  return (
    <nav className="operator-menu" aria-label="Operator menu">
      <span className="operator-name">{operatorName}</span>
      <a className="logout-link" href="/logout">
        Logout
      </a>
    </nav>
  );
}
