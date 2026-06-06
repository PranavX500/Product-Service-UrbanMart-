# Product Service

Product Service is a Spring Boot microservice for managing ecommerce product data. It owns product CRUD, category and price filtering, product image upload, Redis-backed product caching, Eureka registration, and a Kafka request-response flow for resolving product IDs into product details.

The service currently runs on port `8085` and exposes routes under `/Product`.

## Tech Stack

- Java 17
- Spring Boot 3.5.6
- Spring Web
- Spring Data JPA
- MySQL
- Redis cache
- Apache Kafka
- Eureka Discovery Client
- Cloudinary image upload
- Lombok
- Maven

## Runtime Dependencies

This service expects these systems to be available locally unless the application properties are changed:

| Dependency | Default location | Used for |
| --- | --- | --- |
| MySQL | `localhost:3306` | Product persistence |
| Redis | `127.0.0.1:6379` | Product query cache |
| Kafka | `localhost:9092` | Product lookup events |
| Eureka | `localhost:8761` | Service discovery |
| Cloudinary | configured by properties | Product image storage |

Redis can run in Docker with this compose service:

```yaml
services:
  redis:
    image: redis:7.2
    container_name: redis_server
    ports:
      - "6379:6379"
    restart: always
    volumes:
      - redis_data:/data
    command: ["redis-server", "--appendonly", "yes"]

volumes:
  redis_data:
```

When the Spring Boot app runs directly on the host machine, use:

```properties
spring.data.redis.host=127.0.0.1
spring.data.redis.port=6379
```

If the Spring Boot app is moved into Docker Compose with Redis on the same compose network, use the Redis service name instead.

## Configuration

Important properties live in `src/main/resources/application.properties`:

```properties
server.port=8085

spring.datasource.url=jdbc:mysql://localhost:3306/product_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=...

spring.cache.type=redis
spring.data.redis.host=127.0.0.1
spring.data.redis.port=6379

spring.kafka.bootstrap-servers=localhost:9092

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

cloudinary.cloud_name=...
cloudinary.api_key=...
cloudinary.api_secret=...
```

## How The Code Works

### Application Entry Point

`ProductServiceApplication` starts the Spring Boot application. Component scanning picks up controllers, services, repositories, config classes, exception handlers, Kafka listeners, and Redis cache configuration.

### Product API Layer

`ProductController` exposes HTTP endpoints under `/Product`.

It delegates business logic to `ProductService`. Image creation also calls `ImageUploadService` before saving the product.

### Product Service Layer

`ProductService` contains the main product operations:

- Creates products from `ProductDto`
- Maps `Product` entities to `ProductDto`
- Finds products by name, ID, IDs, brand, category, and price range
- Updates selected product fields
- Deletes products
- Throws custom exceptions when products, brands, or categories are not found
- Uses Redis caching for commonly read product queries

Write operations evict the whole `products` cache:

```java
@CacheEvict(cacheNames = "products", allEntries = true)
```

Read operations cache query results with keys such as:

- `all`
- `id:{id}`
- `name:{productName}`
- `brand:{brand}`
- `category:{CATEGORY}`
- `top5:{CATEGORY}`
- `ids:{ids}`

### Persistence Layer

`ProductRepo` extends `JpaRepository<Product, Long>`.

It uses derived query methods for simple lookups and JPQL for paginated price filtering:

- `findByProductName`
- `findAllByCategories`
- `findByIdIn`
- `findByBrand`
- `findTop5ByCategories`
- `findProductsBelowPrice`
- `findProductBetweenPrice`

### Product Entity

`Product` maps to the `products` table. Current fields:

- `id`
- `productName`
- `price`
- `quantity`
- `description`
- `brand`
- `imageUrl`
- `categories`

`categories` is stored as an enum string.

### Redis Cache

`RedisCacheConfig` enables Spring caching and configures Redis cache serialization.

The current cache configuration:

- Uses a 10 minute TTL
- Disables null caching
- Uses JSON serialization
- Adds a `v2::` cache prefix
- Includes type metadata so cached `ProductDto` and `List<ProductDto>` values can be deserialized correctly

If Redis contains stale entries after serializer changes, clear it:

```bash
sudo docker exec -it redis_server redis-cli FLUSHDB
```

### Image Upload

`ImageUploadService` uploads multipart image bytes to Cloudinary under the `ecommerce_products` folder. The returned secure URL is saved in `Product.imageUrl`.

### Kafka Flow

