# Презентація: Spring Security концепція у `security-microservice-example`

> Формат: markdown-слайди (кожен блок між `---` можна використовувати як окремий слайд у Marp/Slidev/Reveal або для DEMO-зустрічі).

---

## 1) Навіщо цей проєкт

**Ціль проєкту** — показати практичну, production-like основу безпеки в Spring Boot:
- аутентифікація через username/password;
- видача stateless JWT токена;
- авторизація по granular permissions;
- поєднання RBAC (ролі) + direct user permissions;
- подвійний контроль доступу: HTTP-рівень + service-рівень.

**Що отримує команда після knowledge transfer:**
- шаблон для нових мікросервісів;
- спільну мову між backend/devops/QA щодо access-control;
- готову базу для переходу до OAuth2/OIDC або інтеграції з IAM.

---

## 2) High-level security architecture

```text
Client -> POST /auth/login -> AuthenticationManager -> UserDetailsService (JPA)
       <- JWT (HS256, claim: auth[])

Client -> /api/items/** + Bearer token
       -> SecurityFilterChain (JWT Resource Server)
       -> JWT -> GrantedAuthorities
       -> endpoint rules + @PreAuthorize
       -> allow / deny
```

Ключова ідея: **сервіс не зберігає сесій**, довіряє лише підписаному JWT + перевіреним правилам авторизації.

---

## 3) Ключові Spring Security building blocks у проєкті

1. **`SecurityFilterChain`**
   - вимикає CSRF для API;
   - вмикає `SessionCreationPolicy.STATELESS`;
   - описує rules по HTTP методах для `/api/items/**`;
   - підключає `oauth2ResourceServer().jwt(...)`.

2. **`UserDetailsService`**
   - завантажує користувача з БД;
   - агрегує authorities з ролі + user-specific permissions + `ROLE_*`.

3. **`DaoAuthenticationProvider` + `AuthenticationManager`**
   - перевіряє пару username/password;
   - використовує BCrypt hash.

4. **`JwtEncoder`/`JwtDecoder` (Nimbus)**
   - HS256 на базі secret key;
   - єдина криптографічна основа для login та resource access.

---

## 4) Модель доступу: RBAC + Permission-based

### Ролі
- `CUSTOMER`
- `BUSINESS`
- `ADMIN`

### Permissions (atomic rights)
- `DATA_READ`
- `DATA_UPDATE`
- `DATA_CREATE`
- `DATA_DELETE`

### Effective authorities користувача
`role authorities` + `user authorities` + `ROLE_<role>`

Це дає баланс:
- **простота** через ролі;
- **гнучкість** через точкові персональні права.

---

## 5) Login flow (аутентифікація)

1. Клієнт відправляє `POST /auth/login` з credentials.
2. `AuthenticationManager` аутентифікує через `DaoAuthenticationProvider`.
3. Після успіху формується список authorities.
4. Генерується JWT з claim `auth` (масив прав).
5. Токен повертається клієнту як `accessToken`.

**Важливо:** сервіс не покладається на роль у відриві від permissions — у токен кладеться повний effective набір прав.

---

## 6) Request authorization flow (після login)

1. Клієнт додає `Authorization: Bearer <token>`.
2. Resource Server декодує та валідує JWT.
3. `JwtAuthenticationConverter` бере `claim("auth")` і перетворює у `GrantedAuthority`.
4. Далі спрацьовують:
   - правила в `SecurityFilterChain` (endpoint-level);
   - правила в `@PreAuthorize` (method-level).
5. Якщо право відсутнє — `403 Forbidden`.

---

## 7) Два рівні захисту: чому це best practice

### Рівень 1: HTTP endpoint security
Переваги:
- швидке відсікання неавторизованих запитів;
- єдина, читабельна карта доступів API.

### Рівень 2: Service method security (`@PreAuthorize`)
Переваги:
- захист від випадкових bypass-ів через інші entry points;
- безпека залишається навіть при рефакторингу контролерів.

**Практичний принцип:** *defense in depth* у межах одного мікросервісу.

---

## 8) Mapping доступів для `/api/items/**`

- `GET` -> `DATA_READ`
- `POST` -> `DATA_CREATE`
- `PUT`/`PATCH` -> `DATA_UPDATE`
- `DELETE` -> `DATA_DELETE`

При цьому ті самі права продубльовані в `ItemService` через `@PreAuthorize`.

---

## 9) Database layer як джерело політики доступу

Ключові таблиці:
- `role`
- `authority`
- `role_authority`
- `app_user`
- `user_authority`

Що це дає для DEMO:
- показуємо, що policy зберігається в БД, а не хардкодиться в коді;
- можна змінювати права без перекомпіляції сервісу (через data migration/admin tooling).

---

## 10) DEMO сценарій (рекомендований)

1. **Login як `customer1`** -> отримати token.
2. `GET /api/items` -> **200** (є `DATA_READ`).
3. `PUT /api/items/1` -> **403** (нема `DATA_UPDATE`).
4. **Login як `business1`** -> `PUT` -> **200**.
5. **Login як `admin1`** -> `POST/DELETE` -> **200**.

Пояснення під час DEMO:
- однаковий API, різна поведінка залежно від authorities у JWT.

---

## 11) Що важливо донести команді (knowledge transfer)

- Безпека = **не тільки login**, а повний lifecycle доступу.
- Роль не замінює granular permissions.
- Stateless JWT спрощує масштабування мікросервісів.
- Method security захищає бізнес-логіку незалежно від transport-рівня.
- Flyway + seed-дані роблять security-поведінку відтворюваною у будь-якому середовищі.

---

## 12) Обмеження поточної реалізації (чесно для технічної аудиторії)

- Один shared HS256 secret (без key rotation).
- Немає refresh token / token revocation.
- Немає централізованого IAM (Keycloak/Auth0/Azure AD).
- Відсутній аудит security events (login fail/success, privilege changes).

Це нормально для educational baseline, але важливо проговорити road-map.

---

## 13) Еволюція до production

1. Перейти на асиметричні ключі (RS256/ES256) + JWKS.
2. Винести auth у окремий Identity Provider.
3. Додати refresh tokens + короткий TTL access token.
4. Додати audit trail і alerting на security події.
5. Впровадити integration/contract security tests у CI.

---

## 14) Q&A / Cheat Sheet для тренінгу

**Q:** Чому не тільки `ROLE_ADMIN` checks?
**A:** Ролі занадто грубі; permissions дають контроль на рівні операцій.

**Q:** Навіщо дублювати перевірки у `SecurityFilterChain` і `@PreAuthorize`?
**A:** Це defense-in-depth, що знижує ризик помилок при рефакторингу.

**Q:** Де змінювати права користувача?
**A:** Через таблиці зв’язків `role_authority` / `user_authority` (через migration або адмін-інструмент).

---

## 15) Практичне завершення сесії

Після презентації команда має вміти:
- спроєктувати role/permission модель під новий домен;
- налаштувати JWT-based stateless security в Spring;
- застосовувати endpoint + method security;
- підготувати DEMO-сценарій перевірки прав для QA та бізнес-стейкхолдерів.

**Рекомендація:** використовуйте цей файл як живий артефакт — оновлюйте разом із змінами security-моделі в коді.
