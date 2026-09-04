import React from "react";

export function LegalNoticeModal({ notice, onClose }) {
  if (!notice) {
    return null;
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section className="legal-modal" role="dialog" aria-modal="true" aria-labelledby="legal-modal-title">
        <div className="legal-modal-header">
          <h2 id="legal-modal-title">{notice.title}</h2>
          <button className="modal-close" type="button" aria-label="Close legal notice" onClick={onClose}>
            x
          </button>
        </div>
        <p>{notice.body}</p>
      </section>
    </div>
  );
}