Kafka is used to receive product lookup requests and publish product details.

Input topic:

```text
Productids
```

Listener:

```java
ProductConsumer.handleIds(ListSuccessEvent event)
```

The listener:

1. Reads item IDs and quantities from `ListSuccessEvent`.
2. Fetches matching products from MySQL.
3. Converts products to `ProductDto`.
4. Replaces product quantity with requested item quantity when present.
5. Copies `requestId`, `emailId`, and `userId` to the response.
6. Sends `ProductResponseEvent` to Kafka.

Output topic:

```text
product-success-topic
```

Kafka config also has a `DefaultErrorHandler` with a `DeadLetterPublishingRecoverer`.

### Error Handling

`GlobalExceptionHandler` converts exceptions into this response shape:

```json
{
  "message": "Product not found with id: 1",
  "status": 404,
  "timestamp": "2026-04-15T16:36:05.305638721"
}
```

Handled custom exceptions:

- `ProductNotFoundException` -> `404`
- `BrandNotFound` -> `404`
- `InvalidCategoryException` -> `400`
- `ImageUploadException` -> `500`
- any other exception -> `500`

## API Endpoints

Base URL:

```text
http://localhost:8085/Product
```

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/add` | Create a product with image upload |
| `GET` | `/{name}` | Get product by product name |
| `GET` | `/category/{category}` | Get products by category |
| `GET` | `/products/{id}` | Get product by ID |
| `GET` | `/products/by-ids?ids=1,2,3` | Get multiple products by IDs |
| `GET` | `/all` | Get all products |
| `PUT` | `/{id}` | Partially update product fields |
| `DELETE` | `/{id}` | Delete product by ID |
| `GET` | `/belowPricePaginated?price=1000&page=0&size=10` | Get products below a price |
| `GET` | `/GetTop5/{category}` | Get top 5 products in a category |
| `GET` | `/BetweenPricePaginated?categories=ELECTRONICS&minprice=1000&maxprice=5000&page=0&size=10` | Get products in a category between prices |
| `GET` | `/BrandName` | Intended to get products by brand, but currently has a route bug |

### Create Product Example

`POST /Product/add` expects multipart form data:

| Field | Type |
| --- | --- |
| `productName` | text |
| `price` | number |
| `quantity` | number |
| `description` | text |
| `brand` | text |
| `categories` | enum value |
| `image` | file |

Example:

```bash
curl -X POST http://localhost:8085/Product/add \
  -F "productName=Keyboard" \
  -F "price=1499" \
  -F "quantity=10" \
  -F "description=Mechanical keyboard" \
  -F "brand=Logitech" \
  -F "categories=COMPUTER_ACCESSORIES" \
  -F "image=@/path/to/image.png"
