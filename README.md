# Security Microservice Example (Spring Boot 3 + Spring Security 6)

Демо-проєкт мікросервісу безпеки зі stateless JWT-auth + permission-based authorization, MySQL, Flyway та Docker Compose.

## Детальний опис проєкту

Це навчальний backend-сервіс, який демонструє повний базовий цикл авторизації й контролю доступу:

- **автентифікація** користувача через `username/password`;
- **видача JWT-токена** (HS256) після успішного логіну;
- **перевірка прав доступу** на рівні HTTP endpoint-ів та service-layer;
- **RBAC + permissions модель** (роль + granular permissions);
- **керування схемою БД через Flyway** з версіонованими міграціями.

### Ключові сценарії

1. Клієнт викликає `POST /auth/login` з логіном і паролем.
2. Сервіс перевіряє облікові дані та повертає JWT.
3. Клієнт додає токен в `Authorization: Bearer <token>`.
4. Сервіс витягує authorities з токена та вирішує, чи дозволена операція.
5. Доступ до `items` відкривається/блокується залежно від permission (`DATA_READ`, `DATA_UPDATE`, `DATA_CREATE`, `DATA_DELETE`).

### Логіка безпеки

- Кожен користувач має **одну роль** (`CUSTOMER`, `BUSINESS`, `ADMIN`).
- Роль дає **базовий набір permissions** через зв'язку `role_authority`.
- Додаткові (або дублюючі) права можуть задаватися **безпосередньо користувачу** через `user_authority`.
- У JWT потрапляє агрегований список authorities:
  - `ROLE_*` (роль користувача);
  - permissions з ролі;
  - персональні permissions користувача.

Таким чином проєкт показує комбінацію Role-Based Access Control (RBAC) і permission-based моделі для більш гнучкого керування доступом.

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

## Опис структури БД (детально)

Нижче — таблиці, які створюються міграцією `V1__init.sql`, та їх призначення.

### 1) `authority`

Довідник permissions (атомарних прав).

| Колонка | Тип | Обмеження | Опис |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Технічний ідентифікатор permission |
| `name` | `VARCHAR(50)` | `UNIQUE`, `NOT NULL` | Назва permission (`DATA_READ`, `DATA_UPDATE`, ...) |

**Призначення:** зберігає список усіх доступних дозволів у системі.

### 2) `role`

Довідник ролей користувачів.

| Колонка | Тип | Обмеження | Опис |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Ідентифікатор ролі |
| `name` | `VARCHAR(30)` | `UNIQUE`, `NOT NULL` | Назва ролі (`CUSTOMER`, `BUSINESS`, `ADMIN`) |

**Призначення:** визначає верхньорівневу категорію доступу користувача.

### 3) `role_authority`

Зв’язувальна таблиця "роль → permission" (many-to-many).

| Колонка | Тип | Обмеження | Опис |
|---|---|---|---|
| `role_id` | `BIGINT` | `PK`, `FK -> role(id)`, `NOT NULL` | Роль |
| `authority_id` | `BIGINT` | `PK`, `FK -> authority(id)`, `NOT NULL` | Permission |

**Особливості:**
- композитний первинний ключ (`role_id`, `authority_id`) унеможливлює дублікати;
- `ON DELETE CASCADE` для обох FK — видалення ролі/permission очищає зв'язки.

### 4) `app_user`

Таблиця користувачів системи.

| Колонка | Тип | Обмеження | Опис |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Ідентифікатор користувача |
| `username` | `VARCHAR(100)` | `UNIQUE`, `NOT NULL` | Логін |
| `password_hash` | `VARCHAR(200)` | `NOT NULL` | BCrypt-хеш пароля |
| `role_id` | `BIGINT` | `FK -> role(id)`, `NOT NULL` | Основна роль користувача |

**Призначення:** зберігає облікові записи та їх базову роль.

### 5) `user_authority`

Зв’язувальна таблиця "користувач → permission" (many-to-many).

| Колонка | Тип | Обмеження | Опис |
|---|---|---|---|
| `user_id` | `BIGINT` | `PK`, `FK -> app_user(id)`, `NOT NULL` | Користувач |
| `authority_id` | `BIGINT` | `PK`, `FK -> authority(id)`, `NOT NULL` | Персональне permission |

**Особливості:**
- дозволяє видавати granular права поверх ролі;
- `ON DELETE CASCADE` на обох FK автоматично очищає зв’язки.

### 6) `item`

Демо-сутність для перевірки контролю доступу.

| Колонка | Тип | Обмеження | Опис |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Ідентифікатор запису |
| `name` | `VARCHAR(120)` | `NOT NULL` | Назва об’єкта |
| `description` | `TEXT` | `NULL` | Опис |
| `updated_at` | `TIMESTAMP` | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Час останнього оновлення |

**Призначення:** приклад бізнес-таблиці, доступ до якої обмежується permissions.

### Початкові дані (seed)

Міграція `V2__seed.sql` створює:

- permissions: `DATA_READ`, `DATA_UPDATE`, `DATA_CREATE`, `DATA_DELETE`;
- ролі: `CUSTOMER`, `BUSINESS`, `ADMIN`;
- користувачів:
  - `customer1 / Password123!`
  - `business1 / Business123!`
  - `admin1 / Admin123!`
- тестові записи в `item` (`Laptop`, `Phone`, `Desk`).

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
