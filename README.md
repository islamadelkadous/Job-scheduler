# Job Scheduler Service

A Spring Boot-based job scheduler service that allows users to create, manage, and schedule jobs with cron or fixed-rate schedules. This project includes features like JWT-based authentication, role-based access control, idempotency, and job scheduling with persistence.

## Features

- **Job Scheduling**:
    - Supports two schedule types:
        - Cron expressions (e.g., `0 0/5 * * * *` for every 5 minutes).
        - Fixed-rate schedules (e.g., every `n` milliseconds).
    - Jobs persist in a Postgresql database and survive service restarts.
    - Configurable missed execution strategy (`catch-up` or `skip`).

- **Job Management**:
    - Create, update, and retrieve jobs via REST API.
    - Idempotent job creation: prevents duplicate jobs based on schedule type and the cron expression or fixed rate value.
    - Schedule jobs having status (e.g., `PENDING`, `RUNNING`).

- **Security**:
    - JWT-based authentication for API endpoints.
    - Role-based access control: (To Be Implemented in the future)

- **Error Handling**:
    - Custom exceptions with consistent JSON error responses.
    - Global exception handling for all API endpoints.

## Prerequisites

- **Java 17** or later
- **Maven 3.6+**
- **MySQL 8.0+**
- **Flyway 10.20.0+**
- **Git** (for cloning the repository)
- **cURL** or **Postman** (for testing API endpoints)

## Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd scheduler_service
```

### 2. Configure the Database
- Ensure MySQL is running on your local machine.
- Create a database named `scheduler_db`:
  ```sql
  CREATE DATABASE scheduler_db;
  ```
- Update the database configuration in `src/main/resources/application.properties` if needed:
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/scheduler_db?useSSL=false
  spring.datasource.username=root
  spring.datasource.password=yourpassword
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
  ```

### 3. Configure Application Properties
Edit `src/main/resources/application.properties` to set the JWT secret and other configurations:
```properties
# JWT configuration
jwt.secret=yourSecretKeyForSigningJWTs1234567890
jwt.expirationMs=86400000

# Flyway configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration

# Scheduler configuration
# Options: catch-up, skip
scheduler.missed-execution-strategy=catch-up
scheduler.check-and-execute-rate=60000
executer.job-execution-time=5000
```

### 4. Build and Run the Application
```bash
mvn clean install
mvn spring-boot:run
```
- The application will start on `http://localhost:8080`.

### 5. Database Initialization
- The application uses Flyway for database migrations. On startup, Flyway will apply the migration scripts in `src/main/resources/db/migration` to create the `jobs` and `users` table.
- Test users are preloaded:
    - `admin` (password: `adminadmin`, role: `ROLE_ADMIN`)
    - `user` (password: `adminadmin`, role: `ROLE_USER`)

### 6. Download Postman Collection
- Download the Postman collection File in the project directory name : `Job Scheduler Project.postman_collection.json`.
- Import The collection in Postman and Start testing the Project.

## API Documentation

### Authentication
All endpoints (except `/authenticate`) require a JWT token in the `Authorization` header as `Bearer <token>`.

#### Authenticate
- **Endpoint**: `POST /authenticate`
- **Description**: Authenticate a user and obtain a JWT token.
- **Request Body**:
  ```json
  {
      "username": "admin",
      "password": "adminadmin"
  }
  ```
- **Response**:
  ```json
  {
      "AccessToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
  ```

### Job Management Endpoints

#### Get All Jobs
- **Endpoint**: `GET /jobs?page=0&size=10`
- **Description**: Retrieve a paginated list of jobs.
- **Response**:
  ```json
  [
      {
          "id": "123e4567-e89b-12d3-a456-426614174000",
          "name": "Test Job 1",
          "schedule_type": "cron",
          "cron_expression": "0 0/5 * * * *",
          "fixed_rate_ms": null,
          "payload": { "key": "value1" },
          "retry_policy": { "maxRetries": 3, "delayMs": 1000 },
          "status": "PENDING",
          "created_at": null,
          "updated_at": null,
          "last_run_at": null,
          "next_run_at": "2025-04-27T12:00:00Z",
          "version": null
      }
  ]
  ```

