# Database Design

## Principles

- Normalize frequently queried data
- Store dynamic attributes in JSONB
- Prevent duplicate citizen identities
- Use lookup tables for static reference data

---

## States

| Column | Type | Notes |
|--------|------|------|
| id | SERIAL | PK |
| name | VARCHAR(100) | Unique |
| code | VARCHAR(10) | KA, MH |

---

## Districts

| Column | Type | Notes |
|--------|------|------|
| id | SERIAL | PK |
| state_id | FK | References states |
| name | VARCHAR(100) | |

---

## Citizen Profile

...