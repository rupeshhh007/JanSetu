# JanSetu — path from working pilot to trusted citizen product

JanSetu is independent decision support, **not** a government application submission service. The aim is to help citizens find evidence-backed schemes, understand eligibility gaps, and prepare documents without misrepresenting official approval.

## Stage 1 — Secure and reliable pilot
- Patch frontend framework and keep transitive dependencies tracked.
- Lock public endpoints behind correct role and citizen-record ownership checks.
- Protect brute-force login and account creation; don't log raw credentials.
- Add a citizen-owned durable shortlist, clear source timestamps and a consistent preparation journey.
- Give citizens data export and password rotation, including session invalidation.
- Restrict production secrets to Render, verify Neon Flyway and API readiness.
- Run Java, Next, PostgreSQL, CodeQL and dependency checks on reviewed PRs.

**Acceptance:** two synthetic citizen accounts cannot access each other's private data; no admin APIs accessible to citizen; no HTTP 500 on expected invalid requests; full UI/API flow on live environment.

## Stage 2 — Private and recoverable accounts
- Verify email ownership before enabling account recovery.
- Password-reset flow with one-time, short-lived, hashed tokens and throttle/abuse checks.
- Administrator MFA and access review.
- Real export/erasure/retention and legal privacy contact workflow.
- Restore testing and secure database snapshots/backups before collecting real personal records.
- HttpOnly cookie or BFF-based sessions with CSRF defense and a reviewed CSP.

**Acceptance:** session can be revoked, identity recovered without enumeration, user data can be removed and backup recovery exercised in a staging environment.

## Stage 3 — High-quality scheme coverage
- Curated authoritative evidence ingestion; official links, provenance, verification timestamp and freshness policy.
- Deterministic rule-engine outputs with understandable evaluation traces, version changes and human-reviewed publication.
- Citizen notification when a policy change affects a previous assessment.
- Accessibility testing with keyboard and screen reader, low-bandwidth and Hindi-first workflows.

**Acceptance:** schemes are supported by dated citations, no automated model guesses become published rules, and adverse policy changes are reviewable by operators.

## Stage 4 — Operational maturity
- Shared rate limits, metrics, request tracing, durable queues and incident playbooks.
- Load tests, uptime and service-level objectives, cost budgets and alerting.
- Named ownership for support, source reviews, deployments and data lifecycle.
- Real-user pilot only after legal/privacy and operator sign-off.

**Acceptance:** repeatable deployment, rollback and incident response, predictable costs and monitored availability.

## Non-goals
No autonomous submissions to government portals, no invented official application status, no upload of Aadhaar/identity documents in the pilot, and no use of personal profiles for an external model without approved consent and policy.
