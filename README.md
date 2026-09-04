# Swissquote Full-Stack Docker Example

This project runs a Spring Boot backend, React UI, and PostgreSQL database in separate Docker containers.

## Architecture

- `frontend`: React app built with Vite and served by Nginx
- `load-balancer`: public Nginx entrypoint on port `3000`
- `api-gateway`: internal Nginx gateway for backend API traffic
- `backend`: Spring Boot REST API built with Gradle, using Spring Web, JPA, and Actuator
- `postgres`: PostgreSQL database with persistent Docker volume storage

The frontend is currently a static service overview. API calls can be added later under `frontend/src` when backend endpoints are defined. Browser traffic enters through the load balancer. Requests under `/api/` and `/actuator/` are forwarded to the API gateway, which forwards them to the backend.

Database schema changes are managed by Liquibase using formatted SQL changelogs under `backend/src/main/resources/db/changelog`.

The initial database includes compact Liquibase demo data: 100 customers with 100 activities each, for 10,000 total customer activities split across card, payment, and crypto records. The seed data is generated with SQL ranges instead of a large committed data dump.

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
- Google login start path: <http://localhost:3000/oauth2/authorization/google>
- PostgreSQL inside Docker network: `postgres:5432`

## Google Login Setup

Google login is implemented with Spring Security OAuth2 Login, following the same pattern as the `home-inventory` project.

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

The frontend Google button redirects to `/oauth2/authorization/google`. The load balancer and API gateway forward `/oauth2/`, `/login/`, and `/logout` to the backend. The backend exposes the current operator session at `/api/auth/me`.

Authenticated operators are written to the database in `operator_users`. The table intentionally avoids personal profile data: it stores only the OAuth provider, a SHA-256 hash of the provider subject, blocked status, optional block reason, and timestamps. The frontend can still display the current session name/email returned by Google, but those values are not persisted by this application.

Important: do not commit real OAuth client secrets. Keep them in local ignored files for demos and in deployment secrets for real environments.

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
