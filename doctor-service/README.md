# Doctor Service

Doctor management microservice for the Clinic Management System.

This service is responsible for managing doctors and their professional information, including professional profiles, work experience, and education history.

**Version:** 1.0
**Status:** Completed

---

## Overview

The `doctor-service` is an independent Spring Boot microservice responsible for managing the professional side of doctor accounts.

The service stores and manages:

* Doctor professional profiles
* Professional experiences
* Educational studies

Authentication and user identity are owned by the separate `auth-service`.

The two services use separate databases and do not share JPA entities or database foreign keys.

Instead, the doctor service stores the `userId` provided by the authentication service as a logical reference.

```text
auth-service                         doctor-service

User                                  Doctor
--------------------------------      --------------------------------
id = 15                               id = 4
username = doctor1                    userId = 15
role = DOCTOR                         firstName = John
                                      specialization = Cardiology
```

---

## Features

### Doctor Management

The service provides full CRUD operations for doctor profiles:

* Create doctor profile
* Get all doctors
* Get doctor by ID
* Get doctor by user ID
* Update doctor profile
* Delete doctor profile

### Experience Management

Doctors can manage their professional experience:

* Add experience
* View experiences
* Update experience
* Delete experience

### Study Management

Doctors can manage their education:

* Add study
* View studies
* Update study
* Delete study

### Validation

Request validation is implemented using Jakarta Bean Validation.

Examples include:

* Required fields
* Valid year ranges
* Valid phone number format
* Non-empty strings

### Authorization

The service implements role-based authorization using JWT.

Supported roles:

* `USER`
* `DOCTOR`
* `ADMIN`

Access is additionally protected by ownership checks.

A doctor can modify only their own experiences and studies.

Administrators have full access to doctor data.

### Service-to-Service Communication

The doctor service communicates with `auth-service` using OpenFeign.

When creating a doctor profile, the service verifies that:

1. The referenced user exists.
2. The user has the `DOCTOR` role.
3. A doctor profile does not already exist for that user.
4. The license number is not already in use.

Communication between the two services is protected using an internal API key.

---

## Technology Stack

| Technology         | Purpose                          |
| ------------------ | -------------------------------- |
| Java 21            | Programming language             |
| Spring Boot 4.1.1  | Application framework            |
| Spring Web MVC     | REST API                         |
| Spring Data JPA    | Persistence                      |
| Spring Security    | Authentication and authorization |
| JWT                | User authentication              |
| OpenFeign          | Service-to-service communication |
| PostgreSQL         | Database                         |
| Jakarta Validation | Request validation               |
| Maven              | Build and dependency management  |

The service currently uses Spring Cloud `2025.1.2` dependency management for OpenFeign.

---

## Architecture

The doctor service follows a layered architecture:

```text
HTTP Request
     │
     ▼
Controller
     │
     ▼
Service
     │
     ├──────────────► Auth Service
     │                 (OpenFeign)
     │
     ▼
Repository
     │
     ▼
PostgreSQL
```

### Main layers

**Controller**

Handles HTTP requests and responses.

**Service**

Contains business logic, validation, ownership checks, and communication with `auth-service`.

**Repository**

Provides database access using Spring Data JPA.

**Entity**

Represents persisted doctor, experience, and study data.

**DTO**

Separates API request/response models from persistence entities.

**Security**

Validates JWT tokens and enforces role-based and ownership-based access.

---

## Domain Model

### Doctor

```text
Doctor
├── id
├── userId
├── firstName
├── lastName
├── specialization
├── licenseNumber
├── phone
├── experiences
└── studies
```

The `userId` is a logical reference to a user owned by `auth-service`.

It is not a JPA relationship and does not create a database foreign key.

### Experience

```text
Experience
├── id
├── startYear
├── endYear
├── position
├── hospital
├── location
└── doctor
```

`endYear` can be null for a current position.

### Study

```text
Study
├── id
├── startYear
├── endYear
├── degree
├── university
├── field
└── doctor
```

`endYear` can be null for an ongoing study.

---

# API Documentation

Base URL:

```text
http://localhost:8082
```

All protected endpoints require authentication.

```http
Authorization: Bearer <JWT>
```

---

