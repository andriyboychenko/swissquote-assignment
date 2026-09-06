# Swissquote Full-Stack Docker Example

This project runs a Spring Boot backend, React UI, and PostgreSQL database in separate Docker containers.

## Architecture

- `frontend`: React app built with Vite and served by Nginx
- `load-balancer`: public Nginx entrypoint on port `3000`
- `api-gateway`: internal Nginx gateway for backend API traffic
- `backend`: Spring Boot REST API built with Gradle, using Spring Web, JPA, and Actuator
- `postgres`: PostgreSQL database with persistent Docker volume storage

Browser traffic enters through the load balancer. Requests under `/api/`, `/actuator/`, `/oauth2/`, `/login/`, `/mock-login`, and `/logout` are forwarded to the API gateway, which forwards them to the backend. After login, the frontend shows the operator dashboard for customer lookup, activity review, filtering, sorting, lazy-loaded rows, and AI risk analysis requests.

Database schema changes are managed by Liquibase using formatted SQL changelogs under `backend/src/main/resources/db/changelog`.

Database entity classes are kept under `backend/src/main/java/com/example/swissquote/infrastructure/persistence/entity`. Current JPA entities are `CustomerEntity` for simple customer lookup/autocomplete and `OperatorUserEntity` for authenticated operator persistence. Customer search is isolated behind `CustomerSearchService` and `CustomerSearchRepository`, backed directly by `JpaCustomerRepository` today so it can be replaced by another search implementation later. The joined customer activity report still uses SQL projections through `JdbcCustomerActivityQueryAdapter`, so `transactions`, `card_activity`, `payment_activity`, `crypto_activity`, `risk_rules`, and `risk_assessments` do not have JPA entity classes yet.

The initial database includes compact Liquibase demo data: 100 customers with 100 activities each, for 10,000 total customer activities split across card, payment, and crypto records. The seed data is generated with SQL ranges instead of a large committed data dump, and it uses deterministic variation so customers do not all share the same activity/status pattern.

## Requirements

- Docker
- Docker Compose

## Start Everything

Create your local `gradle.properties` file first:

```bash
cp gradle.properties.example gradle.properties
```

For this demo, `gradle.properties` can contain:

**Important**: do not commit real database passwords. Keep passwords out of Git and store them in local ignored files for demos, or in a secret manager/deployment platform secrets for shared environments.
```properties
DB_NAME=swissquote
DB_USER=swissquote
DB_PASSWORD=replace-with-local-demo-password
```

From the project root, run:

```bash
docker compose --env-file gradle.properties up --build
```

Then open:

- App through load balancer: <http://localhost:3000>
- Load balancer health: <http://localhost:3000/health>
- Backend health through gateway path: <http://localhost:3000/actuator/health>
- Google login start path, only when local Google OAuth credentials are configured: <http://localhost:3000/oauth2/authorization/google>
- Mock demo login path: <http://localhost:3000/mock-login?operator=analyst-one>
- PostgreSQL inside Docker network: `postgres:5432`

After login, operators can search customer activity by Customer ID. Suspicious rows are highlighted from persisted `transactions.risk_indicators` JSONB metadata that is generated from risk-rule assessments. The backend endpoint is:

```text
GET /api/customers/{customerId}/activities?limit=50&offset=0
```

Activity review supports server-side filtering and sorting through query parameters such as `createdFrom`, `createdTo`, `activityType`, `status`, `amountMin`, `amountMax`, `currency`, `counterparty`, `channel`, `detail`, `riskOnly`, `sortBy`, and `sortDirection`. AI recommendations can apply the flagged-activity filter from the UI or with `Alt+R`.

Operators can request and review persisted AI analysis for a customer through:

```text
POST /api/customers/{customerId}/ai-analyses
GET /api/customers/{customerId}/ai-analyses
```

The current implementation uses a local deterministic analyzer behind the `AiAnalysisGenerator` interface and a generated policy corpus under `backend/src/main/resources/policies`. It persists request status, result text, risk level, recommendations, model/prompt version, and retrieved policy evidence with concrete policy section references. This keeps the contract ready for a later Spring AI implementation with a real chat model, vector-store-backed RAG, token/latency audit events, and asynchronous worker execution.

Policy evidence can be opened from the UI. The backend resolves bundled policy sections through:

```text
GET /api/policies/{documentName}/sections/{sectionAnchor}
```

## Google Login Setup

Google login is implemented with Spring Security OAuth2 Login.

Google login is disabled automatically when `GOOGLE_OAUTH_CLIENT_SECRET` is not present. The secret is intentionally not provided in GitHub; for a company demo, configure it locally before the presentation and show the login flow from that local environment.

Create an OAuth 2.0 Web Client in Google Cloud Console:

- Application type: `Web application`
- Authorized JavaScript origin: `http://localhost:3000`
- Authorized redirect URI: `http://localhost:3000/login/oauth2/code/google`

Then add the credentials to your local `gradle.properties`:

