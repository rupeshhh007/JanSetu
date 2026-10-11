CREATE TABLE policy_assessments (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES citizen_profiles(id),
    policy_version_id UUID NOT NULL REFERENCES policy_versions(id),
    evaluation_date DATE NOT NULL,
    policy_result VARCHAR(20) NOT NULL,
    fact_snapshot JSONB NOT NULL,
    evaluation_trace JSONB NOT NULL,
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_policy_assessments_profile_history
    ON policy_assessments (profile_id, assessed_at DESC);

CREATE INDEX idx_policy_assessments_policy_version
    ON policy_assessments (policy_version_id, assessed_at DESC);
