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

## Backend Rules

- Use Java 17+ and Spring Boot 3 conventions.
- Use Gradle, not Maven.
- Keep only the Spring Boot entrypoint in the root package.
- Organize implementation under `domain`, `application`, `infrastructure`, `interfaces`, and `config`.
- Use constructor injection.
- Use Java records for API request/response DTOs.
- Do not expose JPA entities directly from REST APIs.
- Manage database schema with Liquibase formatted SQL.
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
