# CLAUDE.md - Project Context for AI Assistants

## Project Overview

**Project Name:** Spring Gym CRM - Microservices Migration
**Type:** Gym/Fitness Management System
**Status:** Monolith ready for microservices migration
**Java Version:** 21
**Spring Boot Version:** 3.3.13
**Build System:** Maven

## Current Architecture (Monolith)

The application is a Spring Boot REST API with layered architecture:

```
spring-gym-core/
├── model/          # JPA entities (User, Trainee, Trainer, Training, TrainingType)
├── controller/     # REST endpoints
├── service/        # Business logic
├── dao/            # Data access (EntityManager-based)
├── dto/            # Data Transfer Objects
├── security/       # JWT authentication
├── config/         # Spring configurations
├── mapper/         # Entity-DTO mappers
├── exception/      # Custom exceptions & global handler
└── health/         # Custom health indicators
```

### Domain Model

```
User (base entity, JOINED inheritance)
├── Trainee (extends User)
│   ├── dateOfBirth, address
│   ├── ManyToMany → Trainer (trainee_trainer join table)
│   └── OneToMany → Training
└── Trainer (extends User)
    ├── specialization → TrainingType
    ├── ManyToMany → Trainee (inverse)
    └── OneToMany → Training

Training
├── trainee, trainer, trainingType (ManyToOne)
├── trainingName, trainingDate, trainingDuration

TrainingType
└── trainingTypeName
```

### REST API Endpoints

| Path | Description |
|------|-------------|
| POST /api/auth/login | Authentication, returns JWT |
| PUT /api/auth/change-password/{username} | Password update |
| POST /api/auth/logout | Token blacklisting |
| /api/trainees/* | Trainee CRUD, trainer assignment, training history |
| /api/trainers/* | Trainer CRUD, training history |
| POST /api/trainings | Create training session |
| GET /api/training-types | List training types |

### Current Tech Stack

- **Security:** Spring Security + JWT (JJWT 0.13.0)
- **Database:** H2 in-memory (dev), create-drop DDL
- **Monitoring:** Actuator + Prometheus (Micrometer)
- **Documentation:** SpringDoc OpenAPI 2.5.0
- **Testing:** JUnit 5

---

## Microservices Migration Plan

### Target Architecture

```
┌─────────────────┐
│   API Gateway   │  (Spring Cloud Gateway)
└────────┬────────┘
         │
    ┌────┴────┬──────────┬──────────┐
    ▼         ▼          ▼          ▼
┌───────┐ ┌───────┐ ┌─────────┐ ┌──────┐
│Trainee│ │Trainer│ │Training │ │ Auth │
│Service│ │Service│ │ Service │ │Service│
└───────┘ └───────┘ └─────────┘ └──────┘
    │         │          │          │
    └─────────┴──────────┴──────────┘
              │
    ┌─────────┴─────────┐
    │  Service Registry │ (Eureka)
    └───────────────────┘
```

### Proposed Microservices

1. **trainee-service**
   - Trainee entity and CRUD operations
   - Trainer assignment (via Feign to trainer-service)
   - Training history queries (via Feign to training-service)

2. **trainer-service**
   - Trainer entity and CRUD operations
   - Training history queries (via Feign to training-service)

3. **training-service**
   - Training and TrainingType entities
   - Training session management
   - Cross-service data via Feign

4. **auth-service**
   - User authentication and JWT management
   - Token blacklisting
   - Brute-force protection

5. **api-gateway**
   - Routing to microservices
   - JWT validation
   - Rate limiting

6. **eureka-server**
   - Service discovery and registration

### Migration Challenges

- **Cross-service relationships:** Trainee-Trainer ManyToMany needs eventual consistency
- **JPA inheritance:** JOINED strategy needs rethinking for distributed context
- **Distributed transactions:** Consider Saga pattern for multi-service operations
- **Data duplication:** Some data may need to be replicated across services

### Migration Steps (High-Level)

1. Set up Eureka Server for service discovery
2. Create API Gateway with routing configuration
3. Extract Auth Service (least coupled)
4. Extract Training Service (TrainingType + Training)
5. Extract Trainer Service
6. Extract Trainee Service (most complex due to relationships)
7. Add Feign clients for inter-service communication
8. Implement circuit breakers (Resilience4j)
9. Add distributed tracing (Zipkin/Sleuth)

---

## Migration Progress

### Completed

- [ ] Initial codebase analysis
- [ ] Migration plan documented

### In Progress

- [ ] (Nothing currently in progress)

### Next Steps

- [ ] Create eureka-server module
- [ ] Create api-gateway module
- [ ] Set up multi-module Maven project structure

---

## Important Files

| File | Purpose |
|------|---------|
| `spring-gym-core/pom.xml` | Maven build configuration |
| `spring-gym-core/src/main/resources/application.properties` | App configuration |
| `spring-gym-core/src/main/java/com/gym/crm/model/` | JPA entities |
| `spring-gym-core/src/main/java/com/gym/crm/service/` | Business logic |
| `spring-gym-core/src/main/java/com/gym/crm/dao/` | Data access layer |

---

## Development Commands

```bash
# Build the project
cd spring-gym-core
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Access H2 Console (when running)
# http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:gymdb

# Swagger UI
# http://localhost:8080/swagger-ui.html
```

---

## Notes for AI Assistants

- This is an EPAM Java Specialization project (educational context)
- The monolith is fully functional - migration should be incremental
- Prefer Spring Cloud components for microservices infrastructure
- DAO layer uses EntityManager directly - consider Spring Data JPA repositories for microservices
- Current tests are minimal - expand test coverage during migration

---

*Last updated: 2025-12-20*