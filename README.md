# Sliding Window Rate Limiter with JWT Authentication

## Overview
This project implements a Spring Boot backend service with:
- JWT-based authentication for secure login and API access.
- A Redis-backed sliding window rate limiter to control request frequency per user.
- Clean, configurable, and testable code following Spring best practices.

## Technology Choices
- **Spring Boot**: Provides rapid development, dependency injection, and integration with Spring Security.
- **Spring Security + JWT**: Lightweight stateless authentication mechanism suitable for REST APIs.
- **Redis (via Spring Data Redis)**: Chosen for its speed and atomic operations. Perfect for distributed rate limiting.
- **Lua scripting in Redis**: Ensures atomicity of rate limiting logic under concurrency.
- **JUnit 5 + Mockito**: Used for unit testing, mocking Redis behavior, and verifying edge cases.


## Design Decisions
- **Sliding Window Algorithm**  
  Implemented with Redis ZSET and Lua script. Removes expired requests, counts active ones, and enforces a configurable limit. More accurate than fixed window counters, avoids burst allowance at window boundaries.

- **Fail-Open Strategy**  
  If Redis is unavailable, requests are allowed instead of blocking the system. This prevents accidental denial of service due to infrastructure issues.

- **Configuration Properties**  
  `rate.limit.window` and `rate.limit.max` injected via Spring configuration. Allows environment-specific tuning without code changes.

- **Separation of Concerns**  
  Authentication handled by Spring Security. Rate limiting encapsulated in a dedicated `SlidingWindowRateLimiter` service. Keeps code modular and maintainable.

## Assumptions
- Each user is identified by a unique `userId`.
- Rate limiting is per user, not global.
- Default configuration: 100 requests per 60 seconds.
- Redis is available in production; fail-open is acceptable during outages.
- JWT tokens are valid and verified before rate limiting is applied.

## Testing & Coverage
- **Unit Tests (Mockito)**  
  - Allowed requests (`1L` response).  
  - Blocked requests (`0L` response).  
  - Redis failure (exception handling).  
  - Sequence of requests.  
  - Concurrency test.  

## How to Run
1. Configure Redis connection in `application.properties`.
2. Set rate limiter properties:
   ```properties
   rate.limit.window=60000
   rate.limit.max=100
