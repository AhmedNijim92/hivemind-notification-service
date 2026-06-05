# Notification Service — Code-Level Reference

## NotificationServiceApplication

**Package:** `com.hivemind.notification`

**Annotations:**
- `@SpringBootApplication` — Enables auto-configuration, component scanning, and configuration properties
- `@EnableDiscoveryClient` — Registers with Eureka service registry
- `@EnableKafka` — Enables Kafka consumer annotations

**Design Pattern:** Application Entry Point (Spring Boot convention)

### Methods

#### `main(String[] args)`
- **Signature:** `public static void main(String[] args)`
- **Logic:** `SpringApplication.run(NotificationServiceApplication.class, args)`
- **Returns:** void

---

## KafkaConsumerConfig

**Package:** `com.hivemind.notification.config`

**Annotations:**
- `@Configuration`

**Design Pattern:** Factory Method — creates generic Kafka consumer infrastructure for multiple event types

### Beans

#### `consumerFactory()`
- **Signature:** `@Bean public ConsumerFactory<String, Object> consumerFactory()`
- **Logic:** Configures consumer with:
  - `bootstrap.servers` from application properties
  - `group.id`: `"notification-service"`
  - `auto.offset.reset`: `"earliest"`
  - Key deserializer: `StringDeserializer`
  - Value deserializer: `JsonDeserializer<Object>` (generic — handles multiple event types)
  - Trusted packages: `"*"` (all packages trusted for deserialization)
- **Returns:** `DefaultKafkaConsumerFactory<String, Object>`
- **Note:** Uses generic `Object` type to consume different event types from multiple topics

#### `kafkaListenerContainerFactory()`
- **Signature:** `@Bean public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory()`
- **Logic:** Creates factory, sets the `consumerFactory()`
- **Returns:** `ConcurrentKafkaListenerContainerFactory<String, Object>`

---

## NotificationEventConsumer

**Package:** `com.hivemind.notification.consumer`

**Annotations:**
- `@Component`

**Design Pattern:** Event-Driven Consumer — listens to multiple Kafka topics and generates notifications

### Fields (Constructor Injection)

| Field | Type |
|-------|------|
| notificationService | INotificationService |

### Methods

#### `handleUserCreated(UserCreatedEvent event)`
- **Signature:** `@KafkaListener(topics = "user-created-topic", groupId = "notification-service") public void handleUserCreated(UserCreatedEvent event)`
- **Logic:**
  1. Receives `UserCreatedEvent` from Kafka
  2. Creates a welcome notification:
     - `userId` = event.getUserId()
     - `type` = "WELCOME"
     - `title` = "Welcome to HiveMind!"
     - `message` = Welcome message with user's name
     - `referenceId` = event.getUserId()
  3. Saves via `notificationService.createNotification(...)`
- **Returns:** void

#### `handlePostCreated(PostCreatedEvent event)`
- **Signature:** `@KafkaListener(topics = "post-created-topic", groupId = "notification-service") public void handlePostCreated(PostCreatedEvent event)`
- **Logic:**
  1. Receives `PostCreatedEvent` from Kafka
  2. Creates a post notification:
     - `userId` = event.getAuthorId() (notifies the post author as confirmation, or group members)
     - `type` = "POST_CREATED"
     - `title` = "New Post Published"
     - `message` = Notification about new post in the group
     - `referenceId` = event.getPostId()
  3. Saves via `notificationService.createNotification(...)`
- **Returns:** void

#### `handleMeetingStarted(MeetingStartedEvent event)`
- **Signature:** `@KafkaListener(topics = "meeting-started-topic", groupId = "notification-service") public void handleMeetingStarted(MeetingStartedEvent event)`
- **Logic:**
  1. Receives `MeetingStartedEvent` from Kafka
  2. Creates a meeting notification:
     - `userId` = event.getHostId() (or group members)
     - `type` = "MEETING_STARTED"
     - `title` = "Meeting Started"
     - `message` = Notification that a meeting has begun with the meeting title
     - `referenceId` = event.getMeetingId()
  3. Saves via `notificationService.createNotification(...)`