## Doctor Endpoints

### Create Doctor

```http
POST /doctors
```

**Required role:** `ADMIN`

The `userId` must belong to an existing user with the `DOCTOR` role.

### Request

```json
{
  "userId": 15,
  "firstName": "John",
  "lastName": "Doe",
  "specialization": "Cardiology",
  "licenseNumber": "DOC-TEST-001",
  "phone": "0712345678"
}
```

### Response

```http
201 Created
```

```json
{
  "id": 4,
  "userId": 15,
  "firstName": "John",
  "lastName": "Doe",
  "specialization": "Cardiology",
  "licenseNumber": "DOC-TEST-001",
  "phone": "0712345678"
}
```

---

### Get All Doctors

```http
GET /doctors
```

**Required role:** `USER`, `DOCTOR`, or `ADMIN`

Returns all doctor profiles.

---

### Get Doctor by ID

```http
GET /doctors/{id}
```

**Required role:** `USER`, `DOCTOR`, or `ADMIN`

Example:

```http
GET /doctors/4
```

---

### Get Doctor by User ID

```http
GET /doctors/user/{userId}
```

**Required role:** `USER`, `DOCTOR`, or `ADMIN`

Example:

```http
GET /doctors/user/15
```

---

### Update Doctor

```http
PUT /doctors/{id}
```

**Required role:** `ADMIN`

Example:

```http
PUT /doctors/4
```

```json
{
  "userId": 15,
  "firstName": "John",
  "lastName": "Doe",
  "specialization": "Neurology",
  "licenseNumber": "DOC-TEST-001",
  "phone": "0712345678"
}
```

The existing doctor's `userId` remains associated with the doctor profile.

---

### Delete Doctor

```http
DELETE /doctors/{id}
```

**Required role:** `ADMIN`

Example:

```http
DELETE /doctors/4
```

### Response

```http
204 No Content
```

---

# Experience Endpoints

Base path:

```text
/doctors/{doctorId}/experiences
```

### Get Experiences

```http
GET /doctors/{doctorId}/experiences
```

**Required role:** `USER`, `DOCTOR`, or `ADMIN`

---

### Add Experience

```http
POST /doctors/{doctorId}/experiences
```

**Required role:** `DOCTOR` or `ADMIN`

### Request

```json
{
  "startYear": 2020,
  "endYear": 2022,
  "position": "Resident Doctor",
  "hospital": "City Hospital",
  "location": "Timisoara"
}
```

### Response

```http
201 Created
```

---

### Update Experience

```http
PUT /doctors/{doctorId}/experiences/{experienceId}
```

**Required role:** `DOCTOR` or `ADMIN`

A `DOCTOR` can update only experience belonging to their own doctor profile.

---

### Delete Experience

```http
DELETE /doctors/{doctorId}/experiences/{experienceId}
```

**Required role:** `DOCTOR` or `ADMIN`

A `DOCTOR` can delete only experience belonging to their own doctor profile.

---

# Study Endpoints

Base path:

```text
/doctors/{doctorId}/studies
```

### Get Studies

```http
GET /doctors/{doctorId}/studies
```

**Required role:** `USER`, `DOCTOR`, or `ADMIN`

---

### Add Study

```http
POST /doctors/{doctorId}/studies
```

**Required role:** `DOCTOR` or `ADMIN`

### Request

```json
{
  "startYear": 2014,
  "endYear": 2020,
  "degree": "Doctor of Medicine",
  "university": "University of Medicine",
  "field": "Cardiology"
}
```

### Response

```http
201 Created
```

---

### Update Study

```http
PUT /doctors/{doctorId}/studies/{studyId}
```

**Required role:** `DOCTOR` or `ADMIN`

A `DOCTOR` can update only studies belonging to their own doctor profile.

---

### Delete Study

```http
DELETE /doctors/{doctorId}/studies/{studyId}
```

**Required role:** `DOCTOR` or `ADMIN`

A `DOCTOR` can delete only studies belonging to their own doctor profile.

---

# Authorization Matrix

