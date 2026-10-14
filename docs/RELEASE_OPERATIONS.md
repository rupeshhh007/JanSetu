# JanSetu release operations — free-tier pilot

This runbook describes **verified intent**, not proof that any particular release is already safe.
The live architecture is GitHub main → Render Java 21 API → dedicated Neon PostgreSQL 17,
plus GitHub main → Vercel Next.js 16 frontend.

## Release identity and gates

1. Record the proposed exact 40-character Git commit SHA. Never deploy a moving branch without knowing its current SHA.
2. Review CodeQL, Dependabot and existing open security alerts. Require successful:
   - backend `./mvnw -B test` (H2 + domain + MockMvc)
   - dedicated PostgreSQL Testcontainers integration test
   - frontend `npm ci`, unit tests, ESLint, TypeScript and Next production build
3. Inspect diff and any Flyway Vxx migration for constraints, indexes, data impact and irreversible operations.
4. Deploy a disposable preview first; do not connect preview tests to live citizen records.
5. Review CORS, allowed origins and `NEXT_PUBLIC_API_BASE_URL` against actual HTTPS deployment domains.
6. Agree on live acceptance checks before merging. CI passing is a **necessary**, not **sufficient**, condition.

## Secret boundaries

- Render holds `DATABASE_URL` (JDBC, SSL required), username, password, `JWT_SECRET_KEY`, production profile and exact allowed HTTPS browser origins.
- Never put database strings, password, JWT key or internal provider credentials in GitHub, frontend `NEXT_PUBLIC_*` settings, logs or issue comments.
- Keep optional model provider disabled until an explicit privacy, rate and cost review.
- Never print `env`, `printenv`, or full provider connection strings while diagnosing public logs.

## Migrations

The active baseline before this initiative is Flyway V15.
The product branch introduces:
- **V16** citizen-owned saved schemes: indexes and uniqueness.
- **V17** per-account token version: existing accounts receive version 0; password rotation increments it.

Both are additive but are still **database writes** and require snapshot/recovery planning.
Execute normal Flyway startup only after a recoverable database backup, with production held to the reviewed SHA. Inspect `flyway_schema_history` for exactly one successful row per migration and verify Hibernate `ddl-auto=validate`. Do not delete migration records to force a failed release.

## Validation after the release

1. Render `/api/v1/health/ready` responds `{"status":"UP"}`. Also verify no startup exceptions and 17 successful migrations (after V17 goes live).
2. Browser opens on `https://jansetu-2-0.vercel.app`; no Vercel Authentication required on production.
3. Use **synthetic** citizen accounts A and B; confirm registration, login, scheme search, account-owned bookmarks, preparation and export, then verify A cannot see B's data.
4. Confirm an unauthenticated request cannot call private APIs; a citizen cannot call `/api/v1/admin/**`.
5. Change A's password; verify prior JWTs are rejected and the new password signs in.
6. Verify 429 throttling on a dedicated isolated test instance, never intentionally lock out real users on the shared free pilot.
7. Verify frontend source-date warnings and no official-status claims.
8. Confirm Render, Vercel and GitHub all report the intended version/commit. If one fails, do not claim a full-stack release.

## Rollback and recovery

- If frontend-only regression: Vercel rollback/promotion to a **known-good production deployment**.
- If API regression: deploy/revert a known-good Render commit; do not roll back DB schema blindly.
- If migration changed data semantics: assess compatibility first; use a rehearsed restoration path and maintenance window when needed.
- If JWT secret leaks: rotate Render secret and invalidate all older tokens; communicate expected sign-out.
- If database credential leaks: rotate Neon credential and update Render privately, verify connectivity.
- Log incident timestamps, affected commit, request IDs, and confirmed impact **without** personal details.

## Resource limitations

Render free services can suspend, causing cold starts. Single-instance rate limits are not
distributed controls. Free database tiers have finite quotas and recovery capabilities.
Do not onboard real citizens or collect identity documents until privacy, retention, backup,
administrator MFA, recovery and incident operations are explicitly reviewed.
