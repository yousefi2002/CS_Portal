# Digital Ecosystem Backend

This is a secure Spring Boot backend for the Digital Ecosystem connecting Students, Universities, and Companies.

## Features
- **Clean Architecture**: Properly layered (Controller, Service, Repository, Model, DTO).
- **Security**: JWT-based authentication and role-based access control (RBAC).
- **Validation**: Input validation using `@Valid` and Jakarta Validation annotations.
- **Documentation**: Interactive API documentation with Swagger/OpenAPI.
- **Exception Handling**: Global exception handler for meaningful error messages.
- **Database**: MongoDB for flexible data storage.

## Tech Stack
- Java 17
- Spring Boot 3.2.4
- Spring Security
- Spring Data MongoDB
- JJWT (JSON Web Token)
- Lombok
- SpringDoc OpenAPI (Swagger)

## Project Structure
```
src/main/java/com/manus/digitalecosystem/
├── config/         # Configuration classes (OpenAPI, etc.)
├── controller/     # REST Controllers (handles HTTP requests)
├── dto/            # Data Transfer Objects (request/response)
├── exception/      # Custom exceptions and global handler
├── model/          # MongoDB entities and Enums
├── repository/     # Spring Data MongoDB repositories
├── security/       # JWT and Spring Security configuration
├── service/        # Business logic interfaces
│   └── impl/       # Service implementations
└── util/           # Utility classes
```

## Getting Started

### Prerequisites
- JDK 17
- Maven
- MongoDB (running on localhost:27017)

### Running the Application
1. Clone the repository.
2. Update `src/main/resources/application.properties` with your MongoDB URI.
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### API Documentation
Once the application is running, you can access the Swagger UI at:
`http://localhost:8080/swagger-ui.html`

## Security
- **Authentication**: `POST /api/auth/login`
- **Authorization**: Use the Bearer token in the `Authorization` header for protected endpoints.
- **Roles**: `SUPER_ADMIN`, `UNIVERSITY_ADMIN`, `DEPARTMENT_ADMIN`, `STUDENT`, `COMPANY_ADMIN`.

## API Response Contract
All success and error responses now use a unified DTO structure:

```json
{
  "success": true,
  "status": 200,
  "message": "Localized message",
  "data": {},
  "errors": null,
  "meta": null,
  "pagination": null,
  "timestamp": "2026-04-21T10:00:00",
  "requestId": "uuid"
}
```

## Multilingual Data Shape
Translatable values are stored in MongoDB with this shape:

```json
{
  "fa": "",
  "en": "",
  "ps": ""
}
```

`User.fullName` now follows this format.

## Auth Endpoints
- `POST /api/auth/login` (email + password)
- `POST /api/auth/logout`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`

## User Endpoints
- `POST /api/users` (SUPER_ADMIN)
- `GET /api/users` (SUPER_ADMIN)
- `GET /api/users/me`
- `GET /api/users/{userId}` (SUPER_ADMIN)
- `PATCH /api/users/{userId}/status` (SUPER_ADMIN)

## Error Handling
The API returns logical error messages in the following format:
```json
{
  "message": "Error description",
  "httpStatus": "STATUS_CODE",
  "timestamp": "ISO-8601-TIMESTAMP"
}
```
For validation errors:
```json
{
  "message": "Validation failed",
  "errors": {
    "fieldName": "Validation error message"
  },
  "status": "BAD_REQUEST",
  "timestamp": "ISO-8601-TIMESTAMP"
}
```
