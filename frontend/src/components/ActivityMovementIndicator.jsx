import React from "react";
import ArrowDown from "lucide-react/dist/esm/icons/arrow-down.mjs";
import ArrowUp from "lucide-react/dist/esm/icons/arrow-up.mjs";
import CircleQuestionMark from "lucide-react/dist/esm/icons/circle-question-mark.mjs";
import { TooltipText } from "./TooltipText";

export function getActivityMovement(activity) {
  if (activity.status === "Pending") {
    return {
      className: "pending",
      label: "Pending movement",
      tooltip: "This activity is pending, so the final account movement is not confirmed yet.",
      icon: "unknown"
    };
  }

  if (activity.status === "Failed") {
    return {
      className: "neutral",
      label: "No account movement",
      tooltip: "This activity failed, so no account movement is expected.",
      icon: "unknown"
    };
  }

  if (activity.status === "Reversed") {
    return {
      className: "incoming",
      label: "Added back to account",
      tooltip: "This activity was reversed and is displayed as value returning to the account.",
      icon: "up"
    };
  }

  if (activity.activityType === "CARD" && activity.channel === "Credit") {
    return {
      className: "incoming",
      label: "Added to account",
      tooltip: "Completed credit-card activity is displayed as value added to the account.",
      icon: "up"
    };
  }

  return {
    className: "outgoing",
    label: "Removed from account",
    tooltip: "This settled activity is displayed as value leaving the account.",
    icon: "down"
  };
}

export function ActivityMovementIndicator({ movement }) {
  if (movement.icon === "up") {
    return (
      <TooltipText ariaLabel={movement.label} className="movement-icon movement-up" tooltip={movement.tooltip}>
        <ArrowUp aria-hidden="true" size={13} strokeWidth={3} />
      </TooltipText>
    );
  }

  if (movement.icon === "down") {
    return (
      <TooltipText ariaLabel={movement.label} className="movement-icon movement-down" tooltip={movement.tooltip}>
        <ArrowDown aria-hidden="true" size={13} strokeWidth={3} />
      </TooltipText>
    );
  }

  return (
    <TooltipText ariaLabel={movement.label} className="movement-icon movement-unknown" tooltip={movement.tooltip}>
      <CircleQuestionMark aria-hidden="true" size={13} strokeWidth={2.6} />
    </TooltipText>
  );
}
