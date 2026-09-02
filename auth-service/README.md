# Auth Service

Authentication and authorization microservice for the **Clinic Management System**.
[]()
This service is responsible for user registration, login, password security, JWT generation and validation, and role-based access control.

The service is built as an independent Spring Boot application and is intended to be used as the authentication component of the future Clinic Management System microservices architecture.

---

## Features

### Authentication

* User registration
* User login
* Password hashing using BCrypt
* JWT-based authentication
* JWT expiration
* Bearer token authentication
* Authentication through Spring Security

### Authorization

The service supports role-based authorization using the following roles:

* `USER`
* `ADMIN`
* `DOCTOR`
* `PATIENT`

Currently implemented:

* `USER` authentication
* `ADMIN` authorization
* Protected endpoints using Spring Security
* Role information included in the JWT

`DOCTOR` and `PATIENT` roles are defined for the future microservices implementation.

### Security

* Passwords are never returned through API responses
* Passwords are stored using BCrypt hashes
* JWT secret is provided through an environment variable
* Protected endpoints require a valid Bearer token
* Administrative endpoints require the `ADMIN` role
* Invalid authentication results in `401 Unauthorized`
* Authenticated users without sufficient permissions receive `403 Forbidden`

---

## Technologies

| Technology      | Purpose                          |
| --------------- | -------------------------------- |
| Java 21         | Programming language             |
| Spring Boot     | Application framework            |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database access                  |
| Hibernate       | ORM                              |
| PostgreSQL      | Database                         |
| JJWT            | JWT generation and validation    |
| BCrypt          | Password hashing                 |
| Maven           | Dependency management            |

---

## Project Structure

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
    │   │               │   └── JwtService.java
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

## Architecture

The authentication flow is based on JWT and Spring Security.

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
  | user + BCrypt password
  v
Authentication successful
  |
  v
JwtService
  |
  | JWT
  v
AuthResponse
  |
  | token + type
  v
Client
```

For protected endpoints:

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
  | validate JWT
  v
CustomUserDetailsService
  |
  | load user + role
  v
Spring Security
  |
  +---- authenticated ----> endpoint
  |
  +---- unauthorized ------> 401
  |
  +---- forbidden ---------> 403
```

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

Newly registered users receive the default role:

```text
USER
```

### Response

```text
User registered successfully
```

The password is hashed before being stored in the database.

---

## Login

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

The JWT contains the authenticated username and role.

The token must be sent with protected requests using:

```http
Authorization: Bearer <token>
```

---

## Get Current User

Returns information about the currently authenticated user.

### Request

```http
GET /users/me
Authorization: Bearer <token>
```

### Response

```text
Logged in as: alex
```

Any authenticated user can access this endpoint.

---

## Get All Users

Returns all registered users.

### Request

```http
GET /users
Authorization: Bearer <admin-token>
```

This endpoint is restricted to users with the `ADMIN` role.

### Successful response

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

The password is intentionally not included in the response.

### Authorization behavior

A normal user:

```text
GET /users
→ 403 Forbidden
```

An administrator:

```text
GET /users
→ 200 OK
```

This behavior confirms that role-based authorization is working correctly.

---
## Promote User to Doctor

Promotes an existing `USER` to the `DOCTOR` role.

This operation can only be performed by an authenticated administrator.

### Request

```http
PUT /users/{id}/promote-to-doctor
Authorization: Bearer <admin-token>
```

Example:

```http
PUT /users/1/promote-to-doctor
Authorization: Bearer <admin-token>
```

No request body is required.

### Successful response

```json
{
  "id": 1,
  "username": "demo",
  "email": "demo@gmail.com",
  "role": "DOCTOR"
}
```

The user's role is changed in the `auth-service` database:

```text
USER → DOCTOR
```

### Authorization behavior

A normal user:

```text
PUT /users/1/promote-to-doctor
→ 403 Forbidden
```

An administrator:

```text
PUT /users/1/promote-to-doctor
→ 200 OK
```

### Promotion restrictions

The following operations are not allowed:

* A user who is already a `DOCTOR` cannot be promoted again.
* An `ADMIN` cannot be promoted to `DOCTOR`.

The professional information of the doctor, such as specialization, license number, experience and studies, will be managed by the `doctor-service`.

The `auth-service` is responsible only for the user's identity and role.

# Roles

The current role enum is:

```java
public enum Role {
    USER,
    ADMIN,
    DOCTOR,
    PATIENT
}
```

### Current roles

| Role      | Current purpose                           |
| --------- | ----------------------------------------- |
| `USER`    | Default role assigned during registration |
| `ADMIN`   | Administrative operations                 |
| `DOCTOR`  | Reserved for doctor functionality         |
| `PATIENT` | Reserved for patient functionality        |

The `DOCTOR` and `PATIENT` roles will be used by future services.

---

# Security

## Password Security

Passwords are never stored as plain text.

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

API responses use `UserResponse` instead of exposing the `User` entity directly.

Therefore, the password field is never returned to clients.

