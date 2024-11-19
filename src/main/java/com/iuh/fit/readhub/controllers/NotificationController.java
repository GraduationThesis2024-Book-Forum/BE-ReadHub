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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final FCMService fcmService;
    private final UserService userService;
    private final UserDeviceRepository userDeviceRepository;

    public NotificationController(FCMService fcmService, UserService userService, UserDeviceRepository userDeviceRepository) {
        this.fcmService = fcmService;
        this.userService = userService;
        this.userDeviceRepository = userDeviceRepository;
    }

    @PostMapping("/register-device")
    public ResponseEntity<ApiResponse<?>> registerDevice(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            System.out.println("KKKKKKKKKKK" + user.getUserId() + request.get("fcmToken"));
            fcmService.registerDevice(user.getUserId(), request.get("fcmToken"));

            List<UserDevice> devices = userDeviceRepository.findByUserId(user.getUserId());
            System.out.println("KKKKKKKKKKKKKKK2 "+  user.getUserId()+ devices.size());

            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Device registered successfully")
                    .status(200)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("Error registering device: " + e.getMessage())
                    .status(400)
                    .success(false)
                    .build());
        }
    }
}