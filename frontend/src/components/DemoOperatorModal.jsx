import React from "react";

const DEMO_OPERATORS = [
  {
    id: "analyst-one",
    name: "Sarah Connor",
    description: "Decisive customer-care operator"
  },
  {
    id: "analyst-two",
    name: "Lisbeth Salander",
    description: "Investigation-focused operator"
  },
  {
    id: "risk-reviewer",
    name: "John McClane",
    description: "Escalation reviewer for high-pressure cases"
  }
];

export function DemoOperatorModal({ isOpen, onClose }) {
  if (!isOpen) {
    return null;
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section className="legal-modal demo-operator-modal" role="dialog" aria-modal="true" aria-labelledby="demo-operator-title">
        <div className="legal-modal-header">
          <h2 id="demo-operator-title">Choose demo operator</h2>
          <button className="modal-close" type="button" aria-label="Close demo operator chooser" onClick={onClose}>
            x
          </button>
        </div>
        <div className="demo-operator-list">
          {DEMO_OPERATORS.map((operator) => (
            <a className="demo-operator-option" href={`/mock-login?operator=${operator.id}`} key={operator.id}>
              <span className="demo-operator-name">{operator.name}</span>
              <span className="demo-operator-description">{operator.description}</span>
            </a>
          ))}
        </div>
      </section>
    </div>
  );
}
