# CSMS Authorization System

A Charging Station Management System (CSMS) authorization flow implementation using Kotlin, Spring Boot, and Apache Kafka.

## Architecture

The system consists of three main components:

1. **CSMS Backend Service** - REST API that receives authorization requests from charging stations
2. **Authentication Service** - Processes authorization requests asynchronously via Kafka and maintains a whitelist
3. **Common Module** - Shared data models and DTOs

### Flow

1. Charging station sends authorization request to CSMS Backend REST API
2. Backend validates identifier format (20-80 characters) and publishes request to Kafka
3. Authentication Service consumes the request from Kafka
4. Authentication Service checks the identifier against the whitelist
5. Authentication Service publishes the response back to Kafka
6. Backend consumes the response and returns it to the charging station

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Gradle 7.6+ (or use Gradle Wrapper included in the project)

**Note**: If the Gradle wrapper jar (`gradle/wrapper/gradle-wrapper.jar`) is missing, you can download it:
```bash
# Download the wrapper jar (already included in the project)
curl -L https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar

# Or if you have Gradle installed, generate it:
gradle wrapper
```

## Setup and Running

### 1. Start Kafka Infrastructure

```bash
docker-compose up -d
```

This starts:
- Zookeeper (port 2181)
- Kafka broker (port 9092)

Wait a few seconds for Kafka to be fully ready.

### 2. Build the Project

```bash
./gradlew build
```

Or on Windows:
```bash
gradlew.bat build
```

### 3. Run the Services

**Terminal 1 - Start Authentication Service:**
```bash
./gradlew :authentication-service:bootRun
```

**Terminal 2 - Start CSMS Backend Service:**
```bash
./gradlew :csms-backend:bootRun
```

The CSMS Backend will be available at `http://localhost:8080`

### 4. Test the Authorization Flow

You can use the provided test script:
```bash
./test-api.sh
```

Or test manually with curl:

**Example: Accepted (known and allowed)**
```bash
curl -X POST http://localhost:8080/api/v1/authorization \
  -H "Content-Type: application/json" \
  -d '{
    "stationUuid": "25aac66b-6051-478a-95e2-6d3aa343b025",
    "driverIdentifier": {"id": "id12345678901234567890"}
  }'
```

**Note**: Identifiers must be between 20-80 characters. The sample "id1234" from the requirements is too short, so use valid-length identifiers like "id12345678901234567890" (20+ characters).

Expected response:
```json
{
  "authorizationStatus": "Accepted"
}
```

**Example: Unknown identifier**
```bash
curl -X POST http://localhost:8080/api/v1/authorization \
  -H "Content-Type: application/json" \
  -d '{
    "stationUuid": "25aac66b-6051-478a-95e2-6d3aa343b025",
    "driverIdentifier": {"id": "unknown12345678901234567890"}
  }'
```

Expected response:
```json
{
  "authorizationStatus": "Unknown"
}
```

**Example: Invalid identifier (too short)**
```bash
curl -X POST http://localhost:8080/api/v1/authorization \
  -H "Content-Type: application/json" \
  -d '{
    "stationUuid": "25aac66b-6051-478a-95e2-6d3aa343b025",
    "driverIdentifier": {"id": "short"}
  }'
```

Expected response:
```json
{
  "authorizationStatus": "Invalid"
}
```

**Example: Rejected (known but not allowed)**
```bash
curl -X POST http://localhost:8080/api/v1/authorization \
  -H "Content-Type: application/json" \
  -d '{
    "stationUuid": "25aac66b-6051-478a-95e2-6d3aa343b025",
    "driverIdentifier": {"id": "rejected12345678901234567890"}
  }'
```

Expected response:
```json
{
  "authorizationStatus": "Rejected"
}
```

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Tests for Specific Module
```bash
./gradlew :csms-backend:test
./gradlew :authentication-service:test
```

### Run Integration Tests
```bash
./gradlew integrationTest
```

