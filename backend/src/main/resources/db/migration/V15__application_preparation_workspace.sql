CREATE TABLE application_preparation_sessions (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES citizen_profiles(id),
    scheme_id UUID NOT NULL REFERENCES schemes(id),
    policy_version_id UUID REFERENCES policy_versions(id),
    status VARCHAR(40) NOT NULL,
    scheme_revision BIGINT NOT NULL,
    ready_at TIMESTAMP WITH TIME ZONE,
    citizen_reported_applied_at TIMESTAMP WITH TIME ZONE,
    revision BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_preparation_profile_scheme UNIQUE (profile_id, scheme_id)
);

CREATE TABLE application_preparation_tasks (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES application_preparation_sessions(id) ON DELETE CASCADE,
    task_key VARCHAR(180) NOT NULL,
    task_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    title VARCHAR(500) NOT NULL,
    guidance VARCHAR(1000),
    citizen_completable BOOLEAN NOT NULL,
    evidence_references JSONB NOT NULL DEFAULT '[]'::jsonb,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_preparation_task_key UNIQUE (session_id, task_key)
);

CREATE TABLE application_preparation_history (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES application_preparation_sessions(id) ON DELETE CASCADE,
    actor_user_id UUID NOT NULL REFERENCES users(id),
    event_type VARCHAR(40) NOT NULL,
    task_key VARCHAR(180),
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_preparation_sessions_profile_updated
    ON application_preparation_sessions (profile_id, updated_at DESC);
CREATE INDEX idx_preparation_tasks_session_status
    ON application_preparation_tasks (session_id, status);
CREATE INDEX idx_preparation_history_session_recorded
    ON application_preparation_history (session_id, recorded_at DESC);

CREATE TRIGGER application_preparation_history_append_only
    BEFORE UPDATE OR DELETE ON application_preparation_history
    FOR EACH ROW EXECUTE FUNCTION prevent_administrative_audit_mutation();
