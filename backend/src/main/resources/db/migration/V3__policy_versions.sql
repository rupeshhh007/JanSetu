CREATE TABLE policy_versions (
    id UUID PRIMARY KEY,
    scheme_id UUID NOT NULL REFERENCES schemes(id),
    version_number INTEGER NOT NULL CHECK (version_number > 0),
    status VARCHAR(20) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    approved_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE,
    superseded_at TIMESTAMP WITH TIME ZONE,
    policy_rule JSONB NOT NULL,
    source_references JSONB NOT NULL DEFAULT '[]'::jsonb,
    revision BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_policy_versions_effective_dates
        CHECK (effective_to IS NULL OR effective_to >= effective_from),
    CONSTRAINT uq_policy_versions_scheme_version UNIQUE (scheme_id, version_number)
);

CREATE INDEX idx_policy_versions_scheme_history
    ON policy_versions (scheme_id, version_number DESC);

CREATE UNIQUE INDEX uq_policy_versions_one_active_publication
    ON policy_versions (scheme_id)
    WHERE status = 'PUBLISHED' AND effective_to IS NULL;
