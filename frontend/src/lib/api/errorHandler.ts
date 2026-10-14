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

    return (
      error.response.data?.message ??
      "Something went wrong. Please try again."
    );
  }

  return "Unexpected error occurred.";
}