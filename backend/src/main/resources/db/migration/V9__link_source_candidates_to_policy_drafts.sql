ALTER TABLE source_change_candidates
    ADD COLUMN proposed_policy_version_id UUID UNIQUE REFERENCES policy_versions(id),
    ADD COLUMN drafted_by_user_id UUID REFERENCES users(id),
    ADD COLUMN drafted_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_source_change_candidates_policy_draft
    ON source_change_candidates (proposed_policy_version_id)
    WHERE proposed_policy_version_id IS NOT NULL;
