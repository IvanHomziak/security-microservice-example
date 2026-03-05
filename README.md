# Security Microservice Example (Spring Boot 3 + Spring Security 6)

A demo security-focused microservice that showcases stateless JWT authentication, permission-based authorization, MySQL persistence, Flyway migrations, and Docker Compose setup.

## Documentation

- [`docs/spring-security-demo-presentation.md`](docs/spring-security-demo-presentation.md) — project-focused security demo presentation.
- [`docs/spring-security-how-it-works-detailed-presentation-en.md`](docs/spring-security-how-it-works-detailed-presentation-en.md) — detailed English presentation on how Spring Security works.

## Project overview

This educational backend service demonstrates a complete baseline authentication and access-control flow:

- **Username/password authentication** for users.
- **JWT token issuance** (HS256) after successful login.
- **Permission checks** on both HTTP endpoint and service layers.
- **RBAC + granular permissions** model (role-based + fine-grained authorities).
- **Database schema management with Flyway** using versioned SQL migrations.

### Main request flow

1. Client calls `POST /auth/login` with credentials.
2. Service validates credentials and returns a JWT access token.
3. Client sends the token via `Authorization: Bearer <token>`.
4. Service extracts authorities from the JWT and evaluates access rules.
5. Access to `/api/items/**` is granted/denied by permission (`DATA_READ`, `DATA_UPDATE`, `DATA_CREATE`, `DATA_DELETE`).

## Security model

- Each user has **one primary role** (`CUSTOMER`, `BUSINESS`, `ADMIN`).
- The role provides a **base set of permissions** via `role_authority`.
- Additional (or overriding) permissions can be assigned **directly to users** via `user_authority`.
- JWT contains aggregated authorities:
  - `ROLE_*` value for the role;
  - permissions inherited from the role;
  - permissions assigned directly to the user.

This design combines classic RBAC and permission-based access for flexibility.

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
  - schema creation (`app_user`, `role`, `authority`, `role_authority`, `user_authority`, `item`)
  - seed users/authorities/items.

## Database structure

### 1) `authority`
Permission dictionary (atomic access rights).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Technical permission identifier |
| `name` | `VARCHAR(50)` | `UNIQUE`, `NOT NULL` | Permission name (`DATA_READ`, `DATA_UPDATE`, ...) |

### 2) `role`
User role dictionary.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Role ID |
| `name` | `VARCHAR(30)` | `UNIQUE`, `NOT NULL` | Role name (`CUSTOMER`, `BUSINESS`, `ADMIN`) |

### 3) `role_authority`
Many-to-many relation: role → permission.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `role_id` | `BIGINT` | `PK`, `FK -> role(id)`, `NOT NULL` | Role |
| `authority_id` | `BIGINT` | `PK`, `FK -> authority(id)`, `NOT NULL` | Permission |

Notes:
- Composite PK (`role_id`, `authority_id`) prevents duplicates.
- `ON DELETE CASCADE` on both FKs cleans up links automatically.

### 4) `app_user`
System users table.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | User ID |
| `username` | `VARCHAR(100)` | `UNIQUE`, `NOT NULL` | Login name |
| `password_hash` | `VARCHAR(200)` | `NOT NULL` | BCrypt password hash |
| `role_id` | `BIGINT` | `FK -> role(id)`, `NOT NULL` | Primary role |

### 5) `user_authority`
Many-to-many relation: user → permission.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `user_id` | `BIGINT` | `PK`, `FK -> app_user(id)`, `NOT NULL` | User |
| `authority_id` | `BIGINT` | `PK`, `FK -> authority(id)`, `NOT NULL` | Personal permission |

### 6) `item`
Demo business entity protected by permissions.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Item ID |
| `name` | `VARCHAR(120)` | `NOT NULL` | Item name |
| `description` | `TEXT` | `NULL` | Item description |
| `updated_at` | `TIMESTAMP` | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Last update timestamp |

## Seed data

Migration `V2__seed.sql` inserts:

- permissions: `DATA_READ`, `DATA_UPDATE`, `DATA_CREATE`, `DATA_DELETE`;
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
1. role permissions (`role` + `role_authority`)
2. extra user permissions (`user_authority`)
3. role as `ROLE_*`

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
