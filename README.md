# Secure Notes API

A stateless REST API built with Spring Boot that demonstrates real-world JWT authentication and role-based access control using Spring Security. Users can register, log in, and manage personal notes — with admin-only deletion enforced at the security layer.

Built as a learning project to deeply understand how Spring Security, JWT filters, and authorization work internally.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.x |
| Security | Spring Security 7.x |
| Authentication | JWT (JJWT 0.11.5) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Boilerplate | Lombok |
| Testing | Postman |

---

## Project Structure

```
src/main/java/com/SneakySolo/SecureNote/
│
├── Config/
│   └── SecurityConfig.java          # SecurityFilterChain, AuthenticationProvider, beans
│
├── Controller/
│   ├── AuthController.java          # /api/auth/register, /api/auth/login
│   └── NoteController.java          # /api/notes CRUD endpoints
│
├── Dto/
│   ├── AuthResponseDTO.java         # { token }
│   ├── LoginRequestDTO.java         # { username, password }
│   ├── RegisterRequestDTO.java      # { username, password }
│   ├── NoteRequestDTO.java          # { title, content }
│   └── NoteResponseDTO.java         # { id, title, content, username }
│
├── Entity/
│   ├── User.java                    # users table
│   ├── Note.java                    # notes table
│   └── Role.java                    # USER, ADMIN enum
│
├── Exception/
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice error handling
│
├── Filter/
│   └── JWTAuthFilter.java           # OncePerRequestFilter — JWT validation
│
├── Repository/
│   ├── UserRepository.java
│   └── NoteRepository.java
│
├── Service/
│   ├── AuthService.java             # register + login logic
│   ├── NoteService.java             # note CRUD logic
│   └── CustomUserDetailsService.java # UserDetailsService implementation
│
└── Util/
    └── JWTUtil.java                 # Token generation, extraction, validation
```

---

## Database Schema

```sql
-- Users table
CREATE TABLE users (
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,        -- BCrypt hashed
    role     VARCHAR(50)  NOT NULL         -- 'USER' or 'ADMIN'
);

-- Notes table
CREATE TABLE notes (
    id      BIGSERIAL PRIMARY KEY,
    title   VARCHAR(255) NOT NULL,
    content TEXT         NOT NULL,
    user_id BIGINT       NOT NULL REFERENCES users(id)
);
```

---

## API Reference

### Auth Endpoints — Public

#### Register
```
POST /api/auth/register
```
```json
// Request body
{
  "username": "aditya",
  "password": "pass123"
}

// Response 200
"Successfully registered"

// Response 400 (duplicate username)
{ "error": "Username is already in use" }
```

#### Login
```
POST /api/auth/login
```
```json
// Request body
{
  "username": "aditya",
  "password": "pass123"
}

// Response 200
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}

// Response 401 (wrong credentials)
{ "error": "Invalid username or password" }
```

---

### Notes Endpoints — Authenticated (Bearer Token required)

#### Create Note
```
POST /api/notes
Authorization: Bearer <token>
```
```json
// Request body
{
  "title": "My First Note",
  "content": "Hello World"
}

// Response 201
{
  "id": 1,
  "title": "My First Note",
  "content": "Hello World",
  "username": "aditya"
}
```

#### Get My Notes
```
GET /api/notes
Authorization: Bearer <token>
```
```json
// Response 200
[
  {
    "id": 1,
    "title": "My First Note",
    "content": "Hello World",
    "username": "aditya"
  }
]
```

#### Delete Note — Admin Only
```
DELETE /api/notes/{id}
Authorization: Bearer <admin_token>
```
```
// Response 200
"Note deleted successfully"

// Response 403 (non-admin token)
```

---

## Security Architecture

```
Incoming Request
      │
      ▼
JWTAuthFilter (OncePerRequestFilter)
  ├── Extract Authorization header
  ├── Validate Bearer token via JWTUtil
  ├── Load UserDetails via CustomUserDetailsService
  └── Set Authentication in SecurityContextHolder
      │
      ▼
SecurityFilterChain (Authorization Rules)
  ├── /api/auth/**        → permitAll
  ├── DELETE /api/notes/** → hasRole(ADMIN)
  ├── /api/notes/**       → authenticated
  └── any other request   → authenticated
      │
      ▼
Controller → Service → Repository
```

### How JWT Works in this Project

1. On login, `AuthenticationManager` delegates to `DaoAuthenticationProvider`
2. `DaoAuthenticationProvider` loads the user via `CustomUserDetailsService` and verifies the BCrypt password
3. On success, `JWTUtil` generates a signed HS256 token containing the username as the subject
4. On subsequent requests, `JWTAuthFilter` extracts and validates the token, then sets the authentication into `SecurityContextHolder`
5. Spring Security reads from the context to enforce endpoint authorization rules

---

## Setup & Running

### Prerequisites
- Java 17+
- PostgreSQL running locally
- Maven

### 1. Create the database
```sql
CREATE DATABASE SecureNote;
```

### 2. Configure `application.properties`
```properties
spring.application.name=SecureNote

spring.datasource.url=jdbc:postgresql://localhost:5432/SecureNote
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

jwt.secret=your_base64_encoded_secret_key_min_256_bits
jwt.expiration=86400000
```

### 3. Generate a secure JWT secret
```bash
openssl rand -base64 32
```
Paste the output as the value of `jwt.secret`. The decoded result must be at least 256 bits for HMAC-SHA256.

### 4. Run the application
```bash
mvn spring-boot:run
```
Hibernate will auto-create the tables on first startup.

### 5. Create an admin user
Spring Security doesn't know about registration-time role assignment for admins, so insert one directly:
```sql
INSERT INTO users (username, password, role)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnvtRY9gPvm', 'ADMIN');
-- Password is: admin123
```

---

## Testing with Postman

| Step | Method | Endpoint | Auth | Expected |
|---|---|---|---|---|
| 1 | POST | `/api/auth/register` | None | 200 success message |
| 2 | POST | `/api/auth/register` | None | 400 duplicate error |
| 3 | POST | `/api/auth/login` | None | 200 JWT token |
| 4 | POST | `/api/auth/login` (wrong pass) | None | 401 error |
| 5 | POST | `/api/notes` | USER token | 201 note object |
| 6 | GET | `/api/notes` | USER token | 200 notes list |
| 7 | GET | `/api/notes` | No token | 403 |
| 8 | DELETE | `/api/notes/1` | USER token | 403 |
| 9 | DELETE | `/api/notes/1` | ADMIN token | 200 deleted |

---

## Key Concepts Learned

- How `SecurityFilterChain` evaluates authorization rules top-to-bottom with first-match semantics
- Why CSRF is disabled for stateless JWT APIs and why sessions are set to `STATELESS`
- How `DaoAuthenticationProvider` connects `UserDetailsService` and `PasswordEncoder` to verify credentials
- How `OncePerRequestFilter` intercepts requests and populates `SecurityContextHolder`
- Why DTOs are used as a firewall to prevent leaking entity internals like hashed passwords and roles
- How `FilterRegistrationBean` prevents Spring Boot from double-registering servlet filters
- The `ROLE_` prefix convention in Spring Security's authority system


## Thank you for your time :)
