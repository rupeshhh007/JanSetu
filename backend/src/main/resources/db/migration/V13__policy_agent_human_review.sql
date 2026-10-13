ALTER TABLE policy_agent_runs
    ADD COLUMN review_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_REVIEW',
    ADD COLUMN reviewed_by_user_id UUID REFERENCES users(id),
    ADD COLUMN review_reason VARCHAR(1000),
    ADD COLUMN reviewed_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_policy_agent_runs_review_queue
    ON policy_agent_runs (review_status, created_at DESC);
