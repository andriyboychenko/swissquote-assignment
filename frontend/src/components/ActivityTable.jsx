import React from "react";
import { ActivityDateTime } from "./ActivityDateTime";
import { ActivityDetail } from "./ActivityDetail";
import { ActivityMovementIndicator, getActivityMovement } from "./ActivityMovementIndicator";
import { ActivityRiskIndicator } from "./ActivityRiskIndicator";
import { TooltipText } from "./TooltipText";

function formatAmount(amount, currency) {
  return `${Number(amount).toLocaleString("en-GB", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })} ${currency}`;
}

const COLUMNS = [
  { key: "createdAt", label: "Created" },
  { key: "activityType", label: "Type" },
  { key: "status", label: "Status" },
  { key: "amount", label: "Amount" },
  { key: "counterparty", label: "Counterparty" },
  { key: "channel", label: "Channel" },
  { key: "detail", label: "Details" }
];

export function ActivityTable({ activities, hasMore, isLoadingMore, sort, onSortChange, onLoadMore }) {
  function handleScroll(event) {
    const { clientHeight, scrollHeight, scrollTop } = event.currentTarget;
    const isNearBottom = scrollHeight - scrollTop - clientHeight < 80;

    if (isNearBottom && hasMore && !isLoadingMore) {
      onLoadMore();
    }
  }

  return (
    <div className="activity-table-wrap" onScroll={handleScroll}>
      <table className="activity-table">
        <colgroup>
          <col className="created-column" />
          <col className="type-column" />
          <col className="status-column" />
          <col className="amount-column" />
          <col className="counterparty-column" />
          <col className="channel-column" />
          <col className="details-column" />
        </colgroup>
        <thead>
          <tr>
            {COLUMNS.map((column) => (
              <th key={column.key}>
                <button className="sort-button" type="button" onClick={() => onSortChange(column.key)}>
                  <span>{column.label}</span>
                  <span className="sort-indicator" aria-hidden="true">
                    {sort.sortBy === column.key ? sortIndicator(sort.sortDirection) : ""}
                  </span>
                </button>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {activities.map((activity) => {
            const movement = getActivityMovement(activity);
            const riskIndicators = activity.riskIndicators ?? [];
            const hasRiskIndicators = riskIndicators.length > 0;

            return (
              <tr
                className={hasRiskIndicators ? "activity-row-suspicious" : ""}
                key={activity.transactionId}
              >
                <td>
                  <div className="created-risk-cell">
                    <ActivityDateTime value={activity.createdAt} />
                    <ActivityRiskIndicator riskIndicators={riskIndicators} />
                  </div>
                </td>
                <td>
                  <span className={`activity-type ${activity.activityType.toLowerCase()}`}>
                    {activity.activityType}
                  </span>
                </td>
                <td>
                  <span className={`status-pill ${activity.status.toLowerCase()}`}>{activity.status}</span>
                </td>
                <td className={`amount-cell amount-${movement.className}`}>
                  <span className="amount-content">
                    <ActivityMovementIndicator movement={movement} />
                    <span>{formatAmount(activity.amount, activity.currency)}</span>
                  </span>
                </td>
                <td>
                  <TooltipText className="counterparty-tooltip" tooltip={activity.counterparty}>
                    <span className="counterparty-value">{activity.counterparty}</span>
                  </TooltipText>
                </td>
                <td>{activity.channel}</td>
                <td><ActivityDetail detail={activity.detail} /></td>
              </tr>
            );
          })}
        </tbody>
      </table>
      {isLoadingMore ? <p className="table-state">Loading more activity...</p> : null}
      {hasMore && !isLoadingMore ? <p className="table-state">Scroll to load more</p> : null}
    </div>
  );
}

function sortIndicator(direction) {
  return direction === "ASC" ? "▲" : "▼";
}
