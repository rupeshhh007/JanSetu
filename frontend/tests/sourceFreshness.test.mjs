import test from "node:test";
import assert from "node:assert/strict";
import { schemeSourceFreshness } from "../src/features/schemes/sourceFreshness.ts";

const now = Date.parse("2026-10-14T12:00:00Z");
const daysAgo = (days) => new Date(now - days * 86_400_000).toISOString();

test("unknown or invalid dates are not labeled verified", () => {
  assert.equal(schemeSourceFreshness(null, now).status, "missing");
  assert.equal(schemeSourceFreshness("garbage", now).status, "missing");
  assert.equal(schemeSourceFreshness(new Date(now + 4 * 86_400_000).toISOString(), now).status, "missing");
});

test("source check within 180 days is informational, not government verification", () => {
  const result = schemeSourceFreshness(daysAgo(179), now);
  assert.equal(result.status, "recent");
  assert.match(result.text, /Last checked/);
  assert.doesNotMatch(result.text, /government approved|officially verified/i);
});

test("source checks older than 180 days warn citizens to recheck requirements", () => {
  const result = schemeSourceFreshness(daysAgo(181), now);
  assert.equal(result.status, "stale");
  assert.match(result.text, /recheck requirements/);
});

test("source freshness uses UTC and deterministic date labels", () => {
  assert.equal(schemeSourceFreshness("2026-10-01T00:00:00Z", now).text, "Last checked Oct 2026");
});
