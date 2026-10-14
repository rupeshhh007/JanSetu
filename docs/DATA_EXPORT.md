# Citizen self-service data export (v1)

Signed-in citizens can download their account's current JanSetu records from GET /api/v1/citizen/data-export. The response is JSON, attachment-disposition, and Cache-Control: no-store.

This export includes: citizen profile (if created), recorded self-declared or verified profile facts including consent state and provenance, saved scheme identifiers, locally reported application history, preparation tasks, stored rule assessments, and policy notifications. Password hashes, JWT keys, database internals and other users' records are not included.

The endpoint derives account identity from Spring Security. It does not accept a citizen/user ID in the request. A user without a profile receives null profile and empty profile-dependent collections, not an error.

This is a first step toward data portability, **not** completion of privacy compliance. Before real citizen data is accepted, JanSetu also needs clear retention periods, a documented erasure/request workflow, private incident reporting, data controller contact, recovery/backup policies and verified multi-account access-control testing. Export files themselves contain sensitive personal data and should be stored securely by the citizen.
