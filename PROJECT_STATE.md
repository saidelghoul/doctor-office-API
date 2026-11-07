# Doctor's Office Management System

Single-doctor office web app. Backend: Spring Boot. Frontend (later): Angular.
Roles: DOCTOR, ASSISTANT (Doctor inherits all Assistant permissions).
Learning goals: caching and testing (JUnit 5, Mockito, Spring Boot Test).

## Stack and conventions
- Java 21, Spring Boot 4.1.2, Maven, Lombok
- Database: H2 in-memory for now (PostgreSQL + Flyway later)
- Package: com.clinic (domain, dto, repository, service, web, security, exception, config)
- DTOs, JWT auth (jjwt 0.12), Caffeine cache
- Diagrams in docs/ (PlantUML): class-diagram.puml, use-case-diagram.puml

## Done (to be re-implemented in the new project)
- Entities: User, Patient, Appointment, OfficeConfiguration, WorkingDay, Holiday
- Patients, appointments, calendar, office configuration, doctor dashboard
- JWT login and role-based access
- Office configuration cached; patient/medical data deliberately never cached
- Tests: SchedulingRulesTest, AppointmentServiceTest, OfficeConfigCachingTest,
  ApiSecurityIntegrationTest, AppointmentFlowIntegrationTest

## Not verified yet
- pom.xml was rebuilt by hand and the project has NOT been compiled or tested.
  First task: run `mvn test` and fix anything that fails.

## In progress
- (nothing)

## Next
1. Run mvn test, fix build problems
2. Spring Boot Actuator (health, info, metrics, cache stats; secure the endpoints)
3. Review and harden authentication
4. Medical records (doctor only), reminders, then Angular frontend

## Decisions
- Patient and medical data are never cached
- Appointments keep createdBy for auditing
- Only DOCTOR can complete an appointment or mark no-show, and edit office config

## Known limitations
- Double booking not enforced by the database (race condition)
- ddl-auto=create-drop, seed users and default JWT secret are dev only
- No medical records, reminders, user management or audit log yet
