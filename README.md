# Product Inventory Demo

This project is a small Spring Boot + React inventory app built to practice the full request flow from controller to database, plus Kafka, Redis, caching, Swagger, validation, logging, and JPA indexing.

## What this project demonstrates

- CRUD APIs for products
- Validation with Jakarta Validation
- Enums for product status and audit actions
- Service layer with constructor injection
- Repository layer with Spring Data JPA
- Swagger/OpenAPI docs
- SLF4J logging with Logback
- MySQL persistence with JPA/Hibernate/HikariCP
- Kafka producer and consumer inside the same service
- History table populated by Kafka consumer events
- Redis cache for product-by-id reads and writes
- LRU cache for the all-products list
- Caffeine cache for search results
- React frontend with create/update/delete/search and history view
- Embedded Kafka and embedded Redis so the project runs without Docker

## How Kafka and Redis are used

### Kafka

- Product create, update, and delete operations publish a `HistoryEvent`.
- `HistoryProducer` sends the event to the `history-events` topic.
- `HistoryConsumer` listens on the same topic and stores a row in the `history` table.
- This is how the project demonstrates event-driven audit logging inside one microservice.

### Redis

- Product create and update write the product into Redis.
- `GET /api/products/{id}` first checks Redis and falls back to the database if needed.
- The app starts embedded Redis locally through `LocalInfraBootstrap`, so you do not need to install Redis separately.

### LRU cache

- `GET /api/products` uses the in-memory LRU cache for the full product list.
- The cache is a `LinkedHashMap`-based implementation that keeps recent list results.

### Caffeine cache

- `GET /api/products/search?name=...` uses a Caffeine cache.
- This gives a fast, bounded cache for repeated search queries.

## Main flow

1. React form submits a product request.
2. `ProductController` validates input and calls `ProductService`.
3. `ProductService` saves to MySQL through `ProductRepository`.
4. The service writes through to Redis and invalidates list/search caches.
5. The service publishes a Kafka history event.
6. `HistoryConsumer` writes the audit record to the `history` table.

## Project structure

- `src/main/java/com/example/demo/controller` - REST controllers
- `src/main/java/com/example/demo/service` - business logic
- `src/main/java/com/example/demo/repository` - JPA repositories
- `src/main/java/com/example/demo/entity` - JPA entities
- `src/main/java/com/example/demo/dto` - request DTOs and validation
- `src/main/java/com/example/demo/enums` - status and action enums
- `src/main/java/com/example/demo/kafka` - producer and consumer
- `src/main/java/com/example/demo/cache` - cache helpers and LRU cache
- `src/main/java/com/example/demo/config` - embedded infra, cache, swagger, redis config
- `frontend/` - React UI

## Where to look in code

- Embedded Kafka/Redis startup: `src/main/java/com/example/demo/config/LocalInfraBootstrap.java`
- Product cache strategy: `src/main/java/com/example/demo/cache/ProductCacheService.java`
- CRUD orchestration: `src/main/java/com/example/demo/service/ProductService.java`
- Kafka audit producer: `src/main/java/com/example/demo/kafka/HistoryProducer.java`
- Kafka audit consumer: `src/main/java/com/example/demo/kafka/HistoryConsumer.java`
- Product API: `src/main/java/com/example/demo/controller/ProductController.java`
- History API: `src/main/java/com/example/demo/controller/HistoryController.java`
- Frontend UI: `frontend/src/App.jsx`

## Database indexes

- `products` has indexes on `name` and `status`
- `history` has indexes on `object_type, object_id` and `created_at`

## Swagger

Open:

```bash
http://localhost:8080/swagger-ui.html
```

## Run the project

Backend:

```bash
cd ~/Downloads/demo\ \(1\)/demo
mvn -DskipTests org.springframework.boot:spring-boot-maven-plugin:3.5.15:run
```

Frontend:

```bash
cd ~/Downloads/demo\ \(1\)/demo/frontend
npm install
npm run dev
```

Open the UI at:

```bash
http://localhost:3000
```

## Stop the app

If the backend is running in a terminal, press `Ctrl+C` there. That stops Spring Boot, embedded Kafka, and embedded Redis together.

If a port is stuck:

```bash
ss -ltnp | grep ':8080'
kill <PID>
```

## Notes for learning

- Constructor injection is used everywhere instead of field `@Autowired`.
- `@Valid` on controller methods protects the save/update API.
- `@Enumerated(EnumType.STRING)` makes enum values readable in the database.
- `logback-spring.xml` controls console and file logging.
- HikariCP is the default MySQL connection pool used by Spring Boot.