| Operation                          | USER | DOCTOR | ADMIN |
| ---------------------------------- | :--: | :----: | :---: |
| View doctors                       |   ✅  |    ✅   |   ✅   |
| Create doctor profile              |   ❌  |    ❌   |   ✅   |
| Update doctor profile              |   ❌  |    ❌   |   ✅   |
| Delete doctor profile              |   ❌  |    ❌   |   ✅   |
| View experiences                   |   ✅  |    ✅   |   ✅   |
| Add own experience                 |   ❌  |    ✅   |   ✅   |
| Update own experience              |   ❌  |    ✅   |   ✅   |
| Delete own experience              |   ❌  |    ✅   |   ✅   |
| Modify another doctor's experience |   ❌  |    ❌   |   ✅   |
| View studies                       |   ✅  |    ✅   |   ✅   |
| Add own study                      |   ❌  |    ✅   |   ✅   |
| Update own study                   |   ❌  |    ✅   |   ✅   |
| Delete own study                   |   ❌  |    ✅   |   ✅   |
| Modify another doctor's study      |   ❌  |    ❌   |   ✅   |

---

# Authentication

The service uses JWT-based authentication.

The JWT is generated by `auth-service` and contains information including:

```json
{
  "sub": "doctor1",
  "userId": 15,
  "role": "ROLE_DOCTOR"
}
```

The doctor service validates the JWT locally.

The `userId` from the token is used for ownership checks.

For example:

```text
JWT
 │
 ├── userId = 15
 └── role = ROLE_DOCTOR
          │
          ▼
Doctor Service
          │
          ▼
Find Doctor where userId = 15
          │
          ▼
Compare with requested doctorId
```

This prevents a doctor from modifying another doctor's professional data.

---

# Service-to-Service Authentication

The doctor service communicates with `auth-service` using OpenFeign.

For example, when creating a doctor:

```text
POST /doctors
       │
       ▼
Doctor Service
       │
       │ GET /users/{userId}
       │ X-Service-Key: <internal-key>
       ▼
Auth Service
       │
       ├── User exists?
       │
       └── Role == DOCTOR?
       │
       ▼
Doctor Service
       │
       ▼
Create Doctor
```

The internal API key is provided through an environment variable:

```text
AUTH_SERVICE_API_KEY
```

The key must not be committed to source control.

The service configuration references the environment variable:

```yaml
services:
  auth:
    url: http://localhost:8081
    api-key: ${AUTH_SERVICE_API_KEY}
```

---

# Validation and Error Handling

The service uses validation annotations on request DTOs.

For example, doctor creation requires:

* `userId`
* `firstName`
* `lastName`
* `specialization`
* `licenseNumber`
* `phone`

Phone numbers are restricted to digits and common phone-number characters.

Experience and study years must be between `1900` and `2100`.

### Common HTTP responses

| Status             | Meaning                                      |
| ------------------ | -------------------------------------------- |
| `201 Created`      | Resource successfully created                |
| `200 OK`           | Request successfully completed               |
| `204 No Content`   | Resource successfully deleted                |
| `400 Bad Request`  | Validation error                             |
| `401 Unauthorized` | Missing or invalid authentication            |
| `403 Forbidden`    | User does not have permission                |
| `404 Not Found`    | Requested resource does not exist            |
| `409 Conflict`     | Business rule conflict or duplicate resource |

Examples of business errors:

```json
{
  "error": "Doctor profile already exists for this user"
}
```

```json
{
  "error": "User must have DOCTOR role"
}
```

```json
{
  "error": "User not found with id: 5"
}
```

```json
{
  "error": "You are not allowed to modify this doctor's data"
}
```

---

# Database

The service uses PostgreSQL.

Default local configuration:

```text
Host: localhost
Port: 5433
Database: clinic_doctor
Username: clinic_doctor_user
```

The database password is supplied through:

```text
POSTGRES
```

The service uses Spring Data JPA and Hibernate.

Current development configuration:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

SQL logging is enabled during development.

---

# Configuration

The service runs on:

```text
http://localhost:8082
```

Configuration example:

```yaml
spring:
  application:
    name: doctor-service

  datasource:
    url: jdbc:postgresql://localhost:5433/clinic_doctor
    username: clinic_doctor_user
    password: ${POSTGRES}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8082

jwt:
  secret: ${JWT_SECRET}

services:
  auth:
    url: http://localhost:8081
    api-key: ${AUTH_SERVICE_API_KEY}
```

