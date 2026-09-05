import React, { useState } from "react";

const STORAGE_KEY = "swissquote-authentication-notice-minimized";

function readInitialMinimizedState() {
  if (typeof window.localStorage?.getItem !== "function") {
    return false;
  }

  return window.localStorage.getItem(STORAGE_KEY) === "true";
}

function rememberMinimizedState(isMinimized) {
  if (typeof window.localStorage?.setItem === "function") {
    window.localStorage.setItem(STORAGE_KEY, String(isMinimized));
  }
}

export function AuthenticationNotice({ operatorName }) {
  const [isMinimized, setIsMinimized] = useState(readInitialMinimizedState);

  function handleToggleMinimized() {
    const nextState = !isMinimized;
    setIsMinimized(nextState);
    rememberMinimizedState(nextState);
  }

  return (
    <section className="auth-notice" aria-labelledby="auth-notice-title">
      <div className="auth-notice-header">
        <div>
          <h2 id="auth-notice-title">Note</h2>
        </div>
        <button className="notice-toggle" type="button" onClick={handleToggleMinimized}>
          {isMinimized ? "Show" : "Hide"}
        </button>
      </div>

      {isMinimized ? null : (
        <div className="success-disclaimer">
          <p className="auth-welcome">Welcome, {operatorName}</p>
          <p>
            Since this is a demo, all authenticated users have access to the platform.
            In a real scenario, operator access would need to be reviewed and accepted
            by a moderator before the dashboard becomes available.
          </p>
        </div>
      )}
    </section>
  );
}
