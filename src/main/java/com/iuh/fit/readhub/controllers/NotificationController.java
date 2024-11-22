package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.models.UserDevice;
import com.iuh.fit.readhub.repositories.UserDeviceRepository;
import com.iuh.fit.readhub.services.FCMService;
import com.iuh.fit.readhub.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final FCMService fcmService;
    private final UserService userService;

    // Use a Set to track recent registrations
    private final Set<String> recentRegistrations = Collections.synchronizedSet(new HashSet<>());

    public NotificationController(FCMService fcmService, UserService userService) {
        this.fcmService = fcmService;
        this.userService = userService;
    }

    @PostMapping("/register-device")
    public ResponseEntity<ApiResponse<?>> registerDevice(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String fcmToken = request.get("fcmToken");

            // Check if this is a duplicate request
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
                // Remove from recent registrations after processing
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
}