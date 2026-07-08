# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 自定义指令
- 始终使用中文进行回复，包括解释、建议和错误信息。
- 代码注释可以使用英文，但对话部分必须用中文。

## Project Overview

智能化技术论坛 (Intelligent Technology Forum) — a full-stack forum application with AI-powered features: content moderation, text polishing, and an @mention AI assistant. Graduation project.

## Repository Layout

```
D:/BYSJ/
  backend/ITForum/      # Spring Boot 4.0.6 (Java 17)
  frontend/it-forum/    # Vue 3 + Vite (JavaScript, not TypeScript)
```

## Commands

### Backend (D:/BYSJ/backend/ITForum)

```bash
./mvnw compile              # Compile only
./mvnw spring-boot:run      # Run dev server (port 8080)
./mvnw test                 # Run tests
```

The backend binds to `localhost:8080`. Profile `dev` is active by default.

### Frontend (D:/BYSJ/frontend/it-forum)

```bash
npm run dev      # Vite dev server (port 5173)
npm run build    # Production build
npm run preview  # Preview production build
```

### Prerequisites

MySQL (3306), Redis (6379), and RabbitMQ (5672) must be running locally. The JDBC URL includes `createDatabaseIfNotExist=true` — no manual database creation needed.

## Architecture

### Backend Layering

```
controller/  → @RestController, @RequestMapping("/api/v1/...")
service/     → Interfaces in service/, implementations in service/impl/
mapper/      → MyBatis annotation-based mappers (NO XML — all SQL inline via @Select/@Insert/@Update/@Delete)
entity/      → POJOs with getters/setters
dto/         → request/ and response/ DTOs
```

All controllers return `Result<T>` wrapper: `{ code: 200, message: "success", data: T }`. The `GlobalExceptionHandler` maps domain exceptions to HTTP status codes (401/403/404/400/500).

### ID Generation

`SnowflakeIdGenerator` creates all entity IDs as `long`. IDs are set in service code (`idGenerator.nextId()`) before mapper inserts — no auto-increment. This means all entity tables define `id BIGINT UNSIGNED PRIMARY KEY` without AUTO_INCREMENT.

### Authentication

Stateless JWT authentication via Spring Security:
- `JwtAuthenticationFilter` (OncePerRequestFilter) extracts Bearer token, validates with `JwtTokenProvider`, sets SecurityContext
- `@CurrentUser` annotation + `CurrentUserMethodArgumentResolver` injects userId into controller parameters
- BCrypt for password hashing
- GET endpoints are mostly public; POST/PUT/DELETE require authentication; `/api/v1/admin/**` requires ADMIN role
- CSRF disabled, session management set to STATELESS

### AI Integration (LangChain4j 1.0.0-beta2)

Single `ChatLanguageModel` bean (`OpenAiChatModel`) configured in `AiConfig.java`. All AI agents call `model.chat(prompt)` — the API is `String chat(String)`, NOT `generate()`.

Three AI agents (`ai/` package):
- **ModeratorAgent** — evaluates content and returns JSON `{result, reason, confidence}`. Triggers via RabbitMQ (producer → queue → consumer → `AiModerationService.moderate()`)
- **PolishAgent** — returns polished markdown directly. Called synchronously from `POST /api/v1/ai/polish`
- **ForumAiAgent** — responds as "IT小助手" persona. Triggered when comment content contains `@AI`, `@小助手`, or `@助手`

Key gotcha: `@Async` only works when called from **another bean** (Spring proxy). Do not annotate self-invoked private methods.

### Moderation Flow

```
Post/comment created
  → ModerationProducer.sendModerationTask()  (synchronous, fire-and-forget)
  → RabbitMQ (moderation.exchange → moderation.queue)
  → ModerationConsumer.handleModerationTask()
  → AiModerationService.moderate()
  → AI evaluation → update post/comment status (APPROVED/FLAGGED/REJECTED)
```

### Database Schema

Managed by `DatabaseInitializer` (`@PostConstruct` in `config/`), NOT by Spring Boot's `spring.sql.init`. The initializer reads `schema.sql` from classpath, splits on `;`, and executes each statement. Errors (e.g., table already exists) are silently ignored. Spring's `spring.sql.init.mode` is set to `never`.

### Frontend Patterns

- **API layer**: `src/api/request.js` creates an Axios instance with baseURL `http://localhost:8080/api/v1`. Response interceptor strips the `Result` wrapper — callers receive `data.data` directly. JWT attached via request interceptor from localStorage.
- **State**: Pinia store in `stores/auth.js` — token/user persisted to localStorage. `isLoggedIn` and `userId` are computed properties.
- **Router**: Lazy-loaded routes with `meta.requiresAuth` and `meta.guest` guards in `router/index.js`. MainLayout wraps all non-auth pages with header + sidebar + content area.
- **Icons**: Element Plus icons (`@element-plus/icons-vue`) must be explicitly imported in each component that uses them.
- **Comments**: `CommentItem.vue` renders recursive nested comments. AI bot user is ID 1 — shown with blue "AI" badge.

## Key Configuration

- `application.yml` — base config with `dev` profile active
- `application-dev.yml` — dev overrides (datasource, logging levels). **Must also update JDBC URL here** when changing charset settings.
- `schema.sql` — DDL + seed data (AI bot user id=1, 6 default categories)

## Important Gotchas

1. **JDBC charset**: MySQL Connector/J 9.x rejects `characterEncoding=utf8mb4`. Use `characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci`.
2. **Two config files**: Both `application.yml` and `application-dev.yml` have datasource URLs. Changes must be applied to both.
3. **No MyBatis XML**: All SQL is inline in mapper interfaces. Dynamic SQL uses `<script>` tags in `@Select`/`@Update` annotations.
4. **Snowflake IDs**: Manual ID assignment in service layer. No database auto-increment anywhere.
5. **@Async boundary**: Spring AOP limitation — `@Async` on a method called via `this.` within the same class will NOT run asynchronously. Place `@Async` on a method in a separate bean.
6. **LangChain4j API**: The `ChatLanguageModel` interface uses `chat(String userMessage)` method, not `generate()`. Version 1.0.0-beta2.
