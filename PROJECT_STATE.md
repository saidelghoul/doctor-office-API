# Doctor's Office Management System

Single-doctor office web app. Backend: Spring Boot. Frontend (later): Angular.
Roles: DOCTOR, ASSISTANT (Doctor inherits all Assistant permissions).
Learning goals: caching and testing (JUnit 5, Mockito, Spring Boot Test).

## Stack and conventions
- Java 21 (pom.xml still says 17, change it), Spring Boot 4.1.2, Maven, Lombok
- Database: H2 in-memory for now (PostgreSQL + Flyway later)
- Config: application.properties (not yml); postgres profile = separate
  application-postgres.properties, commented out for now
- Base package: com.clinic.doctor_office (NOT com.clinic; note the underscore)
  Layers: com.clinic.doctor_office.{domain, dto, repository, service, web, security, exception, config}
  Folder path: src/main/java/com/clinic/doctor_office/...
  Tests mirror it: src/test/java/com/clinic/doctor_office/...
  Always use this package in every package/import line and file path.
- DTOs, JWT auth (jjwt 0.12), Caffeine cache
- Diagrams in docs/ (PlantUML): class-diagram.puml, use-case-diagram.puml
- Git: feature branches, conventional commits

## Done
- Branch feature/initial-structure-and-entities:
  - Layered package structure, empty service/controller skeletons
    (Patient, Appointment, OfficeConfiguration; none for User yet)
  - Enums: Role, Gender, AppointmentStatus
  - Entities: User, Patient, Appointment, OfficeConfiguration, WorkingDay, Holiday
  - Empty JpaRepository per entity (6)
  - Placeholders: PatientDto (empty), PatientNotFoundException
  - application.properties (H2), .gitignore, README, context smoke test
  - mvn test passes on this branch
- Still to re-implement from the old project: patients, appointments, calendar,
  office configuration, doctor dashboard, JWT login, caching, and the old tests
  (SchedulingRulesTest, AppointmentServiceTest, OfficeConfigCachingTest,
  ApiSecurityIntegrationTest, AppointmentFlowIntegrationTest)

## Verified
- `mvn test` passes (smoke test: application context loads with JPA, H2 and security).
  The pom.xml and the entity/repository/skeleton code compile and map correctly on H2.

## Not verified yet
- Nothing else has been tested. Only the context smoke test exists so far.
- The H2 console is not reachable yet (blocked by Spring Security until the config exists).

## In progress
- (nothing)

## Next
1. Set java.version to 21 in pom.xml (small chore)
2. JWT authentication (branch feature/jwt-auth): needs jjwt dependencies, ask first
3. Patients vertical slice (DTOs, validation, service, controller, tests)
4. Office configuration with caching (Caffeine)
5. Appointments (slot validation, status rules, double-booking fix)
6. Actuator, medical records (doctor only), reminders, Angular frontend

## Decisions
- Patient and medical data are never cached
- Appointments keep createdBy (ManyToOne User, lazy) for auditing
- Only DOCTOR can complete an appointment or mark no-show, and edit office config
- User table is named "users" (reserved word); Holiday.date column is holiday_date
- Patient <-> Appointment bidirectional, no cascade
- OfficeConfiguration composes WorkingDay (1..7) and Holiday (0..*): cascade ALL + orphanRemoval,
  unidirectional via join column; the 1..7 bound and "single record" are enforced in the service layer
- Entities use @Getter/@Setter/@Builder (no @Data), no equals/hashCode yet (decide before caching work)
- Enums stored as STRING
- No new dependency is added without asking first

## Known limitations
- Double booking not enforced by the database (race condition)
- ddl-auto=create-drop, seed users and default JWT secret are dev only
- No medical records, reminders, user management or audit log yet