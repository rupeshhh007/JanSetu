# JanSetu security policy

## Reporting a vulnerability
Please **do not** file public GitHub issues containing exploit instructions, access tokens, personal data or private infrastructure details. Instead, use GitHub's **Report a vulnerability** option on the repository's Security tab if available, or contact the repository maintainer privately via their GitHub profile.

Include an impact summary, affected URL or commit, minimum reproduction steps using synthetic data, and suggested mitigations. Do not access real citizen accounts or attempt destructive testing.

## Boundaries
JanSetu is an independent scheme-discovery, decision-support and citizen preparation product. It does **not** submit applications to government portals, authenticate government records, or guarantee eligibility. Do not enter real identity documents into experimental deployments.

## Production guardrails
- Server-only secrets belong in Render environment variables; never in Git, browser bundles, issue comments or screenshots.
- Public sign-up always creates citizen accounts. Admin setup is operator-controlled.
- Keep the AI provider disabled until privacy, budget and policy review is complete.
- Deploy schema migrations through Flyway with a documented rollback and a database recovery plan.
- Rotate exposed credentials immediately: removing a file does not remove it from commit history.
- Scope tokens to minimum privileges; require CI and review for protected branches.
