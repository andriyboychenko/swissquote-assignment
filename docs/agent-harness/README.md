# Agent Harness

This harness is the handoff point for future agents working on this project.
Read it before changing code, infrastructure, or tests.

## Purpose

- Preserve project decisions that are easy to lose between agent sessions.
- Give future agents a reliable verification path.
- Keep frontend, backend, database, gateway, and load balancer changes aligned.
- Make it obvious when this harness must be updated as the project evolves.

## Read First

1. `AGENTS.md`
2. `README.md`
3. `docker-compose.yml`
4. `backend/src/main/resources/application.yml`
5. `backend/src/main/resources/db/changelog/db.changelog-master.sql`
6. `frontend/package.json`
7. `frontend/vite.config.js`

## Current Architecture

- `load-balancer`: public Nginx entrypoint on `localhost:3000`.
- `frontend`: React/Vite app served by Nginx.
- `api-gateway`: internal Nginx gateway for backend traffic.
- `backend`: Spring Boot application built with Gradle.
- `postgres`: PostgreSQL database with Liquibase-managed schema.
- Google OAuth login starts at `/oauth2/authorization/google` and returns through `/login/oauth2/code/google`.
- Liquibase seeds a compact demo dataset: 100 customers with 100 activities each, for 10,000 total card/payment/crypto activities and 12 demo risk rules. Demo activity types and statuses use deterministic per-customer variation.
- Legacy quote-demo database objects such as `market_quote` are not part of the current domain and are removed through Liquibase cleanup changesets.

Traffic flow:

```text
Browser
  -> load-balancer
    -> frontend for /
    -> api-gateway for /api/ and /actuator/
      -> backend
        -> postgres
```

## Secrets

- Do not commit real passwords or local secrets.
- `gradle.properties` is intentionally ignored by Git.
- Use `gradle.properties.example` as the safe template.
- Docker Compose must keep using `${DB_PASSWORD:?Set DB_PASSWORD in gradle.properties}` so startup fails when the password is missing.
- Google OAuth credentials must remain in local ignored config or deployment secrets.

## Auth Routes

- `/oauth2/authorization/google`: starts Google OAuth login.
- `/login/oauth2/code/google`: Google OAuth callback handled by Spring Security.
- `/logout`: clears the Spring Security session.
- `/api/auth/me`: returns the current operator session; anonymous users receive `authenticated=false`.
- The load balancer and API gateway must route `/oauth2/`, `/login/`, and `/logout` to the backend.
- Authenticated operators are persisted in `operator_users` with only provider name, a hashed provider subject, blocked status, block reason, and timestamps. Do not persist operator names, emails, or other personal profile data.

## Customer Activity Routes

- `GET /api/customers/{customerId}/activities?limit={limit}&offset={offset}`: authenticated operator endpoint for searching a customer by UUID and reviewing paged card, payment, and crypto activity. The controller defaults to 50 rows and the service clamps pages to 100 rows.
- Customer activity supports server-side filter/sort query parameters for created date range, activity type, status, amount range, currency, counterparty, channel, detail, sort field, and sort direction.
- Card activity details include masked PAN, MCC, and the decline reason when one is present; render decline reason on a separate line in the same Details cell, while keeping the same detail text searchable through the `detail` filter.
- `GET /api/customers?query={prefix}&limit={limit}`: authenticated autocomplete endpoint for bounded Customer ID suggestions.
- Customer activity responses must be DTO records with summary counts and activity rows. Do not return raw persistence entities.
- Customer activity queries should stay read-only and projection-oriented.
- Customer search is a separate application capability under `application.customer`, exposed through `CustomerSearchController`. Keep it replaceable so a future implementation can move from PostgreSQL-backed prefix search to a dedicated search service without changing the activity report workflow.

## Backend Rules

- Use Java 17+ and Spring Boot 3 conventions.
- Use Gradle, not Maven.
- Keep only the Spring Boot entrypoint in the root package.
- Organize implementation under `domain`, `application`, `infrastructure`, `interfaces`, and `config`.
- Name application-layer persistence contracts with a `Repository` suffix.
- Use constructor injection.
- Use Java records for API request/response DTOs.
- Do not expose JPA entities directly from REST APIs.
- Keep JPA table-mapped classes under `infrastructure.persistence.entity`.
- Use simple Spring Data repositories directly for straightforward table access such as customer existence/autocomplete.
- Keep `JdbcCustomerActivityQueryAdapter` focused on the complex joined activity report projection across transaction, card, payment, crypto, and summary data.
- Manage database schema with Liquibase formatted SQL.
- Keep demo data compact and generated through SQL ranges or scripts instead of committing large insert dumps.
- Use `TIMESTAMPTZ` for timestamps and `NUMERIC`/`DECIMAL` for money.
- Add indexes for all foreign key columns.
- Add or update unit tests for behavior changes.

