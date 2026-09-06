import React from "react";

export function LoginOption({ provider, description, href = undefined, symbol, variant, disabled = false, onClick = undefined }) {
  const content = (
    <>
      <span className="provider-symbol" aria-hidden="true">
        {symbol}
      </span>
      <span className="provider-copy">
        <span className="provider-name">Continue with {provider}</span>
        <span className="provider-description">{description}</span>
      </span>
    </>
  );

  return disabled ? (
    <button className={`login-option ${variant}`} disabled type="button">
      {content}
    </button>
  ) : onClick ? (
    <button className={`login-option ${variant}`} type="button" onClick={onClick}>
      {content}
    </button>
  ) : (
    <a className={`login-option ${variant}`} href={href}>
      {content}
    </a>
  );
}
