CREATE TABLE policy_agent_runs (
    id UUID PRIMARY KEY,
    source_candidate_id UUID NOT NULL REFERENCES source_change_candidates(id),
    scheme_id UUID NOT NULL REFERENCES schemes(id),
    started_by_user_id UUID NOT NULL REFERENCES users(id),
    execution_mode VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    max_tool_calls INTEGER NOT NULL,
    tool_call_count INTEGER NOT NULL DEFAULT 0,
    result JSONB,
    failure_message VARCHAR(1000),
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    revision BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_policy_agent_run_submission UNIQUE (started_by_user_id, idempotency_key),
    CONSTRAINT ck_policy_agent_run_tool_budget CHECK (max_tool_calls BETWEEN 1 AND 20),
    CONSTRAINT ck_policy_agent_run_tool_count CHECK (tool_call_count BETWEEN 0 AND max_tool_calls)
);

CREATE INDEX idx_policy_agent_runs_candidate_history
    ON policy_agent_runs (source_candidate_id, created_at DESC);

CREATE INDEX idx_policy_agent_runs_status
    ON policy_agent_runs (status, updated_at);

CREATE TABLE policy_agent_execution_events (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES policy_agent_runs(id),
    sequence_number INTEGER NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    tool_name VARCHAR(80),
    outcome VARCHAR(30) NOT NULL,
    duration_ms BIGINT,
    evidence_references JSONB NOT NULL DEFAULT '[]'::jsonb,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_policy_agent_event_sequence UNIQUE (run_id, sequence_number),
    CONSTRAINT ck_policy_agent_event_duration CHECK (duration_ms IS NULL OR duration_ms >= 0)
);

CREATE INDEX idx_policy_agent_events_run_timeline
    ON policy_agent_execution_events (run_id, sequence_number);

CREATE FUNCTION prevent_policy_agent_event_mutation()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'policy agent execution events are append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER policy_agent_execution_events_append_only
    BEFORE UPDATE OR DELETE ON policy_agent_execution_events
    FOR EACH ROW EXECUTE FUNCTION prevent_policy_agent_event_mutation();
