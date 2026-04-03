
## 📖 Overview

Finance Data Processing Backend is a **Spring Boot 3.x** REST API that powers a complete financial dashboard system. It provides secure user authentication, role-based access control, transaction management with dynamic filtering, automated audit logging, and real-time dashboard analytics — all built with production-grade architecture.

### ✨ Key Features

| Feature | Description |
|---------|-------------|
| 🔐 **JWT Authentication** | Stateless auth with access + refresh token rotation |
| 👥 **Role-Based Access** | Three roles (VIEWER, ANALYST, ADMIN) with granular permissions |
| 💳 **Transaction Management** | Full CRUD with soft delete and ownership enforcement |
| 📊 **Dashboard Analytics** | Summary, category breakdown, monthly trends, recent activity |
| 🔍 **Dynamic Filtering** | Filter by type, category, date range, amount range with pagination |
| 📝 **Audit Logging** | Automatic AOP-driven logging of all write operations |
| 🛡️ **Global Error Handling** | Consistent error responses with validation details |
| 📄 **API Documentation** | Interactive Swagger UI with JWT authentication support |

---

## 🏗️ Architecture

```
com.finance.backend
│
├── 🎮 controller/          # REST endpoints (thin HTTP layer)
│   ├── AuthController       # Register, login, refresh, logout
│   ├── UserController       # User CRUD (ADMIN only)
│   ├── TransactionController# Transaction CRUD with filters
│   ├── DashboardController  # Analytics & summaries
│   └── AuditLogController   # Audit trail (ADMIN only)
│
├── 🔧 service/              # Business logic + @PreAuthorize RBAC
├── 📦 repository/           # Spring Data JPA + custom queries
├── 🗃️ entity/               # JPA entities with audit timestamps
├── 📋 dto/                  # Request/Response DTOs with validation
│   ├── request/             # Input DTOs (jakarta.validation)
│   └── response/            # Output DTOs (no entity exposure)
│
├── 🔒 security/             # JWT filter, config, user details
├── ⚠️ exception/            # Global handler + custom exceptions
├── 🔎 specification/        # JPA Specifications for dynamic queries
├── 📐 aspect/               # AOP audit logging
├── ⚙️ config/               # OpenAPI, ModelMapper, DataSeeder
└── 📊 enums/                # Role, TransactionType
```

---

## 🛡️ Role Permissions

| Action | VIEWER | ANALYST | ADMIN |
|--------|:------:|:-------:|:-----:|
| View transactions | ✅ | ✅ | ✅ |
| View dashboard | ✅ | ✅ | ✅ |
| Create transactions | ❌ | ✅ | ✅ |
| Edit own transactions | ❌ | ✅ | ✅ |
| Edit any transaction | ❌ | ❌ | ✅ |
| Delete own transactions | ❌ | ✅ | ✅ |
| Delete any transaction | ❌ | ❌ | ✅ |
| Manage users | ❌ | ❌ | ✅ |
| View audit logs | ❌ | ❌ | ✅ |

---

## 🚀 Quick Start

### Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+**
- **PostgreSQL 14+** (production) or none (dev profile uses H2)

### 1️⃣ Clone & Configure

```bash
git clone https://github.com/your-username/Finance-Data-Processing-Backend.git
cd Finance-Data-Processing-Backend/FDP
```

### 2️⃣ Database Setup

**Option A — PostgreSQL (Production)**
```sql
CREATE DATABASE finance_db;
```

Update `src/main/resources/application.properties`:
```properties
spring.datasource.username=postgres
spring.datasource.password=your_password
```

**Option B — H2 In-Memory (Zero Setup)**
```properties
# Uncomment this line in application.properties:
spring.profiles.active=dev
```

### 3️⃣ Run

```bash
mvn spring-boot:run
```

The app starts at **http://localhost:8080** 🎉

### 4️⃣ Explore the API

| Resource | URL |
|----------|-----|
| 🟢 Swagger UI | http://localhost:8080/swagger-ui.html |
| 📄 API Docs (JSON) | http://localhost:8080/v3/api-docs |
| 🗄️ H2 Console (dev only) | http://localhost:8080/h2-console |

