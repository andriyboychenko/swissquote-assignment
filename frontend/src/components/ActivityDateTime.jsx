import React from "react";
import CalendarDays from "lucide-react/dist/esm/icons/calendar-days.mjs";
import Clock from "lucide-react/dist/esm/icons/clock.mjs";

const DATE_FORMATTER = new Intl.DateTimeFormat("en-GB", {
  dateStyle: "medium"
});

const TIME_FORMATTER = new Intl.DateTimeFormat("en-GB", {
  timeStyle: "short"
});

function formatActivityDateTime(value) {
  const date = new Date(value);

  return {
    date: DATE_FORMATTER.format(date),
    time: TIME_FORMATTER.format(date)
  };
}

export function ActivityDateTime({ value }) {
  const formatted = formatActivityDateTime(value);

  return (
    <span className="activity-date-time">
      <span className="date-line">
        <CalendarDays aria-hidden="true" size={13} strokeWidth={2} />
        <span>{formatted.date}</span>
      </span>
      <span className="time-line">
        <Clock aria-hidden="true" size={12} strokeWidth={2} />
        <span>{formatted.time}</span>
      </span>
    </span>
  );
}
