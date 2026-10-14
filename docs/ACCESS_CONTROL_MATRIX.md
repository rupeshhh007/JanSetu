# JanSetu authorization test matrix

This matrix covers the existing architecture plus the hardening branch. It states
intended access rules; verify actual outcomes with MockMvc and two synthetic citizens
before enabling real profiles. The backend is the authority. Hiding a frontend link
does **not** provide authorization.

| Resource / operation | Anonymous | Citizen A | Citizen B accessing A | Admin |
| --- | --- | --- | --- | --- |
| POST `/api/v1/auth/login` | Public, throttled | Public, throttled | No account enumeration | Public, throttled |
| POST `/api/v1/auth/register` | Public, throttled, creates citizen | Public, throttled | Cannot specify/admin-create role | Same public rule |
| GET `/api/v1/health/ready` | Status only | Status only | Status only | Status only |
| GET `/api/v1/schemes` | Requires sign-in | Published catalogue | Same public catalogue | Published catalogue |
| POST/PUT/DELETE `/api/v1/schemes/**` | Denied | Denied | Denied | Allowed with publication checks |
| `/api/v1/admin/**` | Denied | Denied | Denied | Admin role required |
| GET/PUT/DELETE `/api/v1/citizen/saved-schemes/**` | Denied | Own bookmarks only | Cannot read/delete A's state | Requires sign-in; any use should be reviewed separately |
| GET `/api/v1/citizen/profile` | Denied | Own profile | Cannot fetch A by UUID | Only current signed-in profile where supported |
| `/api/v1/preparations/**` | Denied | Own preparation | Unknown session/task returns not found | Must respect citizen-owned session identity |
| GET `/api/v1/citizen/data-export` | Denied | Own DTOs only, no-store | No target ID accepted | Any use should remain scoped to caller, not a backdoor |
| POST `/api/v1/citizen/account/password` | Denied | Current password required; own account only | No target ID accepted | Must require current password |
| `/api/v1/notifications/**` | Denied | Own notifications | B cannot mark A's notification read | Must respect owner |

## Explicit tenant-boundary attack cases

1. Citizen A creates and bookmarks a published scheme; citizen B lists bookmarks and sees an empty set.
2. Citizen B sends DELETE with A's scheme UUID and must not remove A's saved record.
3. Citizen A owns a preparation session UUID; B tries to read or update A's task and receives 404.
4. Citizen A sees only its profile facts, including each explicit consent scope. No reusable fact is exposed to a caseworker without a valid consent scope and expiry check.
5. Citizen B cannot replay A's policy assessment ID or mark A's policy notification as read.
6. A wrong password, disabled account and unknown login email cannot reveal user existence via different public error descriptions.
7. Anonymous calls cannot reach new or accidentally added routes under `/api/v1/auth/**`; explicitly list only login/register as public.
8. Password rotation revokes A's prior JWTs, without changing B's session version.
9. A user-supplied JWT role claim cannot elevate privileges. Backend roles are loaded from the database.
10. Preflight from an untrusted domain is rejected; a trusted origin is allowed **without** cross-site cookie credentials.

## Negative cases must be tested

- UUIDs must never serve as authorization.
- State filters, browser-side route guards and admin menu visibility are presentation features, not permissions.
- Raw JPA entities, hashed passwords, JWTs, API keys and Neon credentials are never response DTO fields.
- Errors should return generic safe text with a server-owned X-Request-ID for support, not input-containing traces.
- Counterfeit or stale government documentation must not be labeled officially verified. Only human-reviewed policy versions may be published.

## Before launch

Review the matrix when adding every new endpoint. Attach API-level negative tests, not just
frontend snapshots. Never run destructive horizontal-access tests against real citizens.
The independent JanSetu pilot does not represent an official government identity verification or application status system.
