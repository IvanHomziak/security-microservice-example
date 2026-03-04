# Security Microservice Example (Spring Boot 3 + Spring Security 6)

A demo security-focused microservice that showcases stateless JWT authentication, permission-based authorization, MySQL persistence, Flyway migrations, and Docker Compose setup.

## Project overview

This educational backend service demonstrates a complete baseline authentication and access-control flow:

- **Username/password authentication** for users.
- **JWT token issuance** (HS256) after successful login.
- **Permission checks** on both HTTP endpoint and service layers.
- **RBAC + granular permissions** model (roles with authority sets).
- **Database schema management with Flyway** using versioned SQL migrations.

### Main request flow

1. Client calls `POST /auth/login` with credentials.
2. Service validates credentials and returns a JWT access token.
3. Client sends the token via `Authorization: Bearer <token>`.
4. Service extracts authorities from the JWT and evaluates access rules.
5. Access to `/api/items/**` is granted/denied by permission (`DATA_READ`, `DATA_UPDATE`, `DATA_CREATE`, `DATA_DELETE`).

## Security model

- Each user can have a **set of roles** (`CUSTOMER`, `BUSINESS`, `ADMIN`).
- Each role stores a **set of authorities** (`DATA_READ`, `DATA_UPDATE`, `DATA_CREATE`, `DATA_DELETE`).
- Effective user access is calculated only from assigned roles and their authority sets.
- JWT contains aggregated authorities:
  - all `ROLE_*` values for assigned roles;
  - all authorities inherited from those roles.

## Tech stack

- Java 21
- Maven
- Spring Boot 3.x
- Spring Security 6 (Resource Server + JWT)
- H2 (default local profile)
- MySQL (for docker/production-like setup)
- Flyway

## Implemented features

- `POST /auth/login` — login by username/password, returns HS256 JWT.
- `GET /auth/me` — returns current username and authorities from token.
- Permission-based protection for `/api/items/**`.
- Service-level protection via `@PreAuthorize`.
- Flyway migrations:
  - schema creation (`app_user`, `role`, `role_authority`, `user_role`, `item`)
  - seed users/authorities/items.

## Database structure

### 1) `role`
User role dictionary.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Role ID |
| `name` | `VARCHAR(30)` | `UNIQUE`, `NOT NULL` | Role name (`CUSTOMER`, `BUSINESS`, `ADMIN`) |

### 2) `role_authority`
Many-to-many relation: role → authority enum value.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `role_id` | `BIGINT` | `PK`, `FK -> role(id)`, `NOT NULL` | Role |
| `authority` | `VARCHAR(50)` | `PK`, `NOT NULL` | Authority (`DATA_READ`, `DATA_UPDATE`, ...) |

Notes:
- Composite PK (`role_id`, `authority`) prevents duplicates.
- `ON DELETE CASCADE` keeps links in sync when roles are removed.

### 4) `app_user`
System users table.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | User ID |
| `username` | `VARCHAR(100)` | `UNIQUE`, `NOT NULL` | Login name |
| `password_hash` | `VARCHAR(200)` | `NOT NULL` | BCrypt password hash |


### 4) `user_role`
Many-to-many relation: user → role.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `user_id` | `BIGINT` | `PK`, `FK -> app_user(id)`, `NOT NULL` | User |
| `role_id` | `BIGINT` | `PK`, `FK -> role(id)`, `NOT NULL` | Assigned role |

### 5) `item`
Demo business entity protected by permissions.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Item ID |
| `name` | `VARCHAR(120)` | `NOT NULL` | Item name |
| `description` | `TEXT` | `NULL` | Item description |
| `updated_at` | `TIMESTAMP` | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Last update timestamp |

## Seed data

Migration `V2__seed.sql` inserts:

- roles: `CUSTOMER`, `BUSINESS`, `ADMIN`;
- users:
  - `customer1 / Password123!`
  - `business1 / Business123!`
  - `admin1 / Admin123!`
- demo items: `Laptop`, `Phone`, `Desk`.

## Roles and permissions matrix

Permissions:
- `DATA_READ`
- `DATA_UPDATE`
- `DATA_CREATE`
- `DATA_DELETE`

Role defaults:
- `CUSTOMER`: READ
- `BUSINESS`: READ + UPDATE
- `ADMIN`: READ + UPDATE + CREATE + DELETE

Effective user authorities are composed from:
1. all assigned roles (`user_role`) as `ROLE_*`
2. all role authorities (`role_authority`)

## Quick start

### Option A: Run locally without MySQL (default)

```bash
mvn spring-boot:run
```

By default, the service now uses an in-memory H2 database in MySQL compatibility mode, so Flyway migrations and seed data are applied automatically at startup.

### Option B: Run with MySQL (Docker Compose)

1. Start MySQL manually (recommended for first run):

```bash
docker compose up -d
```

> If `mysql_data` volume already exists, init scripts are not re-run. Use `docker compose down -v` and start again for full re-initialization.

2. Run the application with the `mysql` profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

When Docker is available, Spring Boot can also manage `docker compose` automatically (via `spring-boot-docker-compose`) so the MySQL container is started for you.

You can override connection values when needed:

```bash
MYSQL_HOST=localhost MYSQL_PORT=3307 MYSQL_DATABASE=appdb MYSQL_USERNAME=app MYSQL_PASSWORD=app \
  mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Flyway will apply migrations on startup (if schema is not initialized).

## API testing with curl

### Get a token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"customer1","password":"Password123!"}' | jq -r .accessToken)

echo "$TOKEN"
```

### Use token

```bash
curl -i http://localhost:8080/api/items \
  -H "Authorization: Bearer $TOKEN"
```

## Access examples

### `customer1 / Password123!`

```bash
# login
curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"customer1","password":"Password123!"}'

# GET -> 200
curl -i http://localhost:8080/api/items -H "Authorization: Bearer $TOKEN"

# PUT -> 403
curl -i -X PUT http://localhost:8080/api/items/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Updated by customer","description":"no rights"}'

# POST -> 403
curl -i -X POST http://localhost:8080/api/items \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"New item","description":"no rights"}'

# DELETE -> 403
curl -i -X DELETE http://localhost:8080/api/items/1 -H "Authorization: Bearer $TOKEN"
```

### `business1 / Business123!`

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"business1","password":"Business123!"}' | jq -r .accessToken)

# GET -> 200
curl -i http://localhost:8080/api/items -H "Authorization: Bearer $TOKEN"

# PUT -> 200
curl -i -X PUT http://localhost:8080/api/items/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Updated by business","description":"allowed"}'

# POST -> 403
curl -i -X POST http://localhost:8080/api/items \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"New item","description":"not allowed"}'
```

### `admin1 / Admin123!`

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin1","password":"Admin123!"}' | jq -r .accessToken)

# GET / POST / PUT / DELETE -> allowed
curl -i http://localhost:8080/api/items -H "Authorization: Bearer $TOKEN"
```
