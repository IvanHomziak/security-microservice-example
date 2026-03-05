# Presentation: Spring Security Concept in `security-microservice-example`

---

## 1) Why this project exists

**Project goal** — to demonstrate a practical, production-like security baseline in Spring Boot:
- authentication via username/password;
- issuing a stateless JWT token;
- authorization based on granular permissions;
- combining RBAC (roles) + direct user permissions;
- dual access control: HTTP level + service level.

**Core concepts:**
- a template for new microservices;
- shared language between backend/devops/QA regarding access control;
- a ready foundation for migration to OAuth2/OIDC or integration with IAM.

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

Key idea: **the service does not store sessions**, and trusts only a signed JWT plus verified authorization rules.

---

## 3) Key Spring Security building blocks in the project

1. **`SecurityFilterChain`**
   - disables CSRF for API;
   - enables `SessionCreationPolicy.STATELESS`;
   - defines HTTP method rules for `/api/items/**`;
   - connects `oauth2ResourceServer().jwt(...)`.

2. **`UserDetailsService`**
   - loads a user from DB;
   - aggregates authorities from role + user-specific permissions + `ROLE_*`.

3. **`DaoAuthenticationProvider` + `AuthenticationManager`**
   - verifies username/password pairs;
   - uses BCrypt hash.

4. **`JwtEncoder`/`JwtDecoder` (Nimbus)**
   - HS256 based on a secret key;
   - a single cryptographic foundation for login and resource access.

---

## 4) Access model: RBAC + Permission-based

### Roles
- `CUSTOMER`
- `BUSINESS`
- `ADMIN`

### Permissions (atomic rights)
- `DATA_READ`
- `DATA_UPDATE`
- `DATA_CREATE`
- `DATA_DELETE`

### User effective authorities
`role authorities` + `user authorities` + `ROLE_<role>`

This gives a balance of:
- **simplicity** through roles;
- **flexibility** through targeted personal rights.

---

## 5) Login flow (authentication)

1. Client sends `POST /auth/login` with credentials.
2. `AuthenticationManager` authenticates via `DaoAuthenticationProvider`.
3. After success, the authorities list is formed.
4. JWT is generated with claim `auth` (permissions array).
5. Token is returned to the client as `accessToken`.

**Important:** the service does not rely on role in isolation from permissions — the token contains the full effective set of rights.

---

## 6) Request authorization flow (after login)

1. Client adds `Authorization: Bearer <token>`.
2. Resource Server decodes and validates JWT.
3. `JwtAuthenticationConverter` takes `claim("auth")` and converts it to `GrantedAuthority`.
4. Then the following are applied:
   - rules in `SecurityFilterChain` (endpoint-level);
   - rules in `@PreAuthorize` (method-level).
5. If permission is missing — `403 Forbidden`.

---

## 7) Two protection levels: why this is a best practice

### Level 1: HTTP endpoint security
Benefits:
- fast rejection of unauthorized requests;
- single, readable API access map.

### Level 2: Service method security (`@PreAuthorize`)
Benefits:
- protection against accidental bypasses through other entry points;
- security stays intact even during controller refactoring.

**Practical principle:** *defense in depth* within a single microservice.

---

## 8) Access mapping for `/api/items/**`

- `GET` -> `DATA_READ`
- `POST` -> `DATA_CREATE`
- `PUT`/`PATCH` -> `DATA_UPDATE`
- `DELETE` -> `DATA_DELETE`

At the same time, the same permissions are duplicated in `ItemService` via `@PreAuthorize`.

---

## 9) Database layer as the source of access policy

Key tables:
- `role`
- `authority`
- `role_authority`
- `app_user`
- `user_authority`

What this provides for the DEMO:
- shows that policy is stored in DB, not hardcoded in application code;
- allows changing permissions without recompiling the service (via data migration/admin tooling).

---

## 10) DEMO scenario (recommended)

1. **Login as `customer1`** -> receive token.
2. `GET /api/items` -> **200** (has `DATA_READ`).
3. `PUT /api/items/1` -> **403** (does not have `DATA_UPDATE`).
4. **Login as `business1`** -> `PUT` -> **200**.
5. **Login as `admin1`** -> `POST/DELETE` -> **200**.

Explanation during DEMO:
- same API, different behavior depending on JWT authorities.

---

## 11) What is important

- Security = **not only login**, but the full access lifecycle.
- Role does not replace granular permissions.
- Stateless JWT simplifies microservice scaling.
- Method security protects business logic independently from transport layer.
- Flyway + seed data make security behavior reproducible in any environment.

---

## 12) Current implementation limitations (honestly for a technical audience)

- Single shared HS256 secret (no key rotation).
- No refresh token / token revocation.
- No centralized IAM (Keycloak/Auth0/Azure AD).
- No security event audit (login fail/success, privilege changes).

This is fine for an educational baseline, but it is important to discuss the roadmap.

---

## 13) Evolution toward production

1. Move to asymmetric keys (RS256/ES256) + JWKS.
2. Extract auth into a separate Identity Provider.
3. Add refresh tokens + short access token TTL.
4. Add audit trail and alerting for security events.
5. Introduce integration/contract security tests in CI.

---

## 14) Q&A / Cheat Sheet for training

**Q:** Why not only `ROLE_ADMIN` checks?
**A:** Roles are too coarse; permissions provide operation-level control.

**Q:** Why duplicate checks in `SecurityFilterChain` and `@PreAuthorize`?
**A:** This is defense-in-depth, which reduces the risk of mistakes during refactoring.

**Q:** Where should user permissions be changed?
**A:** Through relation tables `role_authority` / `user_authority` (via migration or admin tool).

---

## 15) Practical session wrap-up illustrate

- design a role/permission model for a new domain;
- configure JWT-based stateless security in Spring;
- apply endpoint + method security;
- prepare a DEMO scenario for permission validation by QA and business stakeholders.

**Recommendation:** use this file as a living artifact — update it together with security model changes in code.
