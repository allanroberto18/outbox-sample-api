# Outbox Sample API (Quarkus + Java 21)

API built with Quarkus to register and activate users using the outbox pattern for reliable email dispatch.

## Features

- Java 21 + Quarkus 3
- OpenAPI (`/q/openapi`) and Swagger UI (`/q/swagger-ui`)
- Health checks (`/q/health`)
- JPA/Hibernate ORM with PostgreSQL
- Flyway migrations for schema creation
- User registration and activation endpoints
- Outbox pattern for post-commit activation email dispatch
- Password hashing with bcrypt before persistence
- Test coverage for API + outbox success/failure flows

## Requirements

- Docker and Docker Compose
- Java 21

## Environment variables

The project includes a `.env` file with defaults:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_FROM`
- `MAIL_MOCK`
- `APP_BASE_URL`
- `OUTBOX_DISPATCH_EVERY`
- `OUTBOX_DISPATCH_BATCH_SIZE`
- `OUTBOX_DISPATCH_MAX_ATTEMPTS`
- `OUTBOX_DISPATCH_RETRY_DELAY_SECONDS`

## Run dependencies (Postgres + MailHog)

```bash
docker compose up -d
```

Services:

- PostgreSQL: `localhost:5432`
- MailHog SMTP: `localhost:1025`
- MailHog UI: `http://localhost:8025`

## Run application

```bash
./mvnw quarkus:dev
```

Quarkus will run Flyway migrations automatically at startup.

## API Endpoints

### Register user

`POST /users`

Request body:

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "username": "john.doe@mail.test",
  "password": "123456"
}
```

Rules:

- `firstName`, `lastName`, `username`, `password` are mandatory
- `enabled` starts as `false`
- `password` is stored hashed (bcrypt)
- duplicate `username` returns `409`
- after commit, an outbox event is created and later dispatched as activation email

### Activate user

`GET /users/{id}/activate`

Rules:

- updates `enabled` from `false` to `true`

## Outbox behavior

- Registration stores user and outbox event in the same transaction
- Background dispatcher processes `PENDING` events every `OUTBOX_DISPATCH_EVERY`
- Event status transitions:
  - `PENDING` -> `SENT` when email dispatch succeeds
  - `PENDING` -> `PENDING` with delayed retry while attempts remain
  - `PENDING` -> `FAILED` when max retry attempts is reached

## Test

```bash
./mvnw test
```

Tests run with H2 (PostgreSQL compatibility mode), Flyway migrations, and mocked mail sender support.
