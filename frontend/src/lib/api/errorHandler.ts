import axios from "axios";

interface BackendError {
  message?: string;
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError<BackendError>(error)) {
    if (!error.response) {
      return "Unable to connect to server.";
    }

    return (
      error.response.data?.message ??
      "Something went wrong. Please try again."
    );
  }

  return "Unexpected error occurred.";
}