# Notification Service

Notification service for the HiveMind platform. Consumes events from Kafka and delivers notifications to users via push, email, or in-app channels.

## Details

| Property | Value |
|----------|-------|
| **Port** | `8086` |
| **Database** | MongoDB |
| **Messaging** | Kafka |
| **Role** | Notifications |

## Build & Run

```bash
# Build
mvn clean package

# Run
java -jar target/*.jar

# Docker
docker build -t hivemind/notification-service .
```

## Links

- [Main Repository](https://github.com/AhmedNijim92/hivemind-backend)
