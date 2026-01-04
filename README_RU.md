# Spring-jwt

![Java CI with Maven](https://github.com/castroy10/spring-jwt/actions/workflows/maven.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-green)
![Redis](https://img.shields.io/badge/Redis-8.x-red)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![License](https://img.shields.io/github/license/castroy10/spring-jwt)
![GitHub issues](https://img.shields.io/github/issues/castroy10/spring-jwt)
![GitHub stars](https://img.shields.io/github/stars/castroy10/spring-jwt?style=social)

[🇺🇸 English version](README.md)

**spring-jwt** — это надежная реализация аутентификации JWT (JSON Web Token) с использованием Spring Security и Redis. Проект демонстрирует паттерн ротации "Access + Refresh Token", обеспечивая stateless аутентификацию с возможностью отзыва сессий.

## Возможности

- **Двухфакторная токенизация**:
    - **Access Token**: Короткоживущий JWT для доступа к ресурсам API.
    - **Refresh Token**: Долгоживущий JWT, сохраняемый в Redis, для обновления токенов доступа.
- **Ротация токенов**: Генерация новой пары токенов при каждом обновлении для предотвращения атак повторного использования (Token Reuse).
- **Интеграция с Redis**:
    - Хранение Refresh токенов с TTL (временем жизни).
    - Валидация существования и принадлежности токена (используя claim `jti`).
- **Безопасность**:
    - Stateless политика сессий (`STATELESS`).
    - Кастомный `JwtFilter` для перехвата запросов.
    - Шифрование паролей с использованием `BCrypt`.
- **Обработка ошибок**: Глобальный `RestControllerAdvice` для стандартизированных JSON-ответов об ошибках.

## Использованные технологии

- Java 21
- Spring Boot 4.0.1
- Spring Security
- Spring Data Redis
- JJWT (0.12.6)
- Docker

## Установка и запуск

### Использование Docker (Рекомендуется)

Самый простой способ запустить приложение — через Docker Compose. Это автоматически настроит контейнер приложения и базу данных Redis.

1. **Клонируйте репозиторий:**
   ```bash
   git clone https://github.com/castroy10/spring-jwt.git
   cd spring-jwt
   ```

2. **Запустите проект:**
   ```bash
   docker compose up --build -d
   ```

3. **Проверьте статус:**
   Приложение будет доступно по адресу `http://localhost:8080`.

### Локальный запуск

Если вы предпочитаете запускать через IDE или Maven локально:

1. **Требования**:
    - JDK 21+
    - Установленный и запущенный Redis (порт по умолчанию `6379`)

2. **Запуск:**
   ```bash
   ./mvnw spring-boot:run
   ```

## Тестовые учетные данные

Пользователи хранятся в памяти (In-Memory) для демонстрационных целей (настроено в `UserConfig.java`):

| Имя пользователя (Email) | Пароль | Роль |
|--------------------------|--------|------|
| `user@example.ru`        | `12345` | USER |
| `admin@example.ru`       | `12345` | ADMIN |

## API Эндпоинты

### 1. Логин (Login)
Аутентификация пользователя и возвращение пары токенов.

*   **URL:** `/api/v1/login`
*   **Метод:** `POST`
*   **Тело запроса:**
    ```json
    {
      "username": "user@example.ru",
      "password": "12345"
    }
    ```
*   **Ответ:**
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

### 2. Обновление токена (Refresh Token)
Получение новой пары токенов с использованием валидного Refresh токена.

*   **URL:** `/api/v1/refresh`
*   **Метод:** `POST`
*   **Заголовки:**
    *   `Authorization`: `Bearer <ВАШ_REFRESH_TOKEN>`
*   **Ответ:** Аналогичен логину, возвращает новые Access и Refresh токены.

### 3. Защищенный ресурс
Пример защищенного маршрута (настроено в `SecurityConfig`).

*   **URL:** `/` (Главная страница) или любой другой настроенный путь.
*   **Метод:** `GET`
*   **Заголовки:**
    *   `Authorization`: `Bearer <ВАШ_ACCESS_TOKEN>`

## Конфигурация (application.yml)

Вы можете настроить время жизни токенов и ключи в `src/main/resources/application.yml`:

```yaml
jwt:
  secretKey: "YourBase64EncodedSecretKey..." # Должен быть надежным
  accessTokenExpirationMinutes: 60
  refreshTokenExpirationDays: 7
```

## Архитектура

### Поток безопасности (Security Flow)
1.  **Логин**: Пользователь отправляет учетные данные. `SecurityService` аутентифицирует через `AuthenticationManager`.
2.  **Генерация токенов**: `JwtUtil` создает Access и Refresh токены.
3.  **Хранение**: `RedisService` сохраняет Refresh токен, используя `username` и `jti` (ID токена) в качестве ключа.
4.  **Фильтрация запросов**: `JwtFilter` перехватывает запросы, извлекает Access токен и валидирует его.
5.  **Обновление (Refresh)**:
    *   Проверяет подпись входящего Refresh токена.
    *   Проверяет наличие токена в Redis.
    *   Если валиден, удаляет старый токен из Redis и выдает новую пару.

### Структура проекта
*   `config`: Конфигурация Spring Security и пользователей.
*   `controller`: REST эндпоинты и обработка исключений.
*   `model/dto`: DTO для запросов и ответов.
*   `security`: Утилиты JWT и реализация фильтра.
*   `service`: Бизнес-логика для операций с Redis и безопасностью.
