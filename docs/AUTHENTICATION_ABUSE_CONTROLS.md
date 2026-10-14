# JanSetu public authentication abuse controls

## Implemented
Both login and registration requests pass a servlet-level per-remote-address limit (150 requests per 15 minutes) before parsing JSON. Valid requests are separately bounded by normalized, SHA-256-hashed email identity (12 login attempts or 4 registration attempts per 15 minutes). The server responds with HTTP 429, Retry-After: 900, a generic error and Cache-Control: no-store. Logs must not contain the raw authentication email or password.

The limiter is deliberately **single-instance** and does not trust arbitrary forwarded IP headers; users sharing a proxy IP may share the network budget. It provides basic abuse resistance, not distributed DDoS protection.

## Before scale
Replace in-memory counters with a trusted gateway or atomic shared Redis counters when deploying multiple instances, add proof-of-work/captcha after repeated attempts, monitor distributed spray patterns and review identity enumeration. Keep 429 errors generic and do not reveal registered addresses. The current limit is an engineering default requiring calibration with legitimate production traffic.
