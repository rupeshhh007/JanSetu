-- This migration is deliberately idempotent so a pre-Flyway JanSetu database
-- can be adopted using baseline-on-migrate without losing local records.

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS citizen_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(32) NOT NULL,
    state VARCHAR(255) NOT NULL,
    district VARCHAR(255) NOT NULL,
    pincode VARCHAR(6) NOT NULL,
    category VARCHAR(32) NOT NULL,
    annual_family_income NUMERIC(12,2) NOT NULL,
    marital_status VARCHAR(32) NOT NULL,
    primary_occupation VARCHAR(32) NOT NULL,
    is_student BOOLEAN NOT NULL,
    is_farmer BOOLEAN NOT NULL,
    has_disability BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS schemes (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    eligibility_criteria JSONB NOT NULL
);

ALTER TABLE schemes ADD COLUMN IF NOT EXISTS state VARCHAR(100) NOT NULL DEFAULT 'India';
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS department VARCHAR(160) NOT NULL DEFAULT 'Unspecified';
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS official_url VARCHAR(2048);
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS source_name VARCHAR(255);
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS last_verified_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS benefits JSONB NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS required_documents JSONB NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED';
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS published_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE schemes ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

UPDATE schemes SET published_at = COALESCE(published_at, NOW()) WHERE status = 'PUBLISHED';

CREATE INDEX IF NOT EXISTS idx_schemes_catalogue ON schemes (status, state, department);

CREATE TABLE IF NOT EXISTS applications (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES citizen_profiles(id),
    scheme_id UUID NOT NULL REFERENCES schemes(id),
    status VARCHAR(32) NOT NULL,
    remarks TEXT,
    submitted_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

ALTER TABLE applications ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_applications_profile_submitted ON applications (profile_id, submitted_at DESC);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications (status);

-- Only terminal rejected/withdrawn applications may be followed by a new one.
CREATE UNIQUE INDEX IF NOT EXISTS uq_active_application_per_scheme
    ON applications (profile_id, scheme_id)
    WHERE status NOT IN ('REJECTED', 'WITHDRAWN');

CREATE TABLE IF NOT EXISTS application_status_history (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id),
    previous_status VARCHAR(32),
    new_status VARCHAR(32) NOT NULL,
    remarks TEXT,
    changed_by_user_id UUID NOT NULL REFERENCES users(id),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_application_status_history_application
    ON application_status_history (application_id, changed_at DESC);