## Frontend Rules

- Keep React components in separate files when they become reusable or non-trivial.
- Keep components small and focused.
- Prefer explicit props over open-ended `...props` or `...params`.
- Avoid inline component declarations inside parent renders.
- Avoid sequential awaits for independent async work.
- Prefer explicit ternaries over `condition && value`.
- Lazy-load non-critical heavy modules or route views.
- Add or update tests for UI behavior changes.
- Keep landing-page Privacy & Data and Terms & Conditions modal copy aligned with changes to authentication, persisted data, customer activity handling, AI analysis, and access-control behavior.
- Authenticated operator screens should use dense dashboard layouts with clear search, loading, error, empty, summary, and table states.
- Non-critical authenticated notices should stay compact and support persisted minimize/dismiss behavior so they do not block the operator workflow.
- ID-heavy search fields should use backend-backed autocomplete with bounded limits, keyboard arrow/enter selection, and explicit loading/no-result states.
- Large activity tables should page lazily, load 50 rows by default, and load more rows on scroll instead of rendering the full dataset at once.
- Keep currency visible in the amount text, but do not duplicate it as a separate activity table column; currency must remain available as a filter and sort field while supported by the API.
- Table filters should match the data type: date/time ranges for timestamps, selects for known enums/status-like fields, numeric ranges for amounts, and text inputs for free-text fields.
- Dense filter panels should be minimized by default while keeping a visible title/control to restore them.
- Financial amount columns should make movement direction scannable with colored icons and amount color. Use a small green up arrow for incoming movement, a small red down arrow for outgoing movement, and a question-mark icon with an app-rendered tooltip for pending or no-movement states.
- Compact ellipsized table values, such as counterparty, should use app-rendered hover/focus tooltips instead of relying only on native `title` behavior.

## AI Feature Rules

- Keep the application in control of AI workflows.
- Decouple business logic from model providers.
- Separate system instructions from user input and delimit untrusted input.
- Version and test prompts.
- Prefer structured model outputs mapped to records or typed DTOs.
- Validate model output before persistence, business decisions, or user-facing use.
- Expose tool calls as controlled application capabilities with argument validation.
- Record safe audit events for AI calls, tool calls, retrieval, latency, token usage, and human-review outcomes.
- Use RAG only with deliberate chunking, metadata, freshness, authorization, evidence tracking, and source attribution.
- Add timeouts, token budgets, bounded retries, fallback behavior, and retention rules.
- Define agent state, memory, tools, guardrails, and termination criteria explicitly.
- Require human review for high-impact decisions or irreversible actions.

## Git Handoff Commands

After every completed change, include copy/paste-ready commands in the final response:

```bash
git add <changed-files>
git commit -m "<concise message>"
git push
```

Use file-scoped `git add` commands when possible. Do not execute these commands unless the user explicitly asks.

## Verification Commands

Run the relevant checks before handing work back.

```bash
./scripts/agent-harness.sh
```

The script runs:

```bash
./gradlew :backend:test
cd frontend && npm test
docker compose --env-file gradle.properties config --quiet
```

Optional runtime verification after containers are started:

```bash
docker compose --env-file gradle.properties up --build
curl -s http://localhost:3000/health
curl -s http://localhost:3000/actuator/health
```

Expected health responses:

```json
{"status":"UP","service":"load-balancer"}
```

```json
{"status":"UP","groups":["liveness","readiness"]}
```

## Update Policy

Update this harness whenever any of these change:

- Service names, ports, routing, gateway, or load balancer behavior.
- Startup command or required environment variables.
- Database migration location or schema management approach.
- Authenticated operator persistence, blocked-user behavior, privacy/data copy, or terms copy.
- Test commands, build tools, or package managers.
- Project structure or package organization.
- Frontend framework, routing, data fetching, or deployment approach.
- AI provider, prompt management, tool calling, RAG, observability, safety, retention, or human-review behavior.
- Git handoff command policy.

When updating project behavior, update these files together when relevant:

- `docs/agent-harness/README.md`
- `AGENTS.md`
- `README.md`
- `scripts/agent-harness.sh`
- Tests that validate the changed behavior
