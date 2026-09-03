# Auth Service

Authentication and authorization microservice for the **Clinic Management System**.

The `auth-service` is responsible for user identity, registration, login, password security, JWT generation, role management, and authentication-related service-to-service communication.

It is implemented as an independent Spring Boot microservice with its own PostgreSQL database.

---

# Overview

The authentication service owns the application's user identity and access-control data.

It is responsible for:

- User registration
- User login
- Password hashing
- JWT generation
- JWT-based authentication
- Role management
- Administrative user management
- Promotion of users to doctors
- Internal authentication for service-to-service communication

Professional doctor information is **not** stored in this service.

That information is managed by the separate `doctor-service`.

The relationship between the two services is based on the user's ID:

```text
auth-service                         doctor-service

User                                  Doctor
--------------------------------      --------------------------------
id = 15                               id = 4
username = doctor1                    userId = 15
role = DOCTOR                         specialization = Cardiology
                                      licenseNumber = DOC-001
```

There is no direct JPA relationship or database foreign key between the two microservices.

---

# Features

## Authentication

- User registration
- User login
- Password hashing using BCrypt
- JWT generation
- JWT expiration
- Bearer token authentication
- JWT authentication filter
- Spring Security integration

## Authorization

The service supports the following roles:

- `USER`
- `DOCTOR`
- `ADMIN`

Role information is included in the JWT.

Administrative operations are protected using Spring Security role-based authorization.

## User Management

- Get all users
- Get a user by ID
- Get the currently authenticated user
- Promote a `USER` to `DOCTOR`
- Prevent invalid role promotions

## Service-to-Service Authentication

The service supports authenticated communication from trusted internal services.

The `doctor-service` communicates with `auth-service` using:

```text
X-Service-Key
```

The service key is stored outside the source code using an environment variable.

---

# Technologies

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot | Application framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| PostgreSQL | Database |
| JJWT | JWT generation and validation |
| BCrypt | Password hashing |
| Jakarta Validation | Request validation |
| Maven | Dependency management |

---

# Project Structure

```text
auth-service/
├── pom.xml
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── clinic/
    │   │           └── auth/
    │   │               │
    │   │               ├── config/
    │   │               │   └── SecurityConfig.java
    │   │               │
    │   │               ├── controller/
    │   │               │   ├── AuthController.java
    │   │               │   └── UserController.java
    │   │               │
    │   │               ├── dto/
    │   │               │   ├── AuthResponse.java
    │   │               │   ├── LoginRequest.java
    │   │               │   ├── RegisterRequest.java
    │   │               │   └── UserResponse.java
    │   │               │
    │   │               ├── entity/
    │   │               │   └── User.java
    │   │               │
    │   │               ├── exception/
    │   │               │   ├── EmailAlreadyExistsException.java
    │   │               │   ├── GlobalExceptionHandler.java
    │   │               │   ├── InvalidUserOperationException.java
    │   │               │   ├── UserNotFoundException.java
    │   │               │   └── UsernameAlreadyExistsException.java
    │   │               │
    │   │               ├── repository/
    │   │               │   └── UserRepository.java
    │   │               │
    │   │               ├── role/
    │   │               │   └── Role.java
    │   │               │
    │   │               ├── security/
    │   │               │   ├── JwtAuthenticationFilter.java
    │   │               │   ├── JwtService.java
    │   │               │   └── ServiceAuthenticationFilter.java
    │   │               │
    │   │               ├── service/
    │   │               │   ├── CustomUserDetailsService.java
    │   │               │   └── UserService.java
    │   │               │
    │   │               └── AuthServiceApplication.java
    │   │
    │   └── resources/
    │       └── application.yml
    │
    └── test/
```

---

# Architecture

The authentication flow is based on JWT and Spring Security.

## User Login Flow

```text
Client
  |
  | POST /auth/login
  | username + password
  v
AuthController
  |
  v
AuthenticationManager
  |
  v
CustomUserDetailsService
  |
  v
PostgreSQL
  |
  | User + BCrypt password
  v
Authentication successful
  |
  v
JwtService
  |
  | Generate JWT
  v
AuthResponse
  |
  v
Client
```

---

# JWT Authentication

Protected requests use the following header:

```http
Authorization: Bearer <JWT>
```

The request is processed by `JwtAuthenticationFilter`.

```text
Client
  |
  | Authorization: Bearer <JWT>
  v
JwtAuthenticationFilter
  |
  v
JwtService
  |
  | Validate signature + expiration
  v
SecurityContext
  |
  v
Spring Security
  |
  +---- authenticated ----> endpoint
  |
  +---- unauthorized ------> 401
  |
  +---- forbidden ---------> 403
```

The JWT contains the user's identity and role.

Example payload:

