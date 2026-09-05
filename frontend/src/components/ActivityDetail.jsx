import React from "react";

function splitDeclineReason(detail) {
  const declineMarker = ", Decline: ";
  const declineIndex = detail.indexOf(declineMarker);

  if (declineIndex < 0) {
    return {
      mainDetail: detail,
      declineReason: ""
    };
  }

  return {
    mainDetail: detail.slice(0, declineIndex),
    declineReason: detail.slice(declineIndex + 2)
  };
}

export function ActivityDetail({ detail }) {
  const { mainDetail, declineReason } = splitDeclineReason(detail);

  return (
    <span className="activity-detail">
      <span>{mainDetail}</span>
      {declineReason ? <span className="decline-detail">{declineReason}</span> : null}
    </span>
  );
}
