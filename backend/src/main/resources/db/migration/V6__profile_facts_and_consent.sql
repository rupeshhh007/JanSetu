-- Profile facts are retained separately from the legacy citizen profile so
-- collection can be incremental and every reusable value carries its origin,
-- verification state, consent scope, and optional expiry.
CREATE TABLE citizen_profile_facts (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES citizen_profiles(id),
    fact_key VARCHAR(64) NOT NULL,
    value_text TEXT NOT NULL,
    provenance VARCHAR(32) NOT NULL,
    verification_status VARCHAR(32) NOT NULL,
    consent_scope VARCHAR(32) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revision BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_citizen_profile_facts_profile_key UNIQUE (profile_id, fact_key)
);

CREATE INDEX idx_citizen_profile_facts_profile_active
    ON citizen_profile_facts (profile_id, expires_at);

CREATE INDEX idx_citizen_profile_facts_caseworker_reuse
    ON citizen_profile_facts (profile_id, consent_scope, expires_at);