```json
{
  "sub": "doctor1",
  "userId": 15,
  "role": "ROLE_DOCTOR",
  "iat": "...",
  "exp": "..."
}
```

The JWT is signed using the configured secret.

---

# API

Base URL:

```text
http://localhost:8081
```

---

## Register

Creates a new user.

### Request

```http
POST /auth/register
Content-Type: application/json
```

```json
{
  "username": "demo",
  "email": "demo@gmail.com",
  "password": "password"
}
```

New users receive the default role:

```text
USER
```

### Response

```text
User registered successfully
```

The password is encoded using BCrypt before being stored.

---

# Login

Authenticates an existing user and returns a JWT.

### Request

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "username": "demo",
  "password": "password"
}
```

### Response

```json
{
  "token": "eyJhbGciOi...",
  "type": "Bearer"
}
```

The token must be sent with protected requests:

```http
Authorization: Bearer <token>
```

---

# Get Current User

Returns information about the currently authenticated user.

### Request

```http
GET /users/me
Authorization: Bearer <token>
```

### Response

```text
Logged in as: demo
```

Any authenticated user can access this endpoint.

---

# Get All Users

Returns all registered users.

### Request

```http
GET /users
Authorization: Bearer <admin-token>
```

**Required role:** `ADMIN`

### Response

```json
[
  {
    "id": 1,
    "username": "demo",
    "email": "demo@gmail.com",
    "role": "USER"
  },
  {
    "id": 2,
    "username": "admin",
    "email": "admin@gmail.com",
    "role": "ADMIN"
  }
]
```

Passwords are never included in API responses.

### Authorization

```text
USER + valid JWT
        |
        v
GET /users
        |
        v
403 Forbidden
```

```text
ADMIN + valid JWT
        |
        v
GET /users
        |
        v
200 OK
```

---

# Get User by ID

Returns a user by their ID.

### Request

```http
GET /users/{id}
```

This endpoint is intended for authenticated internal service communication.

It is used by `doctor-service` when creating a doctor profile.

Example:

```http
GET /users/15
X-Service-Key: <internal-service-key>
```

### Response

```json
{
  "id": 15,
  "username": "doctor1",
  "email": "doctor@gmail.com",
  "role": "DOCTOR"
}
```

If the user does not exist:

```text
404 Not Found
```

Example:

```json
{
  "error": "User not found with id: 5"
}
```

---

# Promote User to Doctor

Promotes an existing `USER` to the `DOCTOR` role.

Only an administrator can perform this operation.

### Request

```http
PUT /users/{id}/promote-to-doctor
Authorization: Bearer <admin-token>
```

Example:

```http
PUT /users/15/promote-to-doctor
Authorization: Bearer <admin-token>
```

No request body is required.

### Successful response

```json
{
  "id": 15,
  "username": "doctor1",
  "email": "doctor@gmail.com",
  "role": "DOCTOR"
}
```

The role changes:

```text
USER
  |
  | ADMIN promotion
  v
DOCTOR
```

### Promotion restrictions

The following operations are rejected:

- Promoting an existing `DOCTOR`
- Promoting an `ADMIN` to `DOCTOR`
- Promoting a non-existent user

Example:

```json
{
  "error": "User is already a doctor"
}
```

or:

```json
{
  "error": "An admin cannot be promoted to doctor"
}
```

---

# Service-to-Service Authentication

The authentication service exposes user information to trusted internal services.

The `doctor-service` needs to verify whether a given `userId` exists and whether the user has the `DOCTOR` role.

The communication is protected using an internal API key.

## Flow

```text
Doctor Service
      |
      | GET /users/15
      | X-Service-Key: <internal-key>
      v
Auth Service
      |
      v
ServiceAuthenticationFilter
      |
      | validate X-Service-Key
      v
ROLE_SERVICE
      |
      v
GET user by ID
      |
      v
UserResponse
      |
      v
Doctor Service
```

The internal authentication is separate from user JWT authentication.

### User authentication

```text
Authorization: Bearer <JWT>
```

Identifies a user.

### Service authentication

```text
X-Service-Key: <internal-key>
```

Identifies a trusted internal service.

The user JWT is not required to be forwarded from `doctor-service` to `auth-service`.

---

# ServiceAuthenticationFilter

The internal service authentication is implemented through `ServiceAuthenticationFilter`.

The filter:

1. Reads the `X-Service-Key` header.
2. Compares it with the configured internal API key.
3. Rejects an invalid key with `401 Unauthorized`.
4. Creates a `ROLE_SERVICE` authentication for a valid key.
5. Allows the request to continue through Spring Security.

Conceptually:

```text
X-Service-Key
     |
     v
ServiceAuthenticationFilter
     |
     +---- invalid ----> 401
     |
     +---- valid
             |
             v
       ROLE_SERVICE
             |
             v
       protected endpoint
