import React from "react";
import { OperatorMenu } from "./OperatorMenu";

export function AuthSuccessPage({ operatorName }) {
  return (
    <main className="success-shell">
      <OperatorMenu operatorName={operatorName} />
      <section className="success-panel" aria-labelledby="success-title">
        <p className="eyebrow">Authentication successful</p>
        <h1 id="success-title">Welcome, {operatorName}</h1>
        <p className="success-disclaimer">
          Since this is a demo, all authenticated users have access to the platform.
          In a real scenario, operator access would need to be reviewed and accepted
          by a moderator before the dashboard becomes available.
        </p>
      </section>
    </main>
  );
}
