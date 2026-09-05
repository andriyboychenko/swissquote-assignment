import React from "react";

const summaryItems = [
  { key: "totalActivities", label: "Total" },
  { key: "cardActivities", label: "Card" },
  { key: "paymentActivities", label: "Payments" },
  { key: "cryptoActivities", label: "Crypto" },
  { key: "failedActivities", label: "Failed" },
  { key: "pendingActivities", label: "Pending" }
];

export function ActivitySummary({ summary }) {
  return (
    <div className="activity-summary" aria-label="Customer activity summary">
      {summaryItems.map((item) => (
        <div className="summary-item" key={item.key}>
          <span className="summary-value">{summary[item.key]}</span>
          <span className="summary-label">{item.label}</span>
        </div>
      ))}
    </div>
  );
}