---

## 🔑 Authentication Flow

```
┌──────────┐    POST /api/auth/register     ┌──────────┐
│          │ ─────────────────────────────▶  │          │
│  Client  │    { name, email, password }    │  Server  │
│          │ ◀─────────────────────────────  │          │
└──────────┘    { accessToken,               └──────────┘
                  refreshToken, user }

┌──────────┐    POST /api/auth/login         ┌──────────┐
│          │ ─────────────────────────────▶  │          │
│  Client  │    { email, password }          │  Server  │
│          │ ◀─────────────────────────────  │          │
└──────────┘    { accessToken,               └──────────┘
                  refreshToken, user }

  All subsequent requests:
  Authorization: Bearer <accessToken>

  Token expired? Use refresh token rotation:
  POST /api/auth/refresh-token { refreshToken }
  → New accessToken + new refreshToken (old one revoked)
```

---

## 📡 API Reference

### Auth Endpoints
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/auth/register` | Register new user | ❌ |
| `POST` | `/api/auth/login` | Login & get tokens | ❌ |
| `POST` | `/api/auth/refresh-token` | Rotate refresh token | ❌ |
| `POST` | `/api/auth/logout` | Revoke all tokens | ✅ |

### Transaction Endpoints
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/transactions` | List with filters & pagination | ✅ |
| `GET` | `/api/transactions/{id}` | Get by ID | ✅ |
| `POST` | `/api/transactions` | Create (ANALYST/ADMIN) | ✅ |
| `PUT` | `/api/transactions/{id}` | Update (owner/ADMIN) | ✅ |
| `DELETE` | `/api/transactions/{id}` | Soft delete (owner/ADMIN) | ✅ |

**Supported Filters:** `type`, `category`, `dateFrom`, `dateTo`, `minAmount`, `maxAmount`, `page`, `size`, `sort`

### Dashboard Endpoints
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/dashboard/summary` | Income, expenses, balance | ✅ |
| `GET` | `/api/dashboard/by-category` | Category breakdown | ✅ |
| `GET` | `/api/dashboard/monthly-trend` | 12-month trend by year | ✅ |
| `GET` | `/api/dashboard/recent` | Recent transactions | ✅ |

### User Management (ADMIN Only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/users` | List all users |
| `GET` | `/api/users/{id}` | Get user by ID |
| `PUT` | `/api/users/{id}` | Update user name |
| `PATCH` | `/api/users/{id}/role` | Change role |
| `PATCH` | `/api/users/{id}/status` | Activate/deactivate |
| `DELETE` | `/api/users/{id}` | Deactivate user |

### Audit Logs (ADMIN Only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/audit-logs` | Paginated audit trail |

---

## 🧪 Sample Requests

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "John@1234"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "John@1234"
  }'
```

### Create Transaction
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d '{
    "amount": 75000.00,
    "type": "INCOME",
    "category": "Salary",
    "date": "2026-04-01",
    "notes": "April salary"
  }'
```

### Get Transactions with Filters
```bash
curl "http://localhost:8080/api/transactions?type=EXPENSE&minAmount=5000&maxAmount=20000&page=0&size=5" \
  -H "Authorization: Bearer <access_token>"
```

### Dashboard Summary
```bash
curl http://localhost:8080/api/dashboard/summary \
  -H "Authorization: Bearer <access_token>"
```

### Monthly Trend
```bash
curl "http://localhost:8080/api/dashboard/monthly-trend?year=2026" \
  -H "Authorization: Bearer <access_token>"
```

---

## 🗂️ Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Language** | Java 21 | Modern LTS with records, pattern matching |
| **Framework** | Spring Boot 3.5 | Auto-configuration, embedded Tomcat |
| **Security** | Spring Security 6 + JWT | Stateless authentication |
| **ORM** | Spring Data JPA + Hibernate 6 | Database abstraction |
| **Database** | PostgreSQL 16 | Production database |
| **Dev Database** | H2 | In-memory for local dev |
| **Validation** | Jakarta Validation | Request body validation |
| **Documentation** | Springdoc OpenAPI 3 (v2.8) | Swagger UI |
| **Utilities** | Lombok, ModelMapper | Boilerplate reduction |
| **AOP** | Spring AOP | Cross-cutting audit logging |
| **Build** | Maven | Dependency management |
| **Testing** | JUnit 5 + MockMvc | Integration tests |

