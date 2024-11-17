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
        userDeviceRepository.findByFcmToken(fcmToken).ifPresentOrElse(
                device -> {
                    device.setUserId(userId);
                    device.updateLastUsedAt();
                    userDeviceRepository.save(device);
                },
                () -> {
                    UserDevice newDevice = UserDevice.builder()
                            .userId(userId)
                            .fcmToken(fcmToken)
                            .build();
                    userDeviceRepository.save(newDevice);
                }
        );
    }

    public void sendNotification(Long userId, String title, String body, Map<String, String> data) {
        List<String> tokens = userDeviceRepository.findByUserId(userId)
                .stream()
                .map(UserDevice::getFcmToken)
                .collect(Collectors.toList());

        if (tokens.isEmpty()) {
            log.warn("No FCM tokens found for user: {}", userId);
            return;
        }

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendMulticast(message);
            log.info("Successfully sent message to {} devices", response.getSuccessCount());

            // Handle failed tokens
            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    String failedToken = tokens.get(i);
                    log.error("Failed to send message to token: {}", failedToken);
                    userDeviceRepository.deleteByFcmToken(failedToken);
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send firebase notification", e);
        }
    }
}