ALTER TABLE policy_agent_runs
    ADD COLUMN max_model_calls INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN model_call_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN max_estimated_tokens INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN estimated_tokens_used INTEGER NOT NULL DEFAULT 0;

ALTER TABLE policy_agent_runs
    ADD CONSTRAINT ck_policy_agent_model_call_budget
        CHECK (max_model_calls BETWEEN 0 AND 20 AND model_call_count BETWEEN 0 AND max_model_calls),
    ADD CONSTRAINT ck_policy_agent_token_budget
        CHECK (max_estimated_tokens BETWEEN 0 AND 50000 AND estimated_tokens_used BETWEEN 0 AND max_estimated_tokens);
