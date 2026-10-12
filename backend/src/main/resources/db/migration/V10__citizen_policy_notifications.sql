CREATE TABLE citizen_policy_notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    scheme_id UUID NOT NULL REFERENCES schemes(id),
    policy_version_id UUID NOT NULL REFERENCES policy_versions(id),
    current_result VARCHAR(20) NOT NULL,
    previous_result VARCHAR(20),
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_citizen_policy_notification UNIQUE (user_id, policy_version_id)
);

CREATE INDEX idx_citizen_policy_notifications_inbox
    ON citizen_policy_notifications (user_id, read_at, created_at DESC);
