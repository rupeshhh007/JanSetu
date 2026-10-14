import axios from "axios";

interface BackendError {
  message?: string;
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError<BackendError>(error)) {
    if (!error.response) {
      return "Unable to connect to server.";
    }

    if (error.response.status === 429) {
      return "Too many attempts. Please wait about 15 minutes before trying again.";
    }

    if (error.response.status >= 500) {
      // Render supplies an opaque correlation ID; display it without echoing
      // server exception details or citizen request contents.
      const value = error.response.headers?.["x-request-id"];
      const requestId = typeof value === "string" && /^[0-9a-f-]{36}$/i.test(value)
        ? value
        : null;
      return requestId
        ? `The service is temporarily unavailable. Reference: ${requestId}`
        : "The service is temporarily unavailable. Please try again.";
    }

    return (
      error.response.data?.message ??
      "Something went wrong. Please try again."
    );
  }

  return "Unexpected error occurred.";
}