# JanSetu engineering notes

## Trust boundaries

Citizen registration never selects a privileged role. Staff routes require ADMIN authority. Administrator bootstrap requires an explicit operator password; no default administrator is provisioned. Disabled users cannot log in or authenticate existing JWTs. Production operators must supply a strong JWT secret, HTTPS, restricted database credentials, and backup/retention policies.

## Eligibility and catalogue

Flyway owns schema evolution. Draft → published → archived preserves historical application references. Publication requires provenance. One evaluator checks publication, jurisdiction, age, income, category, occupation, and recorded boolean requirements. Missing information differs from failing a requirement. This is a recorded-rule assessment, not an official eligibility decision. Catalogue sources must be periodically reviewed by an operator.

## Tracking and consistency

Submission, status updates, and audit history are transactional. A PostgreSQL partial unique index protects against concurrent active duplicates; terminal rejected/withdrawn records permit a fresh submission. Optimistic versions protect staff updates. Batched history reads and entity graphs avoid per-record lookups and lazy-loading failures after transaction boundaries. Staff transitions require remarks and preserve actor/timestamp evidence.

## Guide and privacy

Default chat is deterministic and grounded in published catalogue records. Whole-word retrieval prioritizes named schemes, and a bounded list of at most five context IDs supports follow-ups. Eligibility answers reuse server-side evaluation. External provider use is opt-in and has not been live-tested. Query caches clear at authentication boundaries. Bookmarks and document checklist names remain on this device, scoped by account; document files are never uploaded.

## Verification record

Backend unit/integration tests exercise eligibility, retrieval, grounded answers, profileless access, role boundaries, duplicate/archive/jurisdiction submission rejection, real transaction-boundary reads, and administrator audit transitions. Live PostgreSQL/API smoke checks cover sign-in, eight published records, eligibility, tracking/audit reads, contextual chat, recommendations, and eight frontend HTTP routes. Frontend validation uses ESLint, TypeScript, and a production build.

Desktop interaction checks covered sign-in, profile creation, discovery, saving, and document preparation. On 11 October 2026, a follow-up documentation pass captured real production screenshots at default desktop and narrow tab viewports, and exercised navigation, filtered search, and a grounded guide answer. Browser console checks reported no errors or warnings. Full mobile submission and administrator interaction coverage remains incomplete. Administrator workflow is tested with isolated test authentication, not a privileged live demo account. Government submission integrations and live external model calls are outside this implementation.

## Visual fidelity ledger

The implementation follows the forest sidebar, editorial headings, sage guidance banner, count strip, and two-column citizen workspace of the generated concept. Real profile/application states replace illustrative counts. The sidebar is narrower to preserve working space; a landmark icon replaces the concept's bridge mark. The follow-up screenshots show the desktop layout and mobile scheme/guide views. These are documentation evidence, not a claim of exhaustive visual regression coverage.
