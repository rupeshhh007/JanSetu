/**
 * A catalogue timestamp means JanSetu last checked the cited source.
 * It is never evidence of government approval or a guaranteed benefit.
 */
export type SourceFreshness =
  | { status: "missing"; text: string }
  | { status: "stale"; text: string }
  | { status: "recent"; text: string };

const DAY_MS = 86_400_000;
const MAX_AGE_DAYS = 180;

export function schemeSourceFreshness(lastVerifiedAt: string | null | undefined, now = Date.now()): SourceFreshness {
  if (!lastVerifiedAt) return { status: "missing", text: "Source check date not recorded" };
  const parsed = Date.parse(lastVerifiedAt);
  if (!Number.isFinite(parsed) || parsed > now + DAY_MS) {
    return { status: "missing", text: "Source check date not available" };
  }
  const label = new Intl.DateTimeFormat("en-IN", { month: "short", year: "numeric", timeZone: "UTC" })
    .format(new Date(parsed));
  if (now - parsed > MAX_AGE_DAYS * DAY_MS) {
    return { status: "stale", text: `Last checked ${label} · recheck requirements` };
  }
  return { status: "recent", text: `Last checked ${label}` };
}