- **Returns:** void

---

## NotificationController

**Package:** `com.hivemind.notification.controller`

**Annotations:**
- `@RestController`
- `@RequestMapping("/api/v1/notifications")`

**Design Pattern:** Façade — exposes simplified REST API over service layer

### Fields (Constructor Injection)

| Field | Type |
|-------|------|
| notificationService | INotificationService |

### Endpoints

#### `GET /{userId}`
- **Signature:** `public ResponseEntity<List<Notification>> getNotifications(@PathVariable UUID userId)`
- **Logic:** Delegates to `notificationService.getNotifications(userId)`
- **Returns:** `List<Notification>` — all notifications for the user, ordered by createdAt DESC

#### `GET /{userId}/unread`
- **Signature:** `public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable UUID userId)`
- **Logic:** Delegates to `notificationService.getUnreadNotifications(userId)`
- **Returns:** `List<Notification>` — only unread notifications, ordered by createdAt DESC

#### `GET /{userId}/unread/count`
- **Signature:** `public ResponseEntity<Long> getUnreadCount(@PathVariable UUID userId)`
- **Logic:** Delegates to `notificationService.getUnreadCount(userId)`
- **Returns:** `Long` — count of unread notifications

#### `PUT /{notificationId}/read`
- **Signature:** `public ResponseEntity<Notification> markAsRead(@PathVariable String notificationId)`
- **Logic:** Delegates to `notificationService.markAsRead(notificationId)`
- **Returns:** `Notification` with `read = true`

#### `PUT /{userId}/read-all`
- **Signature:** `public ResponseEntity<ApiResponse> markAllAsRead(@PathVariable UUID userId)`
- **Logic:** Delegates to `notificationService.markAllAsRead(userId)`
- **Returns:** `ApiResponse` with success message

---

## Notification (Entity)

**Package:** `com.hivemind.notification.entity`

**Annotations:**
- `@Document("notifications")` — Maps to MongoDB `notifications` collection

**Note:** This service uses MongoDB (not Cassandra) — optimized for flexible document storage and rich queries on notifications.

### Fields

| Field | Type | Annotation | Description |
|-------|------|------------|-------------|
| id | String | `@Id` | MongoDB document ID (auto-generated) |
| userId | UUID | | Target user for the notification |
| type | String | | Notification type (WELCOME, POST_CREATED, MEETING_STARTED) |
| title | String | | Notification title |
| message | String | | Notification body text |
| referenceId | UUID | | ID of the referenced entity (user, post, meeting) |
| read | boolean | | Whether the notification has been read |
| createdAt | LocalDateTime | | Notification creation timestamp |

---

## NotificationRepository

**Package:** `com.hivemind.notification.repository`

**Extends:** `MongoRepository<Notification, String>`

**Design Pattern:** Repository pattern (Spring Data MongoDB)

### Methods

#### `findByUserIdOrderByCreatedAtDesc(UUID userId)`
- **Signature:** `List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId)`
- **Logic:** Finds all notifications for a user, ordered by creation time (newest first)
- **Returns:** `List<Notification>`

#### `findByUserIdAndReadFalseOrderByCreatedAtDesc(UUID userId)`
- **Signature:** `List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(UUID userId)`
- **Logic:** Finds only unread notifications for a user, ordered newest first
- **Returns:** `List<Notification>`

#### `countByUserIdAndReadFalse(UUID userId)`
- **Signature:** `long countByUserIdAndReadFalse(UUID userId)`
- **Logic:** Counts unread notifications for a user (for badge display)
- **Returns:** `long`

---

## INotificationService (Interface)

**Package:** `com.hivemind.notification.service`

### Method Signatures

