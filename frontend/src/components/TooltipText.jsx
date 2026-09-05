import React from "react";

export function TooltipText({ as: Element = "span", ariaLabel = undefined, children, className, tooltip }) {
  return (
    <Element
      aria-label={ariaLabel}
      className={`tooltip-anchor ${className ?? ""}`.trim()}
      data-tooltip={tooltip}
      tabIndex={0}
    >
      {children}
    </Element>
  );
}