---

## JWT

JWT configuration uses environment variables.

Example:

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

The secret itself must not be committed to Git.

Example environment variables:

```text
JWT_SECRET=<your-secret>
POSTGRES=<your-database-password>
```

---

# HTTP Status Codes

| Status             | Meaning                                                   |
| ------------------ | --------------------------------------------------------- |
| `200 OK`           | Request successful                                        |
| `400 Bad Request`  | Validation error                                          |
| `401 Unauthorized` | Authentication is missing or invalid                      |
| `403 Forbidden`    | User is authenticated but does not have the required role |
| `409 Conflict`     | Username or email already exists                          |

Example:

```text
USER + valid JWT + GET /users
                ↓
             403 Forbidden
```

while:

```text
ADMIN + valid JWT + GET /users
                  ↓
               200 OK
```

---

# Validation

Registration and login requests use Jakarta Bean Validation.

Examples of validation include:

* required username
* required email
* valid email format
* required password

Invalid requests are handled by the global exception handler and return a `400 Bad Request`.

---

# Exception Handling

The service contains a global exception handler for application-level errors.

Currently handled cases include:

* validation errors
* duplicate usernames
* duplicate emails

Example:

```json
{
  "error": "Username already exists"
}
```

---

# Database

The service uses PostgreSQL.

Current database structure contains a `users` table with:

```text
id
username
email
password
role
```

Username and email are unique.

The role is stored according to the application's `Role` enum.

---

# Running the Application

## Requirements

* Java 21
* Maven
* PostgreSQL
* IntelliJ IDEA or another Java IDE

## Database

Create a PostgreSQL database for the authentication service.

Example:

```text
Database: clinic_auth
Port: 5433
```

Configure the database credentials through environment variables.

---

## Environment Variables

The application requires:

```text
JWT_SECRET=<your-jwt-secret>
POSTGRES=<your-postgres-password>
```

Do not commit real secrets to the repository.

---

## Start the application

From the `auth-service` directory:

### Windows

```bash
mvnw.cmd spring-boot:run
```

or run `AuthServiceApplication` from IntelliJ IDEA.

The service runs on:

```text
http://localhost:8081
```

---

# Testing

The service was tested using Postman.

The main scenarios are:

### Registration

```text
POST /auth/register
→ 200 OK
```

### Login

```text
POST /auth/login
→ 200 OK
→ JWT returned in AuthResponse
```

### Authenticated endpoint

```text
GET /users/me
Authorization: Bearer <USER_TOKEN>
→ 200 OK
```

### User attempting admin endpoint

```text
GET /users
Authorization: Bearer <USER_TOKEN>
→ 403 Forbidden
```

### Admin accessing admin endpoint

```text
GET /users
Authorization: Bearer <ADMIN_TOKEN>
→ 200 OK
```

### Invalid/missing authentication

```text
Protected endpoint
without valid JWT
→ 401 Unauthorized
```

---

# Current Status

## Auth Service v1.1

Implemented:

* [x] ADMIN-only doctor promotion endpoint
* [x] Promotion of a `USER` to `DOCTOR`
* [x] Prevention of promoting an existing `DOCTOR`
* [x] Prevention of promoting an `ADMIN` to `DOCTOR`
* [x] `PUT /users/{id}/promote-to-doctor` endpoint
* [x] ADMIN authorization for doctor promotion
* [x] Doctor role persisted in the auth database
* [x] Separation between authentication data and doctor professional data

### Doctor Promotion Flow

An administrator can promote a registered user to a doctor.

```text
USER
  ↓
ADMIN
  ↓
PUT /users/{id}/promote-to-doctor
  ↓
DOCTOR
```

The promotion changes the user's role in `auth-service` from:

```text
USER → DOCTOR
```

Only users with the `ADMIN` role are authorized to perform this operation.

The professional doctor information, such as specialization, license number, experience and studies, will be managed separately by `doctor-service`.

The relationship between the two services will be based on the user's ID:

```text
auth-service
User.id = 15
User.role = DOCTOR

        ↓

doctor-service
Doctor.userId = 15
```

There is no direct database foreign key between the two microservices.



## Auth Service v1.0

Implemented:

* [x] Spring Boot application
* [x] PostgreSQL integration
* [x] User entity
* [x] User repository
* [x] User registration
* [x] BCrypt password hashing
* [x] Login
* [x] JWT generation
* [x] JWT validation
* [x] JWT authentication filter
* [x] AuthResponse
* [x] UserResponse
* [x] Request validation
* [x] Global exception handling
* [x] Role enum
* [x] Role-based authorization
* [x] ADMIN-only user listing
* [x] Environment variables for secrets
* [x] USER vs ADMIN authorization testing

---

## Version

```text
Auth Service
Version: 1.1.0
Status: Stable
```

This version represents the second authentication and authorization milestone of the Clinic Management System.

The authentication service now supports the complete role transition from a regular `USER` to a `DOCTOR` through an administrator-controlled operation.