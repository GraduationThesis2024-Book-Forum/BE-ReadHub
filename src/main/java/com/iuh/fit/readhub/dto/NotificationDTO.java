package com.iuh.fit.readhub.dto;

import com.iuh.fit.readhub.constants.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private Map<String, Object> data;  // Store additional data as Map
    private boolean read;
    private LocalDateTime createdAt;
}