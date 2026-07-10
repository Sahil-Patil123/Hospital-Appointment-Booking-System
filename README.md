# Hospital Appointment Booking System

A production-style REST API for managing hospital appointments, built with Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA, and MySQL. Supports three roles (ADMIN, DOCTOR, PATIENT), full appointment lifecycle management, doctor scheduling with real-time slot availability, email notifications, and medical records.

## Features

- **JWT Authentication & Role-Based Authorization** — ADMIN, DOCTOR, PATIENT roles with method-level `@PreAuthorize` ownership checks
- **Slot Availability Engine** — computes real bookable time slots from a doctor's weekly schedule minus existing bookings, preventing double-booking
- **Appointment Lifecycle** — enforced state machine: `PENDING → CONFIRMED → COMPLETED` / `CANCELLED`
- **Async Email Notifications** — booking, confirmation, and cancellation emails via `@Async` + JavaMailSender, isolated so failures never break bookings
- **Medical Records** — created only after an appointment reaches `COMPLETED`, one record per appointment
- **Global Exception Handling** — consistent `ApiResponse<T>` JSON shape across every success and error case
- **Pagination, Sorting & Search** — dynamic filtering via JPA Specifications on doctors and appointments
- **Input Validation** — Bean Validation (`@Valid`) at the DTO layer, business-rule validation at the Service layer
- **Swagger/OpenAPI Docs** — interactive API docs with JWT "Authorize" support
- **Test Coverage** — JUnit 5 + Mockito unit tests (Service layer) and MockMvc integration tests (Controller layer)

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Security | Spring Security 7 + JWT (jjwt 0.12.6) |
| Persistence | Spring Data JPA + Hibernate 7 |
| Database | MySQL 8 |
| API Docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito, MockMvc |
| Build Tool | Maven |

## Architecture

Layered architecture throughout: **Controller → Service → Repository**, with DTOs at every API boundary (entities are never exposed directly).

```
src/main/java/com/sahil/hospital_appointment/
├── config/          # Security, Swagger, Async configuration
├── controller/       # REST endpoints
├── dto/              # Request/Response DTOs + ApiResponse/PageResponse wrappers
├── exception/         # Custom exceptions + GlobalExceptionHandler
├── model/             # JPA entities
├── repository/        # Spring Data JPA repositories
├── security/           # JWT utility, filter, UserDetailsService
└── service/             # Business logic + mappers + specifications
```

## Getting Started

### Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- MySQL 8.x running locally

### Setup

1. Clone the repository:
```bash
git clone https://github.com/Sahil-Patil123/hospital-appointment-api.git
cd hospital-appointment-api
```

2. Create the database in MySQL:
```sql
CREATE DATABASE hospital_db;
```

3. Configure your database credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

jwt.secret=YOUR_256_BIT_SECRET_KEY

spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
```
> Gmail requires an [App Password](https://myaccount.google.com/apppasswords) (not your regular password) for SMTP.

4. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

5. Open Swagger UI to explore and test the API interactively:
```
http://localhost:8080/swagger-ui.html
```

### Running Tests

```bash
mvn test
```

Generates a JaCoCo coverage report at `target/site/jacoco/index.html`.

## API Overview

| Category | Example Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Doctors | `GET /api/doctors`, `PUT /api/doctors/{id}`, `GET /api/doctors/search` |
| Patients | `GET /api/patients`, `PUT /api/patients/{id}` |
| Schedules | `POST /api/doctors/{doctorId}/schedules` |
| Slots | `GET /api/doctors/{doctorId}/slots?date=2026-07-15` |
| Appointments | `POST /api/appointments/patients/{patientId}`, `PATCH /api/appointments/{id}/confirm`, `PATCH /api/appointments/{id}/cancel` |
| Medical Records | `POST /api/medical-records`, `GET /api/medical-records/appointment/{appointmentId}` |

Full request/response schemas are available in Swagger UI once the app is running.

## Authentication Flow

1. `POST /api/auth/register` or `/api/auth/login` → returns a JWT in the response body
2. Include the token on all subsequent requests:
```
Authorization: Bearer <token>
```

## Author

**Sahil Patil**
GitHub: [@Sahil-Patil123](https://github.com/Sahil-Patil123)