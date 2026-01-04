# Spring-jwt

![Java CI with Maven](https://github.com/castroy10/spring-jwt/actions/workflows/maven.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-green)
![Redis](https://img.shields.io/badge/Redis-8.x-red)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![License](https://img.shields.io/github/license/castroy10/spring-jwt)
![GitHub issues](https://img.shields.io/github/issues/castroy10/spring-jwt)
![GitHub stars](https://img.shields.io/github/stars/castroy10/spring-jwt?style=social)

[🇷🇺 Русская версия](README_RU.md)

**spring-jwt** is a robust implementation of JWT (JSON Web Token) authentication using Spring Security and Redis. It demonstrates the secure "Access + Refresh Token" rotation pattern, ensuring stateless authentication with the ability to revoke sessions.

## Features

- **Dual Token Authentication**:
    - **Access Token**: Short-lived JWT for API resource access.
    - **Refresh Token**: Long-lived JWT stored in Redis for renewing access tokens.
- **Token Rotation**: Generates a new pair of tokens upon refresh to prevent token reuse attacks.
- **Redis Integration**:
    - Stores Refresh Tokens with TTL (Time To Live).
    - Validates token existence and ownership (using `jti` claim).
- **Security**:
    - Stateless session policy (`STATELESS`).
    - Custom `JwtFilter` for request interception.
    - Password encryption using `BCrypt`.
- **Error Handling**: Global `RestControllerAdvice` for standardized JSON error responses.

## Used Technologies

- Java 21
- Spring Boot 4.0.1
- Spring Security
- Spring Data Redis
- JJWT (0.12.6)
- Docker

## Getting Started

### Using Docker (Recommended)

The easiest way to run the application is via Docker Compose, which sets up the application container and the Redis database.

1. **Clone the repository:**
   ```bash
   git clone https://github.com/castroy10/spring-jwt.git
   cd spring-jwt
   ```

2. **Run the project:**
   ```bash
   docker compose up --build -d
   ```

3. **Check status:**
   The application will be available at `http://localhost:8080`.

### Local Run

If you prefer running via IDE or Maven locally:

1. **Prerequisites**:
    - JDK 21+
    - Installed and running Redis (default port `6379`)

2. **Run:**
   ```bash
   ./mvnw spring-boot:run
   ```

## Test Credentials

Users are stored In-Memory for demonstration purposes (configured in `UserConfig.java`):

| Username (Email)   | Password | Role |
|--------------------|----------|------|
| `user@example.ru`  | `12345` | USER |
| `admin@example.ru` | `12345` | ADMIN |

## API Endpoints

### 1. Login
Authenticates the user and returns a pair of tokens.

*   **URL:** `/api/v1/login`
*   **Method:** `POST`
*   **Body:**
    ```json
    {
      "username": "user@example.ru",
      "password": "12345"
    }
    ```
*   **Response:**
    ```json
    {
      "message": "Login successful",
      "payload": {
        "accessToken": "eyJhbGci...",
        "refreshToken": "eyJhbGci..."
      },
      "timestamp": "2026-01-04T12:00:00"
    }
    ```

### 2. Refresh Token
Obtain a new pair of tokens using a valid Refresh Token.

*   **URL:** `/api/v1/refresh`
*   **Method:** `POST`
*   **Headers:**
    *   `Authorization`: `Bearer <YOUR_REFRESH_TOKEN>`
*   **Response:** Similar to Login, returns new Access and Refresh tokens.

### 3. Protected Resource
Example of a protected route (configured in `SecurityConfig`).

*   **URL:** `/` (Home Page) or any other configured path.
*   **Method:** `GET`
*   **Headers:**
    *   `Authorization`: `Bearer <YOUR_ACCESS_TOKEN>`

## Configuration (application.yml)

You can configure token expiration and keys in `src/main/resources/application.yml`:

```yaml
jwt:
  secretKey: "YourBase64EncodedSecretKey..." # Must be strong
  accessTokenExpirationMinutes: 60
  refreshTokenExpirationDays: 7
```

## Architecture

### Security Flow
1.  **Login**: User sends credentials. `SecurityService` authenticates via `AuthenticationManager`.
2.  **Token Generation**: `JwtUtil` creates Access and Refresh tokens.
3.  **Storage**: `RedisService` saves the Refresh Token using the `username` and `jti` (JWT ID) as the key.
4.  **Request Filtering**: `JwtFilter` intercepts requests, extracts the Access Token, and validates it.
5.  **Refresh**:
    *   Validates the incoming Refresh Token signature.
    *   Checks if the token exists in Redis.
    *   If valid, deletes the old token from Redis and issues a new pair.

### Project Structure
*   `config`: Spring Security and User configuration.
*   `controller`: REST endpoints and Exception handling.
*   `model/dto`: Data Transfer Objects for requests/responses.
*   `security`: JWT utilities and Filter implementation.
*   `service`: Business logic for Redis and Security operations.
