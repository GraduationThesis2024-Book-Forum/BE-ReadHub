package com.iuh.fit.readhub.services;

import com.google.firebase.messaging.*;
import com.iuh.fit.readhub.constants.NotificationType;
import com.iuh.fit.readhub.models.UserDevice;
import com.iuh.fit.readhub.repositories.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FCMService {
    private static final Logger logger =LoggerFactory.getLogger(FCMService.class);
    private final FirebaseMessaging firebaseMessaging;
    private final UserDeviceRepository userDeviceRepository;
    private final NotificationService notificationService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized void registerDevice(Long userId, String fcmToken) {
        try {
            logger.info("Registering device for user {} with token {}", userId, fcmToken);

            // Delete existing token first
            userDeviceRepository.deleteByFcmToken(fcmToken);

            // Create new device
            UserDevice device = UserDevice.builder()
                    .userId(userId)
                    .fcmToken(fcmToken)
                    .lastUsedAt(LocalDateTime.now())
                    .build();

            device = userDeviceRepository.save(device);
            logger.info("Device registered successfully. ID: {}", device.getId());

        } catch (DataIntegrityViolationException e) {
            logger.error("Duplicate token detected. Retrying registration...");
            // If duplicate, try to update existing
            userDeviceRepository.findByFcmToken(fcmToken)
                    .ifPresent(existingDevice -> {
                        existingDevice.setUserId(userId);
                        existingDevice.setLastUsedAt(LocalDateTime.now());
                        userDeviceRepository.save(existingDevice);
                        logger.info("Updated existing device instead of creating new one");
                    });
        } catch (Exception e) {
            logger.error("Failed to register device for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to register device", e);
        }
    }

    public void sendNotification(Long userId, String title, String body, Map<String, String> data) {
        try {
            notificationService.saveNotification(
                    userId,
                    title,
                    body,
                    NotificationType.valueOf(data.get("type")),
                    data
            );
            List<UserDevice> devices = userDeviceRepository.findByUserId(userId);

            if (devices.isEmpty()) {
                logger.warn("No devices found for user {}", userId);
                return;
            }

            logger.info("Found {} devices for user {}", devices.size(), userId);

            for (UserDevice device : devices) {
                try {
                    Message message = Message.builder()
                            .setNotification(Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build())
                            .putAllData(data)
                            .setToken(device.getFcmToken())
                            .build();

                    String response = firebaseMessaging.send(message);
                    logger.info("Notification sent to device {}. Response: {}", device.getId(), response);

                    // Update last used time
                    device.setLastUsedAt(LocalDateTime.now());
                    userDeviceRepository.save(device);

                } catch (FirebaseMessagingException e) {
                    if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                        logger.warn("Token expired for device {}. Deleting...", device.getId());
                        userDeviceRepository.delete(device);
                    } else {
                        logger.error("Failed to send notification to device {}", device.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to send notifications to user {}", userId, e);
        }
    }

    private void handleSendResponse(BatchResponse response, List<String> tokens) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            String token = tokens.get(i);

            if (!sendResponse.isSuccessful()) {
                handleFailedToken(token, sendResponse.getException());
            }
        }
    }

    private void handleFailedToken(String token, FirebaseMessagingException e) {
        if (e != null && e.getMessagingErrorCode() != null) {
            switch (e.getMessagingErrorCode()) {
                case INVALID_ARGUMENT:
                case UNREGISTERED:
                case SENDER_ID_MISMATCH:
                    log.warn("Xóa token không hợp lệ: {}", token);
                    userDeviceRepository.deleteByFcmToken(token);
                    break;
                default:
                    log.error("Lỗi gửi tới token {}: {}", token, e.getMessage());
            }
        }
    }

    private void handleFirebaseError(FirebaseMessagingException e, List<String> tokens) {
        switch (e.getMessagingErrorCode()) {
            case INVALID_ARGUMENT:
                log.error("Token không hợp lệ");
                tokens.forEach(userDeviceRepository::deleteByFcmToken);
                break;
            case SENDER_ID_MISMATCH:
                log.error("Sender ID không khớp");
                break;
            case QUOTA_EXCEEDED:
                log.error("Vượt quá giới hạn quota");
                break;
            case UNAVAILABLE:
                log.error("Dịch vụ Firebase không khả dụng");
                break;
            default:
                log.error("Lỗi Firebase khác: {}", e.getMessage());
        }
    }

    private void handleMessagingError(FirebaseMessagingException e, String token) {
        if (e.getErrorCode() != null) {
            switch (e.getErrorCode().name()) {
                case "INVALID_ARGUMENT":
                case "UNREGISTERED":
                    log.warn("Xóa token không hợp lệ: {}", token);
                    userDeviceRepository.deleteByFcmToken(token);
                    break;
                case "UNAVAILABLE":
                    log.error("FCM service không khả dụng");
                    break;
                default:
                    log.error("Lỗi FCM khác: {}", e.getMessage());
            }
        }
    }
}