## Test Coverage

The test suite includes:

### Unit Tests
- **AuthorizationServiceTest** - Tests authorization logic, validation, and Kafka integration
- **AuthorizationControllerTest** - Tests REST controller
- **WhitelistServiceTest** - Tests whitelist operations (add, remove, check)
- **AuthorizationProcessorTest** - Tests authorization processing logic

### Integration Tests
- End-to-end flow tests (can be added as needed)

## Project Structure

```
.
├── common/                          # Shared models and DTOs
│   └── src/main/kotlin/.../model/
├── csms-backend/                    # REST API service
│   ├── src/main/kotlin/.../backend/
│   │   ├── controller/              # REST controllers
│   │   ├── service/                 # Business logic
│   │   ├── config/                  # Configuration
│   │   └── listener/                # Kafka listeners
│   └── src/test/                    # Unit tests
├── authentication-service/          # Kafka consumer service
│   ├── src/main/kotlin/.../auth/
│   │   ├── service/                 # Business logic
│   │   ├── config/                  # Configuration
│   │   └── listener/                # Kafka listeners
│   └── src/test/                    # Unit tests
├── docker-compose.yml               # Kafka infrastructure
└── build.gradle.kts                 # Root build configuration
```

## Authorization Status Values

- **Accepted** - Identifier is known and allowed to charge
- **Unknown** - Identifier is not known in the system
- **Invalid** - Identifier format is invalid (not 20-80 characters)
- **Rejected** - Identifier is known but not allowed for charging

## Whitelist Management

The whitelist is currently stored in memory. Sample identifiers are pre-loaded:

- `id1234` - Accepted
- `allowed12345678901234567890` - Accepted
- `rejected12345678901234567890` - Rejected

To add identifiers programmatically, use the `WhitelistService.addIdentifier()` method.

## Kafka Topics

- **authorization-requests** - Requests from backend to authentication service
- **authorization-responses** - Responses from authentication service back to backend

## Design Decisions

1. **Synchronous REST API with Async Backend**: The REST API waits for Kafka response using CompletableFuture with timeout (5 seconds)
2. **In-Memory Storage**: Whitelist is stored in memory for simplicity (as per assignment scope reduction)
3. **Request-Response Correlation**: Uses requestId in Kafka message key to correlate requests and responses
4. **Validation**: Identifier length validation (20-80 chars) is done in both services for defense in depth

## Scaling Considerations

- **Database**: Replace in-memory whitelist with a database (PostgreSQL, MongoDB, etc.)
- **Caching**: Add Redis cache for frequently accessed identifiers
- **Horizontal Scaling**: Both services are stateless and can be scaled horizontally
- **Kafka Partitions**: Increase partition count for higher throughput
- **Response Timeout**: Adjust timeout based on SLA requirements
- **Monitoring**: Add metrics (Prometheus) and distributed tracing (Jaeger/Zipkin)
- **Error Handling**: Add retry logic and dead letter queues for failed messages

## Troubleshooting

### Gradle Wrapper Issues

**Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain**

This means the `gradle-wrapper.jar` file is missing. Fix it by running:

```bash
# Option 1: Download the wrapper jar directly
curl -L https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar

# Option 2: If you have Gradle installed, regenerate the wrapper
gradle wrapper

# Option 3: Use your system Gradle (if installed)
gradle :authentication-service:bootRun
```

After downloading, verify it works:
```bash
./gradlew --version
```

### Kafka Connection Issues
- Ensure Docker containers are running: `docker-compose ps`
- Check Kafka logs: `docker-compose logs kafka`
- Verify Kafka is ready: `docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list`

### Service Not Starting
- Check if port 8080 is available
- Verify Java 17+ is installed: `java -version`
- Check application logs for errors

### Tests Failing
- Ensure Kafka is running for integration tests
- Run `./gradlew clean build` to rebuild

## License

This is a coding exercise implementation.