```

### Get Products By IDs Example

```bash
curl "http://localhost:8085/Product/products/by-ids?ids=1,2,3"
```

### Get Products By Category Example

```bash
curl "http://localhost:8085/Product/category/ELECTRONICS"
```

### Get Products Between Prices Example

```bash
curl "http://localhost:8085/Product/BetweenPricePaginated?categories=ELECTRONICS&minprice=1000&maxprice=5000&page=0&size=10"
```

## Build And Run

Compile:

```bash
bash mvnw -DskipTests compile
```

Run tests:

```bash
bash mvnw test
```

Run the service:

```bash
bash mvnw spring-boot:run
```

Package:

```bash
bash mvnw clean package
```

Run packaged jar:

```bash
java -jar target/Product-Service-0.0.1-SNAPSHOT.jar
```

## Current Known Issues

These are code or design issues found during review. Security-related concerns are intentionally not included.

1. `GET /Product/BrandName` is broken.

   The controller uses `@PathVariable String brand`, but the route does not include `{brand}`. Change it to either:

   ```java
   @GetMapping("/BrandName/{brand}")
   ```

   or:

   ```java
   @GetMapping("/BrandName")
   public ResponseEntity<List<ProductDto>> findByBrand(@RequestParam String brand)
   ```

2. Price range parameters are passed in the wrong order.

   `ProductController.findProductPriceBetween` receives `minprice` and `maxprice`, but calls:

   ```java
   productService.findProductPriceBetween(categories, maxprice, minprice, page, size)
   ```

   It should pass `minprice` first and `maxprice` second.

3. Product quantity is not saved during product creation.

   `ProductDto` has `quantity`, and `Product` has `quantity`, but `Createproduct` does not copy `productDto.getQuantity()` into the entity.

4. Product quantity, brand, and image URL are not updated during update.

   `UpdateProduct` updates name, description, price, and category only.

5. Some endpoint routes are ambiguous.

   `GET /Product/{name}` can conflict conceptually with other single-segment routes. It works because the more specific mappings exist, but a clearer API would use `/name/{name}` or query params.

6. `GET /Product/GetTop5/{category}` does not validate invalid category values.

   `Categories.valueOf(category)` can throw `IllegalArgumentException`, which falls into the generic `500` handler. This should return `400`, like `findAllByCategory`.

7. Empty query results currently throw exceptions.

   Several list endpoints return `404` when the result is empty. For product listing/filtering APIs, returning `200` with an empty list is often easier for frontend clients.

8. Cache eviction is broad.

   Every create, update, or delete evicts all product cache entries. This is simple, but it can become expensive. Later, use targeted eviction for known keys or split caches by access pattern.

9. Cache key for ID lists depends on list order.

   `ids:[1, 2]` and `ids:[2, 1]` produce different cache entries even though they may represent the same lookup. Sort IDs before building the key if order does not matter.

10. Kafka topic names and group IDs are hardcoded.

    Topics like `Productids` and `product-success-topic` should move to properties so environments can configure them independently.

11. Kafka listener does not handle missing products explicitly.

    If a requested ID is not found, the response silently omits it. The event contract should define whether missing products should be returned as errors, skipped, or marked unavailable.

12. Kafka producer sends asynchronously without checking send result.

    `kafkaTemplate.send(...)` is called without success/failure callbacks. Add result handling or structured logging for production diagnostics.

13. `System.out.print` is used for logging.

    Replace with SLF4J logging so logs have levels, timestamps, class names, and can be filtered.

14. Dependency duplication exists in `pom.xml`.

    `spring-cloud-starter-openfeign` is declared twice. Remove one declaration.

15. Unused imports and redundant code should be cleaned.

    Examples include duplicate imports in `ImageUploadService`, unused imports in `ProductController`, `ProductRepo`, and `ProductServiceApplication`.

16. Naming conventions are inconsistent.

    Java methods should use lower camel case, for example `createProduct`, `updateProduct`, `mapToDto`, and `findByTop5`.

17. DTO/entity mapping is manual and repeated.

    Manual mapping is fine for a small service, but as DTOs grow, consider a dedicated mapper class or MapStruct.

18. Validation is missing for request data.

    Add DTO validation for required fields, positive prices, non-negative quantity, valid page/size, and image presence.

19. Tests are too thin for the current behavior.

    Add unit tests for `ProductService`, controller tests with mocked services, repository tests for custom queries, Redis cache behavior tests, and Kafka listener tests.

20. Local test setup currently depends on external MySQL.

    Use an isolated test profile with H2 or Testcontainers so `mvn test` can run reliably on any machine.

21. Docker support is incomplete for the full service.

    The Dockerfile packages only this service. A full local compose setup for MySQL, Redis, Kafka, Eureka, and this app would make onboarding easier.

22. API path style should be normalized.

    Current routes mix lowercase, camel case, and uppercase style: `/all`, `/GetTop5`, `/BetweenPricePaginated`, `/BrandName`. Prefer consistent REST-style paths such as `/products`, `/products/top`, `/products/search`.

## Suggested Improvement Order

1. Fix the route and parameter bugs:
   `BrandName`, price range order, category validation, and missing quantity mapping.

2. Clean build quality:
   remove duplicate dependency, unused imports, `System.out`, and method naming inconsistencies.

3. Improve API contracts:
   normalize routes, add validation, define empty result behavior, and document request/response examples.

4. Improve reliability:
   add test profile, service tests, controller tests, Redis tests, and Kafka listener tests.

5. Improve operations:
   move topic names to properties, add structured logging, add Docker Compose for the complete local stack, and add actuator health checks.

## Quick Troubleshooting

### Redis responds but Spring says "Unable to connect to Redis"

If Spring runs on the host and Redis runs in Docker with `6379:6379`, use:

```properties
spring.data.redis.host=127.0.0.1
```

Do not use `redis_server` unless Spring is also running inside Docker on the same Docker network.

### Redis JSON deserialization errors

Restart the Spring app after serializer changes and clear Redis:

```bash
sudo docker exec -it redis_server redis-cli FLUSHDB
```

### Maven wrapper permission denied

Run it through bash:

```bash
bash mvnw test
```

or make it executable:

```bash
chmod +x mvnw
```
