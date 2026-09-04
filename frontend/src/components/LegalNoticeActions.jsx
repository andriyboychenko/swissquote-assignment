import React from "react";

export function LegalNoticeActions({ onOpenNotice }) {
  return (
    <div className="legal-actions" aria-label="Legal information">
      <button className="legal-action" type="button" onClick={() => onOpenNotice("privacy")}>
        Privacy & Data
      </button>
      <button className="legal-action" type="button" onClick={() => onOpenNotice("terms")}>
        Terms & Conditions
      </button>
    </div>
  );
}
