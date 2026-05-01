package com.hivemind.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification
{
    @Id
    private String id;

    @Field("user_id")
    private UUID userId;

    @Field("type")
    private String type; // USER_CREATED, POST_CREATED, MEETING_STARTED, GROUP_CREATED

    @Field("title")
    private String title;

    @Field("message")
    private String message;

    @Field("reference_id")
    private UUID referenceId;

    @Field("read")
    private boolean read;

    @Field("created_at")
    private LocalDateTime createdAt;
}
