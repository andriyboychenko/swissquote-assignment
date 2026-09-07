# Project Coding Rules

These rules apply to future coding work in this repository.

## Agent Harness

- Before making changes, read `docs/agent-harness/README.md`.
- Use `./scripts/agent-harness.sh` as the default full-project verification command before handing work back.
- Update `docs/agent-harness/README.md` whenever architecture, startup commands, ports, environment variables, package structure, migrations, or verification commands change.
- Keep `AGENTS.md`, `README.md`, and the harness aligned when the project evolves.

## Git Handoff Commands

- After every completed change, include copy/paste-ready `git add`, `git commit -m`, and `git push` commands in the final response.
- Scope `git add` to the files changed for the task instead of using broad staging when possible.
- Use a concise commit message that describes the user-visible change.
- Do not run `git add`, `git commit`, or `git push` unless the user explicitly asks you to execute them.

## React Frontend Rules

### Waterfall And Async Management

- `async-parallel`: Avoid sequential awaits on independent promises. Trigger independent async work in parallel with `Promise.all()`.
- `async-cheap-condition-before-await`: Evaluate fast synchronous conditionals before triggering expensive async network calls.
- `async-suspense-boundaries`: Use React Suspense boundaries for lazy or data-dependent UI so the shell can render before slower content resolves.

### Bundle Size And Code Splitting

- `bundle-barrel-imports`: Avoid root barrel imports when direct path imports are available and improve tree-shaking.
- `bundle-dynamic-imports`: Lazy-load non-critical heavy modules, dialogs, and route views with `React.lazy()`.

### Re-rendering And Rendering Optimization

- `rerender-derived-state-no-effect`: Calculate values from existing props/state during render instead of syncing derived state in `useEffect`.
- `rerender-move-effect-to-event`: Keep user-triggered logic inside event handlers instead of `useEffect` watchers.
- `rendering-conditional-render`: Prefer explicit ternaries such as `condition ? value : null` over `condition && value`.
- `rerender-no-inline-components`: Do not declare sub-components inside parent render functions.

### Component Structure And Props

- `component-file-per-component`: Keep reusable React components in separate files instead of grouping multiple component declarations in one file.
- `component-small-focused`: Keep components small and focused on one rendering responsibility. Extract child components when a component starts mixing unrelated UI concerns.
- `props-explicit-no-rest-bags`: Prefer explicit, named props. Avoid open-ended rest parameter bags such as `...params` or `...props` unless forwarding DOM attributes is an intentional, documented component contract.
- `legal-copy-kept-current`: Keep Privacy & Data and Terms & Conditions UI copy updated whenever authentication, persisted data, AI analysis, access control, or customer activity handling changes.
- `operator-dashboard-first`: For authenticated operator workflows, prioritize dense, scannable dashboard surfaces with clear search, loading, empty, error, summary, and tabular review states over marketing-style layouts.
- `nonblocking-notices`: Keep authenticated dashboard notices compact, dismissible or minimizable when appropriate, and persist user preference for non-critical repeated notices.
- `search-autocomplete`: For ID-heavy operator search, provide backend-backed autocomplete with minimum input length, bounded result limits, keyboard/mouse selection where practical, and clear no-result/loading states.
- `table-filter-types`: Match table filters to field types: date/time ranges for timestamps, selects for known finite values, numeric ranges for amounts, and text inputs for free-form text.
- `table-server-querying`: For lazily loaded tables, apply filtering and sorting server-side so every loaded page follows the same query.
- `table-collapsible-filters`: Make dense filter panels minimizable and keep a visible title/control to restore them.
- `financial-movement-indicators`: Use both sign and color for amount movement. Keep pending/provisional activity visually distinct from settled incoming/outgoing movement.

## Java 17+ And Spring Boot 3 Rules

### Language And Data Modeling

- Use Java records for immutable API request/response payloads and projection DTOs.
- Use Java 17 pattern matching for `instanceof` and modern switch expressions where they simplify legacy branching.
- Use sealed interfaces when a domain hierarchy should be closed and exhaustively handled.

### Spring Boot Performance And Architecture

- Enable virtual threads with `spring.threads.virtual.enabled=true` unless a specific workload proves otherwise.
- Use constructor injection with final fields for Spring components. Avoid field injection.
- Centralize REST errors with `@RestControllerAdvice` returning RFC 7807 `ProblemDetail`.
- Do not expose raw JPA entity graphs directly from REST APIs. Map entities to DTO records.
- Search/read endpoints should use read-only transactions and projection-style queries or DTO mapping so UI workflows do not accidentally expose persistence internals.

### Backend Package Organization

- `backend-package-by-layer`: Organize backend code under clear packages: `domain`, `application`, `infrastructure`, `interfaces`, and `config`.
- `backend-root-entrypoint-only`: Keep only the Spring Boot application entrypoint in the root backend package. Put business code in a focused subpackage.
- `backend-domain-independent`: Keep domain code independent from Spring MVC, Spring Data, database, and transport annotations unless a deliberate exception is documented.
- `backend-interfaces-adapters`: Put REST controllers and request/response transport DTOs under `interfaces.rest`.
- `backend-infrastructure-adapters`: Put persistence, external clients, and framework adapters under `infrastructure`.

### SOLID And Extension Design

