# Notification Service

> HiveMind Event-Driven Notification Microservice

## Overview

The notification-service consumes Kafka events from other services and generates notifications for users. It provides APIs to retrieve, count, and mark notifications as read. All notifications are stored in MongoDB.

## Service Info

| Property | Value |
|----------|-------|
| Port | 8086 |
| Service Name | `notification-service` |
| Database | MongoDB |
| Database Name | `notifications` |
| Spring Boot | 3.3.5 |
| Spring Cloud | 2023.0.3 |
| Java | 17 |

## Architecture

```
Kafka Topics
  │
  ├── user-created-topic ──────┐
  ├── post-created-topic ──────┤
  └── meeting-started-topic ───┤
                               ▼
                   NotificationEventConsumer
                         │
                         ▼
                   INotificationService
                         │
                         ▼
                   NotificationRepository (MongoDB)
                         │
                         ▼
                   NotificationController ──→ Client (via Gateway)
```

## API Endpoints

Base path: `/api/v1/notifications`
All endpoints require JWT (X-User-Id header injected by gateway).

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Get all notifications for user |
| GET | `/unread` | Get unread notifications |
| GET | `/unread/count` | Get unread count |
| PUT | `/{notificationId}/read` | Mark single notification as read |
| PUT | `/read-all` | Mark all notifications as read |

### Request/Response Examples

#### GET /api/v1/notifications
```json
// Response (200)
[
  {
    "id": "mongo-object-id",
    "userId": "uuid",
    "type": "POST_CREATED",
    "title": "New Post",
    "message": "Ahmed posted in Backend Team",
    "referenceId": "post-uuid",
    "read": false,
    "createdAt": "2025-06-04T10:30:00"
  }
]
```

#### GET /api/v1/notifications/unread/count
```json
// Response (200)
5
```

## Data Model

### Notification (MongoDB collection: `notifications`)

| Field | Type | Description |
|-------|------|-------------|
| id | String | MongoDB ObjectId (auto-generated) |
| user_id | UUID | Target user |
| type | String | Notification category |
| title | String | Notification title |
| message | String | Notification body |
| reference_id | UUID | Related entity ID |
| read | boolean | Read status |
| created_at | LocalDateTime | Creation timestamp |

### Notification Types

| Type | Trigger | Title |
|------|---------|-------|
| USER_CREATED | User registered | Welcome notification |
| POST_CREATED | Post created in group | New post notification |
| MEETING_STARTED | Meeting activated | Meeting started notification |
| GROUP_CREATED | Group created | Group creation confirmation |

## Kafka Events

### Consumes

| Topic | Event Class | Action |
|-------|-------------|--------|
| `user-created-topic` | UserCreatedEvent | Creates welcome notification for new user |
| `post-created-topic` | PostCreatedEvent | Creates notification for group members |
| `meeting-started-topic` | MeetingStartedEvent | Creates notification for group members |

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| MONGODB_URI | mongodb://localhost:27017/notifications | MongoDB connection string |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka brokers |
| EUREKA_SERVER | http://localhost:8761/eureka | Eureka URL |

### Kafka Consumer Config

- Consumer group: `notification-service`
- Auto-offset-reset: `earliest`
- Deserialization: `JsonDeserializer` with trusted packages `*`

## Dependencies

- spring-boot-starter-web
- spring-boot-starter-data-mongodb
- spring-boot-starter-validation
- spring-boot-starter-actuator
- spring-cloud-starter-netflix-eureka-client
- spring-cloud-starter-config
- spring-kafka
- hivemind-common (1.0.0)
- lombok

## Running Locally

```bash
# Prerequisites: MongoDB running on port 27017, Kafka on 9092
cd microservices/notification-service
mvn spring-boot:run
```

MongoDB creates the `notifications` collection automatically on first write.

## Future Improvements

- Push notifications via WebSocket/SSE for real-time delivery
- Email notification channel
- Notification preferences (user can opt out per type)
- Batch notifications (avoid N notifications for N posts in quick succession)
