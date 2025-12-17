# Implementation Notes

## Architecture Overview

The system implements an asynchronous authorization flow using Kafka as the message broker between the CSMS Backend and Authentication Service.

### Components

1. **CSMS Backend Service** (`csms-backend`)
   - REST API endpoint: `POST /api/v1/authorization`
   - Validates identifier format (20-80 characters)
   - Publishes authorization requests to Kafka topic `authorization-requests`
   - Consumes responses from Kafka topic `authorization-responses`
   - Uses CompletableFuture for synchronous response handling (with 5s timeout)

2. **Authentication Service** (`authentication-service`)
   - Consumes authorization requests from Kafka
   - Validates identifier format
   - Checks against in-memory whitelist
   - Publishes responses back to Kafka

3. **Common Module** (`common`)
   - Shared data models and DTOs
   - Request/Response models
   - Authorization status enum

## Design Decisions

### 1. Synchronous REST with Async Backend
- The REST API needs to return a response synchronously to the charging station
- Implemented using CompletableFuture with timeout mechanism
- Request-response correlation via `requestId` in Kafka message key

### 2. In-Memory Whitelist
- As per assignment scope reduction, whitelist is stored in memory
- Easy to replace with database in production
- Thread-safe operations using ConcurrentHashMap

### 3. Validation in Both Services
- Backend validates format early to avoid unnecessary Kafka messages
- Authentication service also validates for defense in depth
- Both return "Invalid" status for format violations

### 4. Identifier Length
- Requirements specify 20-80 characters
- Sample "id1234" (6 chars) would be Invalid
- Updated whitelist to use valid-length identifiers for testing

## Testing Strategy

### Unit Tests
- **AuthorizationServiceTest**: Tests validation logic and Kafka integration
- **AuthorizationControllerTest**: Tests REST endpoint
- **WhitelistServiceTest**: Tests whitelist CRUD operations
- **AuthorizationProcessorTest**: Tests authorization processing logic

### Test Coverage
- All authorization statuses (Accepted, Unknown, Invalid, Rejected)
- Edge cases (boundary values for identifier length)
- Error handling (timeout scenarios)

## Kafka Topics

- `authorization-requests`: Backend → Authentication Service
- `authorization-responses`: Authentication Service → Backend

## Future Improvements

1. **Persistence**: Replace in-memory storage with database
2. **Caching**: Add Redis for frequently accessed identifiers
3. **Monitoring**: Add metrics and distributed tracing
4. **Error Handling**: Dead letter queues for failed messages
5. **Scalability**: Increase Kafka partitions, add load balancing
6. **Security**: Add authentication/authorization for REST API
7. **Configuration**: Externalize timeout values and topic names

## Known Limitations

1. Request timeout is fixed at 5 seconds (could be configurable)
2. No retry mechanism for Kafka failures
3. No dead letter queue for failed messages
4. In-memory whitelist is lost on service restart
5. No distributed tracing for debugging

## Running the System

See README.md for detailed instructions. Quick start:

```bash
# 1. Start Kafka
docker-compose up -d

# 2. Start Authentication Service
./gradlew :authentication-service:bootRun

# 3. Start CSMS Backend (in another terminal)
./gradlew :csms-backend:bootRun

# 4. Test
./test-api.sh
```

## Git Commit Strategy

This implementation follows a logical commit structure:
- Initial project setup
- Common module with shared models
- CSMS Backend service implementation
- Authentication service implementation
- Docker Compose setup
- Unit tests
- Documentation and test scripts
