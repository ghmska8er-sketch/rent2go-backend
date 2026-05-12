# Rent2Go Backend

Rent2Go Backend is the Spring Boot service for the Rent2Go platform. It is built around a DDD-style package structure and organizes the backend by bounded context under the shared package root `app.web.rtgtechnologies.rent2go`.

## Overview

This repository contains the backend API for Rent2Go, including application services, REST interfaces, persistence adapters, security, validation, and OpenAPI documentation support. The main application entry point is `Rent2goBackendApplication`.

## Tech Stack

- Spring Boot 3.5.7
- Java 17
- Maven
- MySQL 8
- Spring Data JPA
- Spring Security
- JWT with JJWT
- OpenAPI / Swagger via springdoc
- Bean Validation
- Lombok

## Repository Structure

```text
rent2go-backend/
├── src/main/java/app/web/rtgtechnologies/rent2go/
│   ├── shared/
│   ├── vehicle_catalog/
│   ├── booking_reservations/
│   ├── iam/
│   ├── payments/
│   └── community_trust/
├── src/main/resources/
├── src/test/java/
├── pom.xml
└── CONFIGURATION_ALIGNMENT.md
```

## Prerequisites

- Java 17
- Maven 3.9+ or the provided Maven wrapper
- MySQL 8
- A configured database for the active profile

## Configuration

The repository uses two main profiles:

- `dev`: local development profile
- `prod`: production profile

### Development Profile

The development profile is intended for local work with a local MySQL instance.

- Default datasource points to `jdbc:mysql://localhost:3306/rent2go_db`
- Default credentials are `root` / `root`
- Logging is more verbose for debugging

Run with:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Production Profile

The production profile reads its connection and security values from environment variables.

Required environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

Example:

```bash
export DB_URL="jdbc:mysql://your-host:3306/rent2go_db"
export DB_USERNAME="your-user"
export DB_PASSWORD="your-password"
export JWT_SECRET="your-secret-key"

java -jar target/rent2go-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## How to Run

From the repository root:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

On Windows, use:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

To build the executable JAR:

```bash
./mvnw clean package
```

On Windows:

```powershell
.\mvnw.cmd clean package
```

Then run it with:

```bash
java -jar target/rent2go-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## How to Test

Run the full test suite with:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

Tests live under `src/test/java` and follow the same package structure as the main code.

## API Documentation

Swagger / OpenAPI is available through springdoc. The configuration exposes the API model with JWT bearer security support, so the generated documentation can be used to inspect and try the REST endpoints.

## Architecture and Bounded Contexts

The backend follows a DDD-oriented structure with separate bounded contexts:

- `vehicle-catalog`
- `booking-reservations`
- `iam`
- `payments`
- `community-trust`

Shared infrastructure and cross-cutting concerns live under `shared`, while each context groups its own domain, application, infrastructure, and REST layers.

## Configuration Notes

See `CONFIGURATION_ALIGNMENT.md` for the detailed profile and environment variable alignment used by this repository.