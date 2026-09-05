import React, { useState } from "react";
import { fetchPolicySection } from "../api/policiesApi";

export function PolicyEvidenceLink({ sourceReference }) {
  const [policySection, setPolicySection] = useState(null);
  const [status, setStatus] = useState("idle");
  const [error, setError] = useState("");

  async function handleOpenPolicy() {
    setStatus("loading");
    setError("");

    try {
      const loadedPolicySection = await fetchPolicySection(sourceReference);
      setPolicySection(loadedPolicySection);
      setStatus("loaded");
    } catch (exception) {
      setError(exception.message);
      setStatus("failed");
    }
  }

  function handleClosePolicy() {
    setPolicySection(null);
    setStatus("idle");
    setError("");
  }

  return (
    <>
      <button
        className="policy-link-button"
        type="button"
        onClick={handleOpenPolicy}
        disabled={status === "loading"}
      >
        {status === "loading" ? "Opening policy..." : sourceReference}
      </button>
      {error ? <span className="policy-link-error">{error}</span> : null}
      {policySection ? (
        <div className="modal-backdrop" role="presentation">
          <section className="legal-modal policy-modal" role="dialog" aria-modal="true" aria-labelledby="policy-title">
            <div className="legal-modal-header">
              <h2 id="policy-title">{policySection.title}</h2>
              <button className="modal-close" type="button" onClick={handleClosePolicy}>Close</button>
            </div>
            <p className="policy-source">{policySection.sourceReference}</p>
            <p>{policySection.content}</p>
          </section>
        </div>
      ) : null}
    </>
  );
}
