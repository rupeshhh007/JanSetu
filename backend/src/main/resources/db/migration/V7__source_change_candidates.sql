CREATE TABLE source_change_candidates (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL REFERENCES official_sources(id),
    previous_snapshot_id UUID NOT NULL REFERENCES source_snapshots(id),
    new_snapshot_id UUID NOT NULL UNIQUE REFERENCES source_snapshots(id),
    status VARCHAR(30) NOT NULL,
    change_summary TEXT NOT NULL,
    affected_scheme_id UUID REFERENCES schemes(id),
    review_note TEXT,
    reviewed_by_user_id UUID REFERENCES users(id),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    revision BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_source_change_candidates_review_queue
    ON source_change_candidates (status, created_at DESC);

CREATE INDEX idx_source_change_candidates_source_history
    ON source_change_candidates (source_id, created_at DESC);
