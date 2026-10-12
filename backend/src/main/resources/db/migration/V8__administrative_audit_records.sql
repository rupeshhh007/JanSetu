CREATE TABLE administrative_audit_records (
    id UUID PRIMARY KEY,
    actor_user_id UUID NOT NULL REFERENCES users(id),
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID NOT NULL,
    decision_reason VARCHAR(1000),
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_administrative_audit_records_entity
    ON administrative_audit_records (entity_type, entity_id, recorded_at DESC);

CREATE INDEX idx_administrative_audit_records_recent
    ON administrative_audit_records (recorded_at DESC);

CREATE FUNCTION prevent_administrative_audit_mutation()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'administrative audit records are append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER administrative_audit_records_append_only
    BEFORE UPDATE OR DELETE ON administrative_audit_records
    FOR EACH ROW EXECUTE FUNCTION prevent_administrative_audit_mutation();
