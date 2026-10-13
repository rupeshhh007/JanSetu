# JanSetu deployment runbook

## Target architecture

- **Web:** Vercel project connected to `rupeshhh007/JanSetu`, root directory `frontend`, Next.js 16, Node.js 22, production branch `main`.
- **API:** Render **web service**, Docker runtime, repository root directory `backend`, health-check path `/api/v1/health/ready`. Java 21, Spring Boot 3.5.
- **Data:** dedicated PostgreSQL database, ideally Supabase in a nearby region. This project uses standard JDBC, **not** Supabase's browser/client REST API or Supabase Auth.

**Do not mix JanSetu tables into the databases of unrelated applications.** Use a separate database or explicitly approved, isolated schema and credentials. The existing Supabase free-plan quota may prevent adding another free project.

## Deployment order

1. Confirm provider/workspace selection, budget, region, and dedicated database ownership.
2. Provision a new PostgreSQL database. Create a dedicated app login; allow TLS connections and least-privilege DB access. Do not grant browser `anon` or `authenticated` API access to JanSetu tables.
3. Create Render Docker web service using `backend/`, with required secrets configured in Render's **secret** environment variables. Use a persistent database; the service filesystem is ephemeral.
4. Confirm /api/v1/health/ready succeeds and Flyway migrations through V15 have applied cleanly.
5. Configure Vercel with root directory `frontend`, framework Next.js, and `NEXT_PUBLIC_API_BASE_URL=https://<confirmed-api-host>`. This URL is public; no backend secret belongs here.
6. Set `CORS_ALLOWED_ORIGINS=https://<confirmed-frontend-host>` on Render (comma-separated if using additional trusted origins).
7. Deploy the tested commit and verify sign-up, sign-in, citizen profile, schemes, preparation, and admin operations using synthetic accounts.
8. Configure optional administrator bootstrap **only once** with an operator-owned email and random 12+ character password; remove bootstrap password from environment after initial creation. Do not use a government-looking domain for an unaffiliated account.
9. Monitor application logs and connection pool. Enable backups, incident recovery, and retention policies before processing real citizen information.

## Required Render environment variables

| Variable | Purpose |
| --- | --- |
| `SPRING_PROFILES_ACTIVE=production` | Activates strict production settings |
| `DATABASE_URL` | JDBC PostgreSQL connection string, e.g. `jdbc:postgresql://host:5432/postgres?sslmode=require` (never commit the actual value) |
| `DATABASE_USERNAME` | Database login |
| `DATABASE_PASSWORD` | Database password |
| `JWT_SECRET_KEY` | Random high-entropy secret, **at least 32 bytes**, never reuse local example |
| `CORS_ALLOWED_ORIGINS` | Exact deployed frontend HTTPS origin(s) |
| `PORT` | Render injects this automatically; backend defaults to 8080 |
| `JAVA_TOOL_OPTIONS` | Optional memory tuning for instance size; measure actual usage |
| `JANSETU_ADMIN_EMAIL`, `JANSETU_ADMIN_PASSWORD` | Optional one-time bootstrap; remove password afterward |

AI is **disabled by default**. Do not configure `GEMINI_API_KEY` or enable provider calls without an approved budget and privacy review.

### Connectivity note

Supabase direct database hosts can be IPv6-only on some networks. Use a supported **Supavisor session pooler** connection string if the API host cannot reach the direct database endpoint. Preserve TLS; do **not** disable SSL to work around connectivity problems. Avoid transaction-pooler behavior for Flyway/schema migration connections unless verified compatible.

## Deployment verification checklist

- [ ] TLS/HTTPS works for frontend and API
- [ ] Production starts without development JWT secrets
- [ ] Health readiness confirms actual PostgreSQL connectivity
- [ ] Flyway migration history is current and `ddl-auto=validate` passes
- [ ] Backend Swagger/OpenAPI endpoints are disabled in production
- [ ] Citizen signup cannot register an ADMIN role
- [ ] Tenant/session isolation works for profile and preparation data
- [ ] CORS allows only the exact HTTPS frontend origin
- [ ] Public web app uses deployed API, not `http://localhost:8080`
- [ ] No sensitive provider token, JDBC password, or JWT key in Vercel's `NEXT_PUBLIC_*` variables
- [ ] Admin and citizen operations are smoke-tested using synthetic data
- [ ] Backups and restore process are tested before accepting genuine sensitive user data

## Limits of low-cost hosting

Render free instances sleep and may have slow cold starts. A free Render PostgreSQL instance may expire after 30 days; avoid it for persistent citizen records. Supabase's Free plan has a maximum of two active Free projects. Stable public operation requires explicit decisions about database hosting, billing, retention, backup, and availability.

JanSetu is independent decision-support software. It does not submit applications or certify government eligibility.
