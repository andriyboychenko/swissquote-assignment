import React, { useEffect, useState } from "react";
import { fetchCurrentOperator } from "./api/authApi";
import { AuthSuccessPage } from "./components/AuthSuccessPage";
import { DemoOperatorModal } from "./components/DemoOperatorModal";
import { LegalNoticeActions } from "./components/LegalNoticeActions";
import { LegalNoticeModal } from "./components/LegalNoticeModal";
import { LoginOption } from "./components/LoginOption";
import "./styles.css";

const loginOptions = [
  {
    provider: "Google",
    description: "Continue with your Google account",
    href: "/oauth2/authorization/google",
    symbol: "G",
    variant: "google"
  },
  {
    provider: "Demo operator",
    description: "Choose a local mock operator session",
    symbol: "D",
    variant: "mock",
    opensDemoOperatorChooser: true
  }
];

const legalNotices = {
  privacy: {
    title: "Privacy & Data",
    body:
      "This demo stores the OAuth or mock provider, a pseudonymous account identifier hash, access status, login timestamps, and the operator display name on AI analysis records for audit attribution. It does not persist the operator's email. Customer activity data is used only to support the analytics and risk review workflow."
  },
  terms: {
    title: "Terms & Conditions",
    body:
      "This assignment demo is provided for evaluation and development purposes. All authenticated demo users can access the platform. In a real deployment, access would require moderator approval, role checks, audit controls, and approved operating policies."
  }
};

export default function App() {
  const [operator, setOperator] = useState(null);
  const [authStatus, setAuthStatus] = useState("loading");
  const [activeNotice, setActiveNotice] = useState(null);
  const [isDemoOperatorModalOpen, setIsDemoOperatorModalOpen] = useState(false);

  const selectedNotice = activeNotice ? legalNotices[activeNotice] : null;

  useEffect(() => {
    let active = true;

    async function loadCurrentOperator() {
      try {
        const currentOperator = await fetchCurrentOperator();

        if (!active) {
          return;
        }

        if (currentOperator.authenticated) {
          setOperator(currentOperator);
          setAuthStatus("authenticated");
        } else {
          setAuthStatus("anonymous");
        }
      } catch {
        if (active) {
          setAuthStatus("anonymous");
        }
      }
    }

    loadCurrentOperator();

    return () => {
      active = false;
    };
  }, []);

  if (authStatus === "loading") {
    return (
      <main className="loading-shell" aria-label="Loading operator session">
        Loading operator session
      </main>
    );
  }

  if (authStatus === "authenticated") {
    return <AuthSuccessPage operatorName={operator.name || operator.email || "Operator"} />;
  }

  return (
    <main className="landing-shell">
      <section className="welcome-panel" aria-labelledby="welcome-title">
        <div className="welcome-copy">
          <p className="eyebrow">Customer Activity Analytics</p>
          <h1 id="welcome-title">Operator dashboard for client activity and risk</h1>
          <p className="welcome-message">
            Search customer activity, review card, payment, and crypto behavior,
            and request AI-assisted risk findings with policy-aware recommendations.
          </p>
        </div>

        <div className="login-panel" aria-label="Login options">
          <p className="panel-kicker">Operator access</p>
          <h2>Sign in to monitor customers</h2>
          <div className="login-options">
            {loginOptions.map((option) => (
              <LoginOption
                key={option.provider}
                provider={option.provider}
                description={option.description}
                href={option.href}
                symbol={option.symbol}
                variant={option.variant}
                disabled={option.disabled}
                onClick={option.opensDemoOperatorChooser ? () => setIsDemoOperatorModalOpen(true) : undefined}
              />
            ))}
          </div>
          <LegalNoticeActions onOpenNotice={setActiveNotice} />
        </div>
      </section>
      <LegalNoticeModal notice={selectedNotice} onClose={() => setActiveNotice(null)} />
      <DemoOperatorModal isOpen={isDemoOperatorModalOpen} onClose={() => setIsDemoOperatorModalOpen(false)} />
    </main>
  );
}