```

---

# Security

## Password Security

Passwords are never stored in plain text.

The service uses BCrypt:

```text
Plain password
      |
      v
BCryptPasswordEncoder
      |
      v
BCrypt hash
      |
      v
PostgreSQL
```

The password is never returned in `UserResponse`.

---

# JWT Security

JWT configuration:

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

The JWT secret is provided through an environment variable.

The secret must never be committed to Git.

---

# Internal Service Security

The service-to-service API key is also provided through an environment variable.

```yaml
service:
  api-key: ${AUTH_SERVICE_API_KEY}
```

Example environment variable:

```text
AUTH_SERVICE_API_KEY=<internal-service-key>
```

The actual secret value must not be stored in the repository.

---

# Roles

The current role enum is:

```java
public enum Role {
    USER,
    ADMIN,
    DOCTOR
}
```

| Role | Purpose |
|---|---|
| `USER` | Default role assigned during registration |
| `DOCTOR` | Doctor account and doctor-specific functionality |
| `ADMIN` | Administrative operations |

The `DOCTOR` role is used by `doctor-service` for doctor professional functionality.

---

# Authorization Matrix

| Operation | USER | DOCTOR | ADMIN | SERVICE |
|---|:---:|:---:|:---:|:---:|
| Register | ✅ | — | — | — |
| Login | ✅ | ✅ | ✅ | — |
| Get current user | ✅ | ✅ | ✅ | — |
| Get all users | ❌ | ❌ | ✅ | ❌ |
| Get user by ID | ❌ | ❌ | ❌ | ✅ |
| Promote user to doctor | ❌ | ❌ | ✅ | ❌ |

`SERVICE` is an internal security authority and is not a user role stored in the database.

---

# HTTP Status Codes

| Status | Meaning |
|---|---|
| `200 OK` | Request successful |
| `400 Bad Request` | Validation error |
| `401 Unauthorized` | Authentication is missing or invalid |
| `403 Forbidden` | Authenticated identity does not have sufficient permissions |
| `404 Not Found` | Requested user does not exist |
| `409 Conflict` | Business rule conflict |

Examples:

```text
USER + GET /users
→ 403 Forbidden
```

```text
Invalid JWT
→ 401 Unauthorized
```

```text
Invalid X-Service-Key
→ 401 Unauthorized
```

```text
Missing user
→ 404 Not Found
```

```text
Existing DOCTOR promoted again
→ 409 Conflict
```

---

# Validation

Registration and login requests use Jakarta Bean Validation.

Validation includes:

- Required username
- Required email
- Valid email format
- Required password

Invalid requests return:

```text
400 Bad Request
```

---

# Exception Handling

The service uses a global exception handler for application-level errors.

Currently handled exceptions include:

- Validation errors
- `UsernameAlreadyExistsException`
- `EmailAlreadyExistsException`
- `UserNotFoundException`
- `InvalidUserOperationException`

Examples:

```json
{
  "error": "Username already exists"
}
```

```json
{
  "error": "Email already exists"
}
```

```json
{
  "error": "User not found with id: 5"
}
```

```json
{
  "error": "User is already a doctor"
}
```

---

# Database

The authentication service uses PostgreSQL.

Default local configuration:

```text
Host: localhost
Port: 5433
Database: clinic_auth
Username: clinic_auth_user
```

The `users` table contains:

```text
id
username
email
password
role
```

Username and email are unique.

The password column stores a BCrypt hash.

The role is stored according to the application's `Role` enum.

---

# Configuration

Example configuration:

```yaml
spring:
  application:
    name: auth-service

  datasource:
    url: jdbc:postgresql://localhost:5433/clinic_auth
    username: clinic_auth_user
    password: ${POSTGRES}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

server:
  port: 8081

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

service:
  api-key: ${AUTH_SERVICE_API_KEY}
```

The service runs on:

```text
http://localhost:8081
```

---

# Environment Variables

The service requires:

```text
POSTGRES=<database-password>
JWT_SECRET=<jwt-secret>
AUTH_SERVICE_API_KEY=<internal-service-key>
```

Never commit real secrets to Git.

---

# Running the Application

## Requirements

- Java 21
- Maven
- PostgreSQL
- IntelliJ IDEA or another Java IDE

## Database

Create a PostgreSQL database for the authentication service.

Example:

```text
Database: clinic_auth
Port: 5433
Username: clinic_auth_user
```

Configure the database credentials through environment variables.

---

## Start

From IntelliJ IDEA:

1. Configure the required environment variables.
2. Make sure PostgreSQL is running.
3. Run `AuthServiceApplication`.
4. The application starts on port `8081`.

Alternatively, on Windows:

```bash
mvnw.cmd spring-boot:run
```

---

# Integration with Doctor Service

The authentication service is the owner of user identity and roles.

The doctor service owns professional doctor information.

The complete doctor creation flow is:

```text
1. USER registers
        |
        v
