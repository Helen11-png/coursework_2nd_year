const API_PREFIX = "/api";

const DEFAULT_ERROR_MESSAGE = "Что-то пошло не так, попробуйте позже";

export class ApiError extends Error {
  constructor(message, code, status, fields = []) {
    super(message);

    this.name = "ApiError";
    this.code = code;
    this.status = status;
    this.fields = fields;
  }
}

export async function apiRequest(path, options = {}) {
  const { headers, body, ...restOptions } = options;

  const requestHeaders = new Headers(headers);

  if (body != null && !(body instanceof FormData)) {
    requestHeaders.set("Content-Type", "application/json");
  }

  let response;

  try {
    response = await fetch(`${API_PREFIX}${path}`, {
      ...restOptions,
      headers: requestHeaders,
      body:
        body != null && !(body instanceof FormData)
          ? JSON.stringify(body)
          : body,
    });
  } catch {
    throw new ApiError(DEFAULT_ERROR_MESSAGE, "NETWORK_ERROR", 0);
  }

  if (response.status === 204) {
    return null;
  }

  const responseText = await response.text();

  let data;

  try {
    data = responseText ? JSON.parse(responseText) : null;
  } catch {
    throw new ApiError(
      DEFAULT_ERROR_MESSAGE,
      "INVALID_RESPONSE",
      response.status,
    );
  }

  if (!response.ok) {
    if (!data || typeof data !== "object" || typeof data.code !== "string") {
      throw new ApiError(
        DEFAULT_ERROR_MESSAGE,
        "INVALID_RESPONSE",
        response.status,
      );
    }

    throw new ApiError(
      data.code === "INTERNAL_ERROR"
        ? DEFAULT_ERROR_MESSAGE
        : data.message || DEFAULT_ERROR_MESSAGE,
      data.code,
      response.status,
      Array.isArray(data.fields) ? data.fields : [],
    );
  }

  return data;
}
