package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.NotificationDTO;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.models.UserDevice;
import com.iuh.fit.readhub.repositories.UserDeviceRepository;
import com.iuh.fit.readhub.services.FCMService;
import com.iuh.fit.readhub.services.NotificationService;
import com.iuh.fit.readhub.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final FCMService fcmService;
    private final UserService userService;
    private final NotificationService notificationService;

    // Use a Set to track recent registrations
    private final Set<String> recentRegistrations = Collections.synchronizedSet(new HashSet<>());

    public NotificationController(FCMService fcmService, UserService userService, NotificationService notificationService) {
        this.fcmService = fcmService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/unread-notifications-count")
    public ResponseEntity<ApiResponse<?>> getUnreadNotificationsCount(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            long count = notificationService.getUnreadCount(user.getUserId());

            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Unread notifications count fetched successfully")
                    .status(200)
                    .data(count)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error fetching unread notifications count: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @PostMapping("/register-device")
    public ResponseEntity<ApiResponse<?>> registerDevice(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String fcmToken = request.get("fcmToken");
            if (!recentRegistrations.add(fcmToken)) {
                return ResponseEntity.ok(ApiResponse.builder()
                        .message("Device already registered")
                        .status(200)
                        .success(true)
                        .build());
            }
            try {
                User user = userService.getCurrentUser(authentication);
                fcmService.registerDevice(user.getUserId(), fcmToken);

                return ResponseEntity.ok(ApiResponse.builder()
                        .message("Device registered successfully")
                        .status(200)
                        .success(true)
                        .build());
            } finally {
                recentRegistrations.remove(fcmToken);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error registering device: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getUserNotifications(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            List<NotificationDTO> notifications = notificationService.getUserNotifications(user.getUserId());

            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Notifications fetched successfully")
                    .status(200)
                    .data(notifications)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error fetching notifications: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<?>> markAsRead(
            @PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            notificationService.markAsRead(user.getUserId(), id);

            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Notification marked as read")
                    .status(200)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error marking notification as read: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<?>> markAllAsRead(Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            notificationService.markAllAsRead(user.getUserId());

            return ResponseEntity.ok(ApiResponse.builder()
                    .message("All notifications marked as read")
                    .status(200)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error marking notifications as read: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }
}