---

## ⚙️ Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | `postgres` | PostgreSQL username |
| `DB_PASSWORD` | — | PostgreSQL password |
| `JWT_SECRET` | (built-in) | 256-bit HMAC key for JWT signing |

### Profiles

| Profile | Database | Data Seeder | Use Case |
|---------|----------|-------------|----------|
| *(default)* | PostgreSQL | ❌ | Production |
| `dev` | H2 in-memory | ✅ (3 users + 20 transactions) | Local development |

---

## 🏛️ Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Soft delete for transactions** | Financial data must be recoverable and auditable — never hard-deleted |
| **BCrypt-hashed refresh tokens** | Prevents token theft even if database is compromised |
| **Refresh token rotation** | Each refresh revokes the old token — prevents replay attacks |
| **Service-level RBAC** | `@PreAuthorize` on service methods keeps security decoupled from HTTP concerns |
| **AOP audit logging** | Zero-intrusion — service classes stay clean, auditing is automatic |
| **JPA Specifications** | Composable query filters scale cleanly without repository method explosion |
| **DTO pattern** | Entities are never exposed in API responses — prevents data leaks |
| **`@SQLRestriction`** | Automatically filters soft-deleted records at the Hibernate level |

---

## 🧪 Testing

```bash
# Run all integration tests
mvn test

# Run with dev profile
mvn test -Dspring.profiles.active=dev

# Run a specific test class
mvn test -Dtest=AuthControllerTest
```

### Test Coverage

| Module | Tests | Covers |
|--------|-------|--------|
| Auth | 4 | Register, login, validation, duplicates |
| Transactions | 4 | CRUD, RBAC, soft delete |
| Dashboard | 2 | Summary, monthly trend |

---

## 📁 Project Structure

```
FDP/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/finance/backend/
│   │   │   ├── FinanceBackendApplication.java
│   │   │   ├── aspect/
│   │   │   │   └── AuditLoggingAspect.java
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java
│   │   │   │   ├── DataSeeder.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AuditLogController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateTransactionRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RefreshTokenRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   ├── UpdateTransactionRequest.java
│   │   │   │   │   └── UpdateUserRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ApiErrorResponse.java
│   │   │   │       ├── AuthResponse.java
│   │   │   │       ├── CategorySummaryResponse.java
│   │   │   │       ├── DashboardSummaryResponse.java
│   │   │   │       ├── MonthlyTrendResponse.java
│   │   │   │       ├── TransactionResponse.java
│   │   │   │       └── UserResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── AuditLog.java
│   │   │   │   ├── RefreshToken.java
│   │   │   │   ├── Transaction.java
│   │   │   │   └── User.java
│   │   │   ├── enums/
│   │   │   │   ├── Role.java
│   │   │   │   └── TransactionType.java
│   │   │   ├── exception/
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── UnauthorizedAccessException.java
│   │   │   ├── repository/
│   │   │   │   ├── AuditLogRepository.java
│   │   │   │   ├── RefreshTokenRepository.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   ├── JwtUtil.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── service/
│   │   │   │   ├── AuditLogService.java
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── DashboardService.java
│   │   │   │   ├── TransactionService.java
│   │   │   │   └── UserService.java
│   │   │   └── specification/
│   │   │       └── TransactionSpecification.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
│       ├── java/com/finance/backend/
│       │   ├── controller/
│       │   │   ├── AuthControllerTest.java
│       │   │   ├── DashboardControllerTest.java
│       │   │   └── TransactionControllerTest.java
│       │   └── util/
│       │       └── TestUtils.java
│       └── resources/
│           └── application.properties
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Built with ❤️ using <strong>Spring Boot</strong> & <strong>Java 21</strong>
</p>
