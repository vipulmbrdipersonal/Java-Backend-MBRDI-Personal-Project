# Mercedes-Benz Vehicle Service

Standalone JDK 17 / Spring Boot 3.2.12 microservice. No Docker and no API Gateway.

## Responsibilities
- Registers virtual Mercedes-Benz vehicles for the authenticated customer.
- Generates a unique Vehicle ID (`MB-VEH-...`) and 17-character VIN (`W1K...`). Database unique constraints protect both identifiers.
- Stores current ownership and an ownership-history row in its own PostgreSQL database.
- Consumes `purchase.completed` and creates/assigns the purchased vehicle.
- Publishes `vehicle.created` and `vehicle.ownership.assigned` for vehicles created through its REST API.
- Consumes `auth.token.revoked` and checks its local revoked-token table on authenticated requests.
- Creates Kafka topics at startup through `KafkaAdmin` / `NewTopic` declarations.

## Project tree

```text
vehicle-service/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/mercedes/vehicle/
    │   ├── VehicleServiceApplication.java
    │   ├── config/       KafkaTopicConfig.java, WebConfig.java
    │   ├── controller/  VehicleController.java
    │   ├── dto/         ApiError, CreateVehicleRequest, OwnershipResponse, VehicleResponse
    │   ├── entity/      RevokedToken, Vehicle, VehicleOwnership, VehicleStatus
    │   ├── event/       KafkaEventPublisher, VehicleEventConsumer
    │   ├── exception/   GlobalExceptionHandler
    │   ├── repository/  RevokedTokenRepository, VehicleOwnershipRepository, VehicleRepository
    │   ├── security/    CurrentUser, JwtAuthenticationFilter, JwtService
    │   └── service/     VehicleService
    └── resources/application.properties
```

## 1. Create PostgreSQL database

Run once in pgAdmin or psql:

```sql
CREATE DATABASE vehicle_db;
```

The service creates/updates its tables automatically using `spring.jpa.hibernate.ddl-auto=update`.

## 2. Configure application.properties

Update `spring.datasource.username` and `spring.datasource.password`, or set `DB_USERNAME` and `DB_PASSWORD` environment variables.

Set `JWT_SECRET` to the exact same Base64-encoded HS256 secret used by auth-service. It must decode to at least 32 bytes. Do not use the placeholder in a real run.

Kafka defaults to `localhost:9092`; override with `KAFKA_BOOTSTRAP_SERVERS` if needed. Kafka broker-side topic auto-creation is not relied upon: this service declares all four topics using Spring Kafka `NewTopic` beans. The Kafka user must have topic-create permissions and the broker must be reachable at startup.

## 3. Start the service

From this directory:

```bash
mvn clean spring-boot:run
```

Or package and run:

```bash
mvn clean package
java -jar target/vehicle-service-1.0.0.jar
```

Health: `http://localhost:8086/actuator/health`

## 4. REST API

All `/api/**` endpoints require `Authorization: Bearer <JWT>`. The token must contain `sub` (email), `userId`, `role`, `jti`, and `exp`, consistent with the platform auth-service.

### Register a virtual vehicle

`POST http://localhost:8086/api/vehicles`

```json
{
  "model": "Mercedes-Benz G-Wagon",
  "modelId": "G63-AMG",
  "modelYear": 2026
}
```

The authenticated user's `userId` and email become the owner. The response contains generated `vehicleId` and `vin`.

### List my vehicles

`GET http://localhost:8086/api/vehicles/my`

### Get a vehicle

`GET http://localhost:8086/api/vehicles/MB-VEH-<generated-id>`

### Ownership history

`GET http://localhost:8086/api/vehicles/MB-VEH-<generated-id>/ownership-history`

A customer can access only their own vehicles. A role containing `ADMIN` can access any vehicle.

## 5. Kafka event contracts

The consumer accepts flexible JSON field aliases for purchase data. `purchase.completed` should include at minimum `purchaseId` and one of `userId`, `ownerId`, or `customerId`. Recommended fields: `email` (or `customerEmail` / `ownerEmail`), `model` (or `modelName` / `vehicleModel`), `modelId`, and `modelYear`.

The service publishes `vehicle.created` and `vehicle.ownership.assigned` as JSON objects containing `eventId`, `eventType`, `occurredAt`, `vehicleId`, `vin`, owner details, model details, and `purchaseId` where applicable.

`auth.token.revoked` uses the established contract: `eventId`, `tokenId`, `expiresAt`, `revokedAt` (ISO-8601 timestamps). Each service maintains its own local revocation table.

## Notes
- `purchase.completed` processing is idempotent by `purchaseId` (a repeat event will not create another vehicle).
- Kafka delivery and PostgreSQL writes are not a distributed transaction. For production-grade guaranteed publishing, add an outbox table and relay; this implementation keeps the service self-contained as requested.
- VINs are synthetic identifiers for virtual/demo vehicles, not manufacturer-issued VINs. Do not represent them as real factory VINs.