- `solid-single-responsibility`: Keep classes focused on one reason to change. Split orchestration, mapping, persistence, and transport concerns.
- `solid-open-closed`: Prefer adding implementations behind interfaces or strategies over editing large conditional blocks.
- `solid-liskov`: Subtypes must preserve the behavior expected by their abstractions.
- `solid-interface-segregation`: Prefer small role-specific interfaces over wide service contracts.
- `solid-dependency-inversion`: Application services should depend on domain/application ports, not concrete infrastructure classes.
- `customer-search-extension`: Keep customer search as its own application capability, separate from activity review, so future search backends such as a dedicated index can be added behind the repository interface.
- `patterns-strategy-for-rules`: Use the Strategy pattern for extensible business rules, scoring, validations, and transaction activity handling.
- `patterns-factory-for-creation`: Use factories for non-trivial object creation when construction rules vary by type.
- `patterns-template-method-carefully`: Use Template Method only when shared algorithm steps are stable and variation points are clear.
- `patterns-avoid-premature-patterns`: Do not add design patterns without a real extension or reuse pressure.

## PostgreSQL And JPA/Hibernate Rules

### ORM And Query Efficiency

- Prevent N+1 queries with explicit `@EntityGraph`, `JOIN FETCH`, or projection DTOs when relationships are fetched.
- Mark read-only operations with `@Transactional(readOnly = true)`.
- Use `FetchType.LAZY` for all `@ManyToOne` and `@OneToOne` relationships.

### Database Schema And Query Design

- Add an index for every foreign key column.
- Store timestamps as `TIMESTAMPTZ` using `OffsetDateTime` or `Instant` in Java.
- Store money as `NUMERIC`/`DECIMAL` using `BigDecimal` in Java.
- Tune HikariCP maximum pool size deliberately using: pool size = `(core count * 2) + effective spindle count`.

## Spring AI And Agentic Feature Rules

### AI Integration Design

- `ai-application-in-control`: Treat AI as an integration dependency behind application-owned services, not as the owner of business workflow.
- `ai-provider-decoupling`: Keep business logic decoupled from model providers so providers can be swapped and failures can degrade gracefully.
- `ai-explicit-prompt-contracts`: Separate system instructions from user input, delimit untrusted input, and avoid ad hoc prompt string concatenation.
- `ai-prompt-versioning`: Version, review, and test prompts as application artifacts.
- `ai-structured-output`: Prefer structured model output mapped to Java records or typed DTOs over parsing free-form text.
- `ai-output-validation`: Validate model output before using it in business logic, persistence, or user-facing responses.

### Tool Calling And MCP

- `ai-controlled-tools`: Expose tools as controlled application capabilities, not as direct unrestricted infrastructure access.
- `ai-tool-argument-validation`: Validate tool arguments before execution.
- `ai-tool-audit-events`: Record tool name, arguments summary, latency, result size, and outcome without leaking secrets.
- `ai-mcp-for-shared-tools`: Prefer MCP when tools need dynamic discovery or must be shared across processes.

### Retrieval Augmented Generation

- `ai-rag-for-private-knowledge`: Use RAG when answers depend on internal documents, logs, code, runbooks, or domain knowledge.
- `ai-rag-intentional-retrieval`: Design chunking, metadata, freshness, authorization, and evidence tracking deliberately.
- `ai-rag-metadata-filters`: Use metadata filters when retrieving from vector stores.
- `ai-rag-auditability`: Track retrieved document ids, source files, relevance scores, and chunk references for auditability.

### Observability And Safety

- `ai-observability-events`: Capture request id, caller, model provider, model name, prompt version, latency, token usage, finish reason, tool calls, retrieval results, and human-review outcome.
- `ai-sensitive-data-minimization`: Do not log sensitive prompts, outputs, credentials, personal data, or raw tool payloads verbatim.
- `ai-timeouts-and-budgets`: Add strict timeouts and token budgets around model, tool, and vector-store calls.
- `ai-bounded-retries`: Retry AI, tool, and retrieval calls only when safe, bounded, and observable.
- `ai-fallback-states`: Provide clear fallback behavior when a model, tool, or vector store is unavailable.
- `ai-retention-rules`: Define retention rules for prompts, completions, embeddings, retrieval results, and tool outputs.

### Bounded Agent Workflows

- `ai-explicit-agent-boundaries`: Define agent state, memory, tools, guardrails, and termination criteria explicitly.
- `ai-risk-scoped-autonomy`: Keep agent autonomy proportional to action risk.
- `ai-human-review-gates`: Require human review before high-impact decisions, external side effects, or irreversible actions.
- `ai-low-risk-first`: Prefer low-risk actions such as summarizing, classifying, or drafting before actions that modify systems or user data.
## Service boundaries

- `backend` is the core service. It owns authentication, customers, transactions, activity filtering, and risk signals.
- `ai-service` is the internal AI service. It receives bounded snapshots from the core service and owns AI analysis persistence and policy evidence.
- `core-postgres` and `ai-postgres` are separate database nodes. Local Compose may reuse `DB_PASSWORD`, but service credentials should be separated in deployed environments.
- Keep the browser API contract under `/api` stable. The core service remains the authenticated facade; do not expose `/internal/ai-analyses` through the public gateway.
- Any future Spring AI provider or vector store belongs behind the AI service interfaces and must not make the core service depend on AI infrastructure.
