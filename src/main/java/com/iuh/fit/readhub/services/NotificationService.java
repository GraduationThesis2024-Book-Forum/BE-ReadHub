package com.iuh.fit.readhub.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iuh.fit.readhub.constants.NotificationType;
import com.iuh.fit.readhub.dto.NotificationDTO;
import com.iuh.fit.readhub.models.NotificationEntity;
import com.iuh.fit.readhub.repositories.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository notificationRepository, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    public void saveNotification(Long userId, String title, String message,
                                 NotificationType type, Map<String, String> data) {
        try {
            NotificationEntity notification = NotificationEntity.builder()
                    .userId(userId)
                    .title(title)
                    .message(message)
                    .type(type)
                    .data(objectMapper.writeValueAsString(data))
                    .read(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Saved notification for user {}: {}", userId, title);
        } catch (Exception e) {
            log.error("Error saving notification:", e);
        }
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .ifPresent(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<NotificationEntity> unreadNotifications =
                notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    private NotificationDTO convertToDTO(NotificationEntity entity) {
        try {
            Map<String, Object> data = objectMapper.readValue(
                    entity.getData(),
                    new TypeReference<Map<String, Object>>() {}
            );

            return NotificationDTO.builder()
                    .id(entity.getId())
                    .title(entity.getTitle())
                    .message(entity.getMessage())
                    .type(entity.getType())
                    .data(data)
                    .read(entity.isRead())
                    .createdAt(entity.getCreatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error converting notification to DTO:", e);
            return null;
        }
    }
}