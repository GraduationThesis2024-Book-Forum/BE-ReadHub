package com.iuh.fit.readhub.services;

import com.google.firebase.messaging.*;
import com.iuh.fit.readhub.models.UserDevice;
import com.iuh.fit.readhub.repositories.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FCMService {
    private final FirebaseMessaging firebaseMessaging;
    private final UserDeviceRepository userDeviceRepository;

    public void registerDevice(Long userId, String fcmToken) {
        try {
            if (fcmToken == null || fcmToken.isEmpty()) {
                log.warn("Token không hợp lệ cho user: {}", userId);
                return;
            }

            userDeviceRepository.findByFcmToken(fcmToken).ifPresentOrElse(
                    device -> {
                        device.setUserId(userId);
                        device.updateLastUsedAt();
                        userDeviceRepository.save(device);
                        log.info("Cập nhật token cho user: {}", userId);
                    },
                    () -> {
                        UserDevice newDevice = UserDevice.builder()
                                .userId(userId)
                                .fcmToken(fcmToken)
                                .build();
                        userDeviceRepository.save(newDevice);
                        log.info("Đăng ký token mới cho user: {}", userId);
                    }
            );
        } catch (Exception e) {
            log.error("Lỗi đăng ký thiết bị: {}", e.getMessage());
            throw new RuntimeException("Không thể đăng ký thiết bị", e);
        }
    }

    public void sendNotification(Long userId, String title, String body, Map<String, String> data) {
        try {
            List<String> tokens = userDeviceRepository.findByUserId(userId)
                    .stream()
                    .map(UserDevice::getFcmToken)
                    .filter(token -> token != null && !token.isEmpty())
                    .collect(Collectors.toList());

            if (tokens.isEmpty()) {
                log.warn("Không tìm thấy token cho user: {}", userId);
                return;
            }

            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .setToken(tokens.get(0)) // Gửi cho token đầu tiên
                    .build();

            try {
                String response = firebaseMessaging.send(message);
                log.info("Gửi thông báo thành công: {}", response);
            } catch (FirebaseMessagingException e) {
                log.error("Lỗi gửi thông báo: {}", e.getMessage());
                handleMessagingError(e, tokens.get(0));
            }

        } catch (Exception e) {
            log.error("Lỗi không xác định: {}", e.getMessage());
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