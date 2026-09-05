import React from "react";
import { AuthenticationNotice } from "./AuthenticationNotice";
import { CustomerActivityDashboard } from "./CustomerActivityDashboard";
import { OperatorMenu } from "./OperatorMenu";

export function AuthSuccessPage({ operatorName }) {
  return (
    <main className="success-shell">
      <OperatorMenu operatorName={operatorName} />
      <AuthenticationNotice operatorName={operatorName} />
      <CustomerActivityDashboard />
    </main>
  );
}