```properties
GOOGLE_OAUTH_CLIENT_ID=your-google-client-id
GOOGLE_OAUTH_CLIENT_SECRET=your-google-client-secret
GOOGLE_OAUTH_REDIRECT_URI={baseUrl}/login/oauth2/code/{registrationId}
```

Restart the stack after changing credentials:

```bash
docker compose --env-file gradle.properties up --build
```

The frontend asks `/api/auth/me` whether Google login is enabled. If `GOOGLE_OAUTH_CLIENT_SECRET` is missing, the Google button is hidden and only demo operator login is shown. When enabled, the frontend Google button redirects to `/oauth2/authorization/google`; the demo operator button redirects to `/mock-login?operator=analyst-one`. The load balancer and API gateway forward `/oauth2/`, `/login/`, `/mock-login`, and `/logout` to the backend.

Authenticated operators are written to the database in `operator_users`. The table intentionally avoids personal profile data: it stores only the OAuth provider, a SHA-256 hash of the provider subject, blocked status, optional block reason, and timestamps. AI analysis requests also store the operator display name shown at request time so saved reviews can show who generated them. Operator email is not persisted by this application.

Important: do not commit real OAuth client secrets. Keep them in local ignored files for demos and in deployment secrets for real environments.

## Mock Demo Login

The Docker demo enables mock operator login by default with `MOCK_AUTH_ENABLED=true`. The landing page includes a `Demo operator` option that opens a chooser modal. Each selected operator starts a real backend session through:

```text
GET /mock-login?operator=analyst-one
```

Available mock operators:

- `analyst-one`: Sarah Connor
- `analyst-two`: Lisbeth Salander
- `risk-reviewer`: John McClane

The backend creates a normal Spring Security session and `/api/auth/me` records the login through the same `operator_users` persistence flow as OAuth login, using provider `mock` and a hashed mock subject. AI analysis records may store the mock operator display name for audit attribution. Set `MOCK_AUTH_ENABLED=false` for non-demo deployments.

## Database Connection

From another Docker container on the same Compose network, connect with:

```text
Host: postgres
Port: 5432
Database: swissquote
User: swissquote
Password: value from your local ignored gradle.properties
```

From your host machine, PostgreSQL is not published by default. To connect from a local DB client such as IntelliJ Database, DBeaver, or DataGrip, temporarily publish the PostgreSQL port in `docker-compose.yml`, for example `5432:5432`, then use:

```text
Host: localhost
Port: 5432
Database: swissquote
User: swissquote
Password: value from your local ignored gradle.properties
```

Important: do not publish database ports or commit demo passwords in production. Use private networking and secrets instead.

## Stop Everything

```bash
docker compose down
```

To also delete the PostgreSQL data volume:

```bash
docker compose down -v
```

If you change `DB_PASSWORD` after PostgreSQL has already created its Docker volume, recreate the database volume with `docker compose down -v` before starting again.

## Demo Data

Liquibase creates demo data on a fresh database:

```text
Customers: 100
Activities per customer: 100
Total activities: 10,000
Activity types: card, payment, crypto
Risk rules: 12 demo rules
Risk assessments: deterministic subset for risky-looking activity
Pagination: activity review returns 50 rows by default, caps requests at 100 rows, and loads additional rows on scroll
AI analysis: requests, results, and retrieved policy evidence are persisted once an operator asks for analysis
Policy corpus: generated Markdown policy files live under backend resources and are cited by analysis evidence
Suspicious rows: activity rows include JSONB-backed risk indicators generated from risk assessments
First five demo IDs: curated as 2 low risk, 2 medium risk, and 1 high risk customer for predictable demos
Known low-risk customers: `005514e6-1ebe-8010-de91-aff66d1d9484`, `0a3ab26d-12b1-0efc-65d4-a2d6cc72ec67`
```

To fetch one demo customer ID from the running database:

```bash
docker compose --env-file gradle.properties exec -T postgres psql -U swissquote -d swissquote -c "SELECT customer_id FROM customers ORDER BY customer_id LIMIT 5;"
```

To recreate the demo data from scratch, delete the PostgreSQL volume and start again:

```bash
docker compose --env-file gradle.properties down -v
docker compose --env-file gradle.properties up --build
```

## Deploy Frontend To Vercel

The frontend is Vercel-ready from the `frontend` directory.

In Vercel project settings:

- Root Directory: `frontend`
- Framework Preset: Vite
- Build Command: `npm run build`
- Output Directory: `dist`

Add `VITE_`-prefixed environment variables only when the browser needs non-secret runtime configuration.

## Scaling Notes

Keeping frontend, backend, and database in separate containers is the correct approach for growth. Each service can be built, restarted, logged, and scaled independently.

For local experiments, you can scale internal services with:

```bash
docker compose --env-file gradle.properties up --build --scale backend=3 --scale frontend=2
```

For real production scaling, use a load balancer or orchestrator such as Kubernetes, ECS, or Cloud Run, and strongly consider managed PostgreSQL instead of running the database on the same host.