#### Get Job by ID
- **Endpoint**: `GET /jobs/{id}`
- **Description**: Retrieve a job by its ID.
- **Response**:
  ```json
  {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Test Job 1",
      "schedule_type": "cron",
      "cron_expression": "0 0/5 * * * *",
      "fixed_rate_ms": null,
      "payload": { "key": "value1" },
      "retry_policy": { "maxRetries": 3, "delayMs": 1000 },
      "status": "PENDING",
      "created_at": null,
      "updated_at": null,
      "last_run_at": null,
      "next_run_at": "2025-04-27T12:00:00Z",
      "version": null
  }
  ```

#### Create a Job
- **Endpoint**: `POST /jobs`
- **Description**: Create a new job (idempotent: prevents duplicates based on name and schedule).
- **Request Body**:
  ```json
  {
    "name": "programming_revolutionary_games.mpn",
    "schedule_type": "CRON",
    "cron_expression": "0 */12 * * * ?",
    "payload": {...},
    "retry_policy": {...},
    "status": "PENDING"
  }
  ```
- **Response**:
  ```json
  {
      "id": "some-uuid",
      "name": "New Job",
      "schedule_type": "cron",
      "cron_expression": "0 * * * * *",
      "fixed_rate_ms": null,
      "payload": null,
      "retry_policy": null,
      "status": "PENDING",
      "created_at": "2025-04-28T12:00:00Z",
      "updated_at": "2025-04-28T12:00:00Z",
      "last_run_at": null,
      "next_run_at": "2025-04-28T12:01:00Z",
      "version": 0
  }
  ```

#### Update a Job
- **Endpoint**: `PATCH /jobs/{id}`
- **Description**: Update an existing job.
- **Request Body**:
  ```json
  {
      "name": "Updated Job",
      "cron_expression": "0 0/10 * * * *"
  }
  ```
- **Response**: Same as the `GET /jobs/{id}` response, with updated fields.

### Error Responses
All error responses follow this format:
```json
{
    "error": "Error Code",
    "message": "Error message",
    "status": 400
}
```
- **401 Unauthorized**
- **400 Bad Request**
- **404 Not Found**
- **500 Internal server Error**

## Project Structure

```
scheduler_service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/vrtx/scheduler_service/
│   │   │       ├── controller/        # REST controllers
│   │   │       ├── exceptions/        # Custom exceptions
│   │   │       ├── executers/         # Job Executer
│   │   │       ├── model/             # /Entities, /dto, /enums and /mappers
│   │   │       ├── repository/        # JPA repositories and facades
│   │   │       ├── jwt/               # JWT and Spring Security configuration
│   │   │       └── service/           # Business logic and scheduling
│   │   └── resources/
│   │       ├── application.properties # Configuration
│   │       └── db/migration/          # Flyway migration scripts
│   └── test/                          # Unit and integration tests (to be added)
├── pom.xml                            # Maven dependencies
└── README.md                          # This file
```

## Dependencies

- **Spring Boot 3.x** (Web, Data JPA, Security)
- **MySQL Connector** (for database access)
- **Flyway** (for database migrations)
- **JJWT** (for JWT handling)
- **cron-utils** (for cron expression parsing)
- **Lombok** (for reducing boilerplate code)

See `pom.xml` for the full list of dependencies.

## Next Steps

- **Status Updates**: Add a `PATCH /jobs/{id}/status` endpoint to pause, resume, or cancel jobs.
- **Partial Updates**: Use a `JobUpdateRequest` DTO for the `PATCH /jobs/{id}` endpoint.
- **Observability**: Integrate Spring Boot Actuator for monitoring.
- **Testing**: Add unit and integration tests using `spring-boot-starter-test` and `Testcontainers`.

## License

This project is for assessment purposes and is not licensed for production use.