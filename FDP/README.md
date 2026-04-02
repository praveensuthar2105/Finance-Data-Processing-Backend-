# Finance Dashboard Backend

A production-structured REST API backend for a Finance Dashboard system built with **Spring Boot 3.x** and **Java 21**. The system manages financial transactions, users with role-based access control (VIEWER, ANALYST, ADMIN), and provides dashboard analytics — all secured with JWT-based authentication.

## Tech Stack

- **Java 21** — Language
- **Spring Boot 3.x** — Framework
- **Spring Security 6** — JWT-based authentication & authorization
- **Spring Data JPA + Hibernate** — ORM & data access
- **PostgreSQL** — Primary database (production)
- **H2** — In-memory database (dev/test profile)
- **Lombok** — Boilerplate reduction
- **ModelMapper** — Entity ↔ DTO conversion
- **Springdoc OpenAPI 3** — Swagger UI documentation
- **Spring Validation** — Input validation (jakarta.validation)
- **Spring AOP** — Automatic audit logging
- **JUnit 5 + MockMvc** — Integration tests
- **Maven** — Build tool

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- PostgreSQL 14+ (only if running production profile)
- *No PostgreSQL needed for dev profile — uses H2 in-memory*

## Setup & Run

### Clone the repository
```bash
git clone <repository-url>
cd FDP
```

### Run in Dev Mode (H2 — zero dependencies)
```bash
mvn spring-boot:run
```
The `dev` profile is active by default, using H2 in-memory database. No external database needed.

### Run with PostgreSQL (Production)
```bash
# Set environment variables
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-256-bit-secret-key

# Update active profile in application.properties
# Change spring.profiles.active=dev to spring.profiles.active=prod

mvn spring-boot:run
```

### Run Tests
```bash
mvn test
```

## API Documentation

Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```

H2 Console (dev profile only):
```
http://localhost:8080/h2-console
```
- JDBC URL: `jdbc:h2:mem:financedb`
- Username: `sa`
- Password: *(empty)*

## Default Credentials

| Role    | Email               | Password     |
|---------|---------------------|--------------|
| ADMIN   | admin@finance.com   | Admin@123    |
| ANALYST | analyst@finance.com | Analyst@123  |
| VIEWER  | viewer@finance.com  | Viewer@123   |

*These are seeded automatically in dev profile only.*

## Role Permissions

| Action                     | VIEWER | ANALYST   | ADMIN |
|----------------------------|--------|-----------|-------|
| GET /transactions          | ✓      | ✓         | ✓     |
| GET /dashboard/**          | ✓      | ✓         | ✓     |
| POST /transactions         | ✗      | ✓         | ✓     |
| PUT /transactions/{id}     | ✗      | own only  | ✓     |
| DELETE /transactions/{id}  | ✗      | own only  | ✓     |
| GET /users                 | ✗      | ✗         | ✓     |
| PUT /users/{id}            | ✗      | ✗         | ✓     |
| PATCH /users/{id}/role     | ✗      | ✗         | ✓     |
| PATCH /users/{id}/status   | ✗      | ✗         | ✓     |
| GET /audit-logs            | ✗      | ✗         | ✓     |

## Assumptions & Tradeoffs

1. **Soft delete for transactions** — Used soft delete (`is_deleted` flag) instead of hard delete because financial data should be recoverable and auditable.

2. **Refresh tokens stored as BCrypt hashes** — Not plaintext, to prevent token theft from database breaches. Trade-off: token lookup requires iterating and comparing hashes.

3. **AOP-based audit logging** — Chosen over manual logging in each service to keep business logic clean and auditing consistent across all operations.

4. **H2 in-memory for dev profile** — The project runs with zero external dependencies — just `mvn spring-boot:run`. Makes onboarding and testing frictionless.

5. **ANALYST can only modify their own transactions** — Simulates a real multi-user finance system where users own their data. ADMIN can override this restriction.

6. **JPA Specification for filtering** — Used composable `Specification<Transaction>` instead of multiple repository method variants. This scales cleanly as filter criteria grow.

## Folder Structure

```
com.finance.backend
├── config/              # AppConfig, OpenApiConfig, DataSeeder
├── controller/          # REST controllers (thin HTTP layer)
├── dto/
│   ├── request/         # Input DTOs with validation
│   └── response/        # Output DTOs (never expose entities)
├── entity/              # JPA entities
├── enums/               # Role, TransactionType
├── exception/           # Custom exceptions + GlobalExceptionHandler
├── repository/          # Spring Data JPA repositories
├── security/            # JWT utilities, filters, SecurityConfig
├── service/             # Business logic with @PreAuthorize RBAC
├── specification/       # JPA Specifications for dynamic queries
├── aspect/              # AOP audit logging aspect
└── FinanceBackendApplication.java
```

## Sample cURL Commands

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","password":"Password123"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@finance.com","password":"Admin@123"}'
```

### Create Transaction (requires ANALYST or ADMIN token)
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d '{"amount":1500.00,"type":"INCOME","category":"Freelance","date":"2024-06-15","notes":"Project work"}'
```

### Get Dashboard Summary
```bash
curl -X GET http://localhost:8080/api/dashboard/summary \
  -H "Authorization: Bearer <access_token>"
```

### Get Monthly Trend
```bash
curl -X GET "http://localhost:8080/api/dashboard/monthly-trend?year=2024" \
  -H "Authorization: Bearer <access_token>"
```

### Get Transactions with Filters
```bash
curl -X GET "http://localhost:8080/api/transactions?type=EXPENSE&category=Food&page=0&size=5" \
  -H "Authorization: Bearer <access_token>"
```
