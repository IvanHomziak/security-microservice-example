# Security Microservice Example (Spring Boot 3 + Spring Security 6)

Демо-проєкт зі stateless JWT-auth + permission-based authorization, MySQL, Flyway та Docker Compose.

## Stack

- Java 21
- Maven
- Spring Boot 3.x
- Spring Security 6 (Resource Server + JWT)
- MySQL
- Flyway

## Що реалізовано

- `POST /auth/login` — логін через username/password, повертає JWT (HS256)
- `GET /auth/me` — показує username та authorities з токена
- Permission-based доступ до `/api/items/**`
- Service-layer захист через `@PreAuthorize`
- Flyway міграції:
  - створення схеми (`app_user`, `role`, `authority`, `role_authority`, `user_authority`, `item`)
  - seed users/authorities/items

## Ролі та permissions

Permissions:
- `DATA_READ`
- `DATA_UPDATE`
- `DATA_CREATE`
- `DATA_DELETE`

Roles:
- `CUSTOMER`: READ
- `BUSINESS`: READ + UPDATE
- `ADMIN`: READ + UPDATE + CREATE + DELETE

Ефективні authorities користувача формуються як:
1. permissions із таблиці ролі (`role` + `role_authority`)
2. додаткові permissions із таблиці `user_authority`
3. роль як `ROLE_*`

## Швидкий старт

1. Підняти БД (під час першого старту MySQL автоматично створить усі таблиці з `docker/mysql/init/001_create_tables.sql`):

```bash
docker compose up -d
```

> Якщо volume `mysql_data` уже існує, init-скрипти не перезапускаються. Для повторної ініціалізації виконайте `docker compose down -v` і підніміть контейнери знову.

2. Запустити застосунок:

```bash
mvn spring-boot:run
```

Після старту Flyway автоматично застосує міграції та seed.

## Тестування API через curl

### Отримати токен у змінну

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"customer1","password":"Password123!"}' | jq -r .accessToken)

echo "$TOKEN"
```

Використання:

```bash
curl -i http://localhost:8080/api/items \
  -H "Authorization: Bearer $TOKEN"
```

---

## Матриця доступів

### A) `customer1 / Password123!`

```bash
# login
curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"customer1","password":"Password123!"}'

# GET -> 200
curl -i http://localhost:8080/api/items \
  -H "Authorization: Bearer $TOKEN"

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
curl -i -X DELETE http://localhost:8080/api/items/1 \
  -H "Authorization: Bearer $TOKEN"
```

### B) `business1 / Business123!`

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

# PATCH -> 200
curl -i -X PATCH http://localhost:8080/api/items/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"description":"patched by business"}'

# POST -> 403
curl -i -X POST http://localhost:8080/api/items \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Denied create","description":"should be forbidden"}'

# DELETE -> 403
curl -i -X DELETE http://localhost:8080/api/items/1 \
  -H "Authorization: Bearer $TOKEN"
```

### C) `admin1 / Admin123!`

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin1","password":"Admin123!"}' | jq -r .accessToken)

# GET -> 200
curl -i http://localhost:8080/api/items -H "Authorization: Bearer $TOKEN"

# POST -> 200
curl -i -X POST http://localhost:8080/api/items \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Admin created","description":"ok"}'

# PUT -> 200
curl -i -X PUT http://localhost:8080/api/items/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Admin updated","description":"ok"}'

# PATCH -> 200
curl -i -X PATCH http://localhost:8080/api/items/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"description":"admin patch"}'

# DELETE -> 204
curl -i -X DELETE http://localhost:8080/api/items/1 \
  -H "Authorization: Bearer $TOKEN"
```
