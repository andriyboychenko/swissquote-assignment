import React from "react";

export function LoginOption({ provider, description, href, symbol, variant, disabled = false }) {
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
  ) : (
    <a className={`login-option ${variant}`} href={href}>
      {content}
    </a>
  );
}
