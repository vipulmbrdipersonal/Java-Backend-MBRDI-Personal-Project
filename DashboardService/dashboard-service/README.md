# Mercedes-Benz Auth Service

Java 17 / Spring Boot 3.4.5, PostgreSQL, Spring Kafka and JWT. No Docker and no API Gateway.

## Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL running on localhost:5432
- Kafka running on localhost:9092

## Database setup
Run `infra/create-databases.sql` once using a PostgreSQL administrator, or create `auth_db` manually. The service creates/updates its tables on startup with Hibernate `ddl-auto=update`.

## Configuration
Defaults are in `src/main/resources/application.properties`. Override with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, and `CORS_ALLOWED_ORIGINS`. Change the development JWT secret before deployment. The Kafka broker must allow topic creation; Spring Kafka's `KafkaAdmin` provisions `user.registered` and `auth.token.revoked` from the `NewTopic` beans. Set broker `auto.create.topics.enable=true` only if you also want broker-side auto-creation; this service does not depend on manual topic creation.

## Run
From this directory: `mvn spring-boot:run`

## API
- `POST /api/auth/signup` (public)
- `POST /api/auth/login` (public)
- `GET /api/auth/me` (Bearer JWT)
- `PUT /api/auth/password` (Bearer JWT)
- `POST /api/auth/logout` (Bearer JWT)
- `GET /actuator/health`

### Example signup
```json
{"name":"Vipul Choubey","email":"vipul@example.com","mobile":"9876543210","password":"Mercedes@123"}
```
### Example login
```json
{"identifier":"vipul@example.com","password":"Mercedes@123"}
```

The signup/login response includes `token`, `tokenType`, `expiresInSeconds`, and `user`. Send `Authorization: Bearer <token>` to protected endpoints. Logout records the token JTI in `auth_db` and publishes `auth.token.revoked` so the other services can maintain their own local revocation state.
