# Synced saved schemes

JanSetu shortlists are now account-owned PostgreSQL data rather than untrusted browser-only state.

## REST contract
Every request requires a valid JanSetu bearer token.

- GET /api/v1/citizen/saved-schemes → list of saved scheme UUID strings, newest first; only the authenticated account.
- PUT /api/v1/citizen/saved-schemes/{schemeId} → 204; idempotent, only for published catalogue entries.
- DELETE /api/v1/citizen/saved-schemes/{schemeId} → 204 even if not saved; never deletes another citizen's bookmark.
- Missing, unpublished or archived schemes cannot be newly saved (404).

## Ownership and consistency
The server derives the citizen identifier from Spring Security and accepts no client-supplied user ID. Mutations take a row lock on that citizen before checking and updating saved state, preventing duplicate saves from concurrent requests by the same user. A unique (citizen_id, scheme_id) constraint provides a database backstop.

## Migration
Flyway V16 creates the table, keys and index, using ON DELETE CASCADE for user and scheme deletion. Existing browser bookmarks cannot be recovered for other devices automatically; any migration must be explicit, opt-in, account-bound and deduplicated. Deleting a local-only shortlist does not delete account data.
