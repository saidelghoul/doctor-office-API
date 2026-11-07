# Doctor's Office Management System: backend PoC

Spring Boot 4.1 / Java 21 / H2 (in-memory) / JWT / Caffeine cache.

## Run

    mvn spring-boot:run          # http://localhost:8080
    mvn test                     # unit + integration tests

Seeded dev users: `doctor / doctor123` (DOCTOR), `assistant / assistant123` (ASSISTANT).
Default office: Mon-Fri 09:00-17:00, break 12:00-13:00, 30-minute appointments.

    TOKEN=$(curl -s localhost:8080/api/auth/login -H 'Content-Type: application/json' \
      -d '{"username":"doctor","password":"doctor123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
    curl -H "Authorization: Bearer $TOKEN" localhost:8080/api/office-config

## API

| Method | Path | Roles |
|---|---|---|
| POST | /api/auth/login | public |
| GET / POST | /api/patients (`?q=&page=&size=`) | both |
| GET / PUT / DELETE(archive) | /api/patients/{id} | both |
| POST | /api/appointments | both |
| GET | /api/appointments?from=YYYY-MM-DD&to=YYYY-MM-DD (calendar) | both |
| GET | /api/appointments/{id} | both |
| PUT | /api/appointments/{id}/reschedule | both |
| POST | /api/appointments/{id}/confirm, /cancel | both |
| POST | /api/appointments/{id}/complete, /no-show | DOCTOR |
| GET | /api/office-config | both |
| PUT | /api/office-config | DOCTOR |
| GET | /api/dashboard/today | DOCTOR |

Errors use RFC 7807 problem details: 400 validation, 401 unauthenticated, 403 wrong role,
404 not found, 409 business-rule violation (double booking, closed day, break, bad status change).

## Design decisions worth knowing

- **Caching**: only the office configuration is cached (`OfficeConfigService`): read on every
  booking, rarely changed. `@Cacheable` on read, `@CachePut` on update. A DTO is cached, never an entity.
  Patient and medical data are deliberately NOT cached (sensitive, must be fresh).
- **Scheduling rules** live in `SchedulingRules`, a pure class with an injected `Clock`, so they are
  tested without Spring or a database.
- **Roles** are declared in one place: `SecurityConfig`.

## Testing layers

| Layer | Class | Tools |
|---|---|---|
| Pure unit | `SchedulingRulesTest` | JUnit 5, parameterized tests, fixed Clock |
| Service unit | `AppointmentServiceTest` | JUnit 5 + Mockito |
| Cache behaviour | `OfficeConfigCachingTest` | @SpringBootTest, CacheManager |
| API + security | `ApiSecurityIntegrationTest` | MockMvc, spring-security-test, real JWT login |
| Full flow | `AppointmentFlowIntegrationTest` | MockMvc + H2, rolled back per test |

## Known PoC limitations (next iterations)

- Double booking is checked in the service but not enforced by the database (race condition between two simultaneous requests).
- H2 in-memory, `ddl-auto=create-drop`: move to PostgreSQL + Flyway + Testcontainers.
- No medical records, reminders, user management, password change, or audit log yet.
- Seed users and the default JWT secret are for development only.
