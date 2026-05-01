package com.hivemind.notification.consumer;

import com.hivemind.common.event.MeetingStartedEvent;
import com.hivemind.common.event.PostCreatedEvent;
import com.hivemind.common.event.UserCreatedEvent;
import com.hivemind.notification.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer
{
    private final INotificationService notificationService;

    @KafkaListener(topics = "user-created-topic", groupId = "notification-service")
    public void handleUserCreated(UserCreatedEvent event)
    {
        log.info("Received UserCreatedEvent for userId: {}", event.getUserId());
        notificationService.createNotification(
                event.getUserId(),
                "USER_CREATED",
                "Welcome to HiveMind!",
                "Your account has been created successfully. Join or create a group to get started.",
                event.getUserId()
        );
    }

    @KafkaListener(topics = "post-created-topic", groupId = "notification-service")
    public void handlePostCreated(PostCreatedEvent event)
    {
        log.info("Received PostCreatedEvent for postId: {}", event.getPostId());
        // In a real scenario, notify group members. Here we notify the author.
        notificationService.createNotification(
                event.getAuthorId(),
                "POST_CREATED",
                "Post published",
                "Your post has been published to the group.",
                event.getPostId()
        );
    }

    @KafkaListener(topics = "meeting-started-topic", groupId = "notification-service")
    public void handleMeetingStarted(MeetingStartedEvent event)
    {
        log.info("Received MeetingStartedEvent for meetingId: {}", event.getMeetingId());
        notificationService.createNotification(
                event.getHostId(),
                "MEETING_STARTED",
                "Meeting started",
                "Your meeting '" + event.getTitle() + "' has started.",
                event.getMeetingId()
        );
    }
}