| Method | Parameters | Returns |
|--------|-----------|---------|
| `createNotification` | `UUID userId, String type, String title, String message, UUID referenceId` | `Notification` |
| `getNotifications` | `UUID userId` | `List<Notification>` |
| `getUnreadNotifications` | `UUID userId` | `List<Notification>` |
| `getUnreadCount` | `UUID userId` | `long` |
| `markAsRead` | `String notificationId` | `Notification` |
| `markAllAsRead` | `UUID userId` | `void` |

---

## NotificationServiceImpl

**Package:** `com.hivemind.notification.service.impl`

**Annotations:**
- `@Service`

**Implements:** `INotificationService`

**Design Pattern:** Service Layer — encapsulates notification CRUD and state management

### Fields (Constructor Injection)

| Field | Type |
|-------|------|
| notificationRepository | NotificationRepository |

### Methods

#### `createNotification(UUID userId, String type, String title, String message, UUID referenceId)`
- **Signature:** `@Override public Notification createNotification(UUID userId, String type, String title, String message, UUID referenceId)`
- **Logic:**
  1. Builds `Notification` entity:
     - `userId` = userId
     - `type` = type
     - `title` = title
     - `message` = message
     - `referenceId` = referenceId
     - `read` = false (new notifications are always unread)
     - `createdAt` = LocalDateTime.now()
  2. Saves via `notificationRepository.save(notification)`
- **Returns:** `Notification`

#### `getNotifications(UUID userId)`
- **Signature:** `@Override public List<Notification> getNotifications(UUID userId)`
- **Logic:** Calls `notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)`
- **Returns:** `List<Notification>` — ordered newest first

#### `getUnreadNotifications(UUID userId)`
- **Signature:** `@Override public List<Notification> getUnreadNotifications(UUID userId)`
- **Logic:** Calls `notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)`
- **Returns:** `List<Notification>` — only unread, ordered newest first

#### `getUnreadCount(UUID userId)`
- **Signature:** `@Override public long getUnreadCount(UUID userId)`
- **Logic:** Calls `notificationRepository.countByUserIdAndReadFalse(userId)`
- **Returns:** `long` — number of unread notifications

#### `markAsRead(String notificationId)`
- **Signature:** `@Override public Notification markAsRead(String notificationId)`
- **Logic:**
  1. Calls `notificationRepository.findById(notificationId)`
  2. If not found → throws RuntimeException ("Notification not found")
  3. Sets `read` = true
  4. Saves updated notification
- **Returns:** `Notification` with `read = true`
- **Exceptions:** RuntimeException if notification not found

#### `markAllAsRead(UUID userId)`
- **Signature:** `@Override public void markAllAsRead(UUID userId)`
- **Logic:**
  1. Finds all unread notifications: `notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)`
  2. For each notification: sets `read` = true
  3. Saves all via `notificationRepository.saveAll(notifications)`
- **Returns:** void

---

## DTOs / Events

**Package:** `com.hivemind.notification.dto`

### UserCreatedEvent (Kafka Event — consumed)

| Field | Type | Description |
|-------|------|-------------|
| userId | UUID | New user's ID |
| mobileNumber | String | Phone number |
| name | String | Display name |
| email | String | Email address |

### PostCreatedEvent (Kafka Event — consumed)

| Field | Type | Description |
|-------|------|-------------|
| postId | UUID | New post's ID |
| groupId | UUID | Group ID |
| authorId | UUID | Author's user ID |
| authorName | String | Author's display name |
| content | String | Post content |

### MeetingStartedEvent (Kafka Event — consumed)

| Field | Type | Description |
|-------|------|-------------|
| meetingId | UUID | Meeting ID |
| groupId | UUID | Group ID |
| hostId | UUID | Host's user ID |
| title | String | Meeting title |

### ApiResponse

| Field | Type | Description |
|-------|------|-------------|
| message | String | Success/error message |
| success | boolean | Operation result |
