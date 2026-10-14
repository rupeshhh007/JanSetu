# JanSetu account security (October 2026)

## Public signup
Email is normalized, verified syntactically and created with the CITIZEN role only. Passwords are hashed with BCrypt; never logged or included in a citizen export. New signup passwords require 12–72 characters and at most 72 UTF-8 bytes. Concurrent duplicate registration is handled by the unique database key and a 409 response.

## Authentication and throttling
Invalid account and invalid password both exercise a BCrypt hash comparison and return the same public error; raw email attempts are not logged. A per-address pre-JSON request guard and per-account fixed-window throttle return 429 with Retry-After. These in-JVM controls are a first line of defense on a single Render free instance.

## Sessions
JWTs are signed with a production-only high-entropy secret. Tokens contain the server-stored account token version, rather than a client-provided authority. Every authenticated request resolves the current User from PostgreSQL and rejects a stale token version. Production access tokens are limited to four hours by default and held in browser sessionStorage (tab-scoped).

The authenticated /api/v1/citizen/account/password endpoint verifies the current password, requires a new strong password, atomically increments token_version and hashes the new password. Old bearer tokens stop working as soon as the DB transaction commits. The UI signs out after success.

## Limitations
sessionStorage reduces persistence but still permits token theft if the application's JavaScript is compromised. A carefully designed HttpOnly/Secure/SameSite cookie session with explicit CSRF protections remains a future requirement. Per-IP throttling needs a trusted upstream proxy policy and a shared store once JanSetu scales past one API instance.

Missing capabilities before real citizen onboarding: verified email ownership, forgotten-password recovery, MFA for administrators, explicit data erasure and tested backup/restore procedures. Do not claim these exist.
