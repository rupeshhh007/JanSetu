export type ResponseType =
  | "recommendation"
  | "comparison"
  | "status"
  | "markdown";

export function detectResponseType(
  message: string
): ResponseType {

  const text = message.toLowerCase();

  // -----------------------------
  // APPLICATION STATUS
  // -----------------------------
  if (
    text.includes("application history") &&
    text.includes("scheme:") &&
    text.includes("status:")
  ) {
    return "status";
  }

  // -----------------------------
  // COMPARISON
  // -----------------------------
  if (
    text.includes("compare schemes") &&
    text.includes("vs")
  ) {
    return "comparison";
  }

  // -----------------------------
  // RECOMMENDATION
  // -----------------------------
  if (
    text.includes("priority recommendation") ||
    (
      text.includes("top match") &&
      text.includes("personalized recommendation")
    )
  ) {
    return "recommendation";
  }

  // -----------------------------
  return "markdown";
}