The current repository configuration uses environment variables for sensitive values instead of storing secrets directly in the configuration file.

---

# Running the Service

## Prerequisites

Make sure the following are installed:

* Java 21
* Maven
* PostgreSQL
* `auth-service`

The project includes Maven Wrapper scripts:

```text
mvnw
mvnw.cmd
```

---

## Required Environment Variables

Before starting the service, configure:

```text
POSTGRES=<database-password>
JWT_SECRET=<jwt-secret>
AUTH_SERVICE_API_KEY=<internal-service-api-key>
```

The `JWT_SECRET` must be compatible with the secret used by `auth-service`, because the doctor service validates JWTs generated by the authentication service.

---

## Start the Service

Using IntelliJ IDEA:

1. Configure the required environment variables in the Run Configuration.
2. Make sure PostgreSQL is running.
3. Make sure `auth-service` is running on port `8081`.
4. Start `DoctorServiceApplication`.

The application will start on:

```text
http://localhost:8082
```

The application enables OpenFeign clients through `@EnableFeignClients`.

---

# Project Structure

```text
doctor-service/
├── .mvn/
│   └── wrapper/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── clinic/
│   │   │           └── doctor/
│   │   │               ├── client/
│   │   │               ├── config/
│   │   │               ├── controller/
│   │   │               ├── dto/
│   │   │               ├── entity/
│   │   │               ├── exception/
│   │   │               ├── repository/
│   │   │               ├── security/
│   │   │               ├── service/
│   │   │               └── DoctorServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── com/
│               └── clinic/
│                   └── doctor/
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

---

# Security Design

The service uses two different authentication concepts.

### User authentication

Used when a client calls the doctor service.

```text
Client
  │
  │ Authorization: Bearer JWT
  ▼
Doctor Service
  │
  ▼
JWT Authentication Filter
  │
  ├── userId
  └── role
```

### Internal service authentication

Used when the doctor service calls the authentication service.

```text
Doctor Service
  │
  │ X-Service-Key
  ▼
Auth Service
  │
  ▼
Service Authentication
```

The user's JWT is not forwarded automatically to `auth-service`.

The internal service API key identifies the trusted service-to-service request.

---

# Business Rules

The doctor service enforces the following rules:

### Doctor creation

A doctor profile can be created only when:

```text
User exists
     AND
User role == DOCTOR
     AND
No existing Doctor profile for user
     AND
License number is unique
```

### Doctor profile management

Only administrators can:

* Create doctor profiles
* Update doctor profiles
* Delete doctor profiles

### Professional information

A doctor can:

* Create their own experiences
* Update their own experiences
* Delete their own experiences
* Create their own studies
* Update their own studies
* Delete their own studies

An administrator can manage professional information for any doctor.

---

# Testing

The service has been manually tested for:

* Successful doctor creation
* User existence validation
* Doctor role validation
* Duplicate doctor prevention
* Duplicate license prevention
* Doctor retrieval
* Experience creation
* Experience update
* Experience deletion
* Study creation
* Study update
* Study deletion
* JWT authentication
* Role-based authorization
* Ownership validation
* Administrator access
* Unauthorized requests
* Invalid requests
* Validation errors
* Service-to-service authentication

Ownership testing confirms that a doctor cannot modify another doctor's professional information.

---

# Version 1.0

Version 1.0 represents the first completed version of the doctor microservice.

### Included in v1.0

* Doctor CRUD
* Experience CRUD
* Study CRUD
* DTO-based API
* Jakarta validation
* PostgreSQL persistence
* JWT authentication
* Role-based authorization
* Ownership-based authorization
* ADMIN override
* OpenFeign integration with `auth-service`
* Internal service API-key authentication
* Global exception handling
* Business rule validation
* Separate service database

---

# Related Service

The `doctor-service` depends on the authentication functionality provided by:

```text
auth-service
```

The authentication service owns:

* User accounts
* Passwords
* Roles
* Login
* JWT generation

The doctor service owns:

* Doctor professional profiles
* Experiences
* Studies

This separation keeps authentication and professional doctor data within their respective microservices.