2. ADMIN promotes USER
        |
        v
3. USER becomes DOCTOR
        |
        v
4. ADMIN creates Doctor profile
        |
        v
5. doctor-service calls auth-service
        |
        | GET /users/{userId}
        | X-Service-Key
        v
6. auth-service validates internal service
        |
        v
7. User exists?
        |
        +---- NO ----> 404
        |
        v
8. User role == DOCTOR?
        |
        +---- NO ----> 409
        |
        v
9. doctor-service creates Doctor profile
```

The two databases remain independent:

```text
clinic_auth
     |
     | User.id
     |
     +--------------------+
                          |
                          v
                    Doctor.userId
                          |
                    clinic_doctor
```

No cross-service database foreign key is used.

---

# Testing

The service has been tested using Postman.

Tested scenarios include:

### Registration

```text
POST /auth/register
→ 200 OK
```

### Duplicate username

```text
POST /auth/register
→ 409 Conflict
```

### Duplicate email

```text
POST /auth/register
→ 409 Conflict
```

### Login

```text
POST /auth/login
→ 200 OK
→ JWT returned
```

### Current user

```text
GET /users/me
→ 200 OK
```

### Admin user listing

```text
ADMIN + GET /users
→ 200 OK
```

### Unauthorized user listing

```text
USER + GET /users
→ 403 Forbidden
```

### Doctor promotion

```text
ADMIN + PUT /users/{id}/promote-to-doctor
→ 200 OK
```

### Invalid doctor promotion

```text
Already DOCTOR
→ 409 Conflict
```

```text
ADMIN promoted to DOCTOR
→ 409 Conflict
```

### User lookup

```text
SERVICE + GET /users/{id}
→ 200 OK
```

### Non-existent user

```text
SERVICE + GET /users/{id}
→ 404 Not Found
```

### Invalid service authentication

```text
Invalid X-Service-Key
→ 401 Unauthorized
```

### Missing/invalid JWT

```text
Protected endpoint
→ 401 Unauthorized
```

---

# Version 1.2.0

## Status

```text
Auth Service
Version: 1.2.0
Status: Stable
```

Version `1.2.0` represents the current completed authentication milestone of the Clinic Management System.

### Implemented

- [x] Spring Boot application
- [x] PostgreSQL integration
- [x] User entity
- [x] User repository
- [x] User registration
- [x] BCrypt password hashing
- [x] Login
- [x] JWT generation
- [x] JWT validation
- [x] JWT authentication filter
- [x] AuthResponse
- [x] UserResponse
- [x] Request validation
- [x] Global exception handling
- [x] Role-based authorization
- [x] `USER` role
- [x] `DOCTOR` role
- [x] `ADMIN` role
- [x] ADMIN-only user listing
- [x] ADMIN-only doctor promotion
- [x] USER → DOCTOR role transition
- [x] Prevention of invalid role promotions
- [x] Get user by ID
- [x] Internal service authentication
- [x] `X-Service-Key` authentication
- [x] `ROLE_SERVICE`
- [x] Environment-based JWT configuration
- [x] Environment-based internal service API key
- [x] Integration with `doctor-service`

---

# Version History

## v1.0.0

Initial authentication service implementation.

Implemented:

- User registration
- Login
- BCrypt password hashing
- JWT authentication
- JWT validation
- Spring Security
- PostgreSQL persistence
- Basic role-based authorization
- ADMIN-protected endpoints
- Request validation
- Exception handling

---

## v1.1.0

Added doctor role management.

Implemented:

- `DOCTOR` role
- ADMIN-only doctor promotion
- `USER → DOCTOR` transition
- Prevention of promoting existing doctors
- Prevention of promoting administrators
- Separation between authentication data and professional doctor data

---

## v1.2.0

Added integration with `doctor-service`.

Implemented:

- `GET /users/{id}`
- Internal service authentication
- `X-Service-Key`
- `ServiceAuthenticationFilter`
- `ROLE_SERVICE`
- Secure service-to-service communication
- User existence validation for doctor creation
- Doctor role validation through `auth-service`
- Environment-based internal API key configuration
- Complete integration between `auth-service` and `doctor-service`

---

# Related Services

The authentication service currently integrates with:

```text
doctor-service
```

Responsibilities are separated as follows:

```text
auth-service
├── User identity
├── Credentials
├── Roles
├── Login
├── JWT
└── Authentication

doctor-service
├── Doctor profiles
├── Professional experiences
├── Studies
└── Doctor-specific professional data
```

This separation allows each microservice to own its own domain and database while communicating through well-defined APIs.