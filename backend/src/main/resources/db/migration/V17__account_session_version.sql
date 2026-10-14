-- Revoke previously issued JWTs when credentials change; no plaintext tokens are stored.
ALTER TABLE users ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;
