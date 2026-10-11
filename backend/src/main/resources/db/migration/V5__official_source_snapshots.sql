CREATE TABLE official_sources (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    authority VARCHAR(160) NOT NULL,
    canonical_url VARCHAR(2048) NOT NULL UNIQUE,
    normalized_host VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_snapshot_at TIMESTAMP WITH TIME ZONE,
    revision BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_official_sources_active_host
    ON official_sources (status, normalized_host);

CREATE TABLE source_snapshots (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL REFERENCES official_sources(id),
    source_name_at_capture VARCHAR(160) NOT NULL,
    source_url_at_capture VARCHAR(2048) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    http_status INTEGER NOT NULL CHECK (http_status >= 200 AND http_status < 300),
    content_length BIGINT NOT NULL CHECK (content_length >= 0),
    content_hash VARCHAR(80) NOT NULL,
    content_excerpt TEXT NOT NULL,
    content_bytes BYTEA NOT NULL,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_source_snapshots_source_history
    ON source_snapshots (source_id, captured_at DESC);
