-- A citizen-controlled shortlist, not a declaration of eligibility or an application.
CREATE TABLE citizen_saved_schemes (
    id UUID PRIMARY KEY,
    citizen_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    scheme_id UUID NOT NULL REFERENCES schemes(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_citizen_saved_scheme UNIQUE (citizen_id, scheme_id)
);
CREATE INDEX idx_citizen_saved_schemes_recent
    ON citizen_saved_schemes (citizen_id, created_at DESC);
