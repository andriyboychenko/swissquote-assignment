const CSRF_COOKIE_NAME = "XSRF-TOKEN";
const CSRF_HEADER_NAME = "X-XSRF-TOKEN";
const MUTATING_METHODS = new Set(["POST", "PUT", "PATCH", "DELETE"]);

export function readCsrfToken() {
  return document.cookie
    .split(";")
    .map((cookie) => cookie.trim())
    .find((cookie) => cookie.startsWith(`${CSRF_COOKIE_NAME}=`))
    ?.slice(CSRF_COOKIE_NAME.length + 1) ?? "";
}

export function csrfFetch(url, options = {}) {
  const method = (options.method ?? "GET").toUpperCase();

  if (!MUTATING_METHODS.has(method)) {
    return fetch(url, {
      credentials: "include",
      ...options
    });
  }

  const csrfToken = readCsrfToken();
  const headers = csrfToken
    ? {
        ...options.headers,
        [CSRF_HEADER_NAME]: csrfToken
      }
    : options.headers;

  return fetch(url, {
    credentials: "include",
    ...options,
    headers
  });
}
