package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.UserResponse;
import com.iuh.fit.readhub.dto.request.ChangePasswordRequest;
import com.iuh.fit.readhub.dto.request.UserRequest;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.UserRepository;
import com.iuh.fit.readhub.security.JwtUtil;
import com.iuh.fit.readhub.services.AuthenService;
import com.iuh.fit.readhub.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private AuthenService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllUser() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.builder().data(users)
                    .message("Không có người dùng nào")
                    .status(HttpStatus.OK.value())
                    .success(true)
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.builder().data(users)
                .message("Lấy danh sách người dùng thành công")
                .status(HttpStatus.OK.value())
                .success(true)
                .build());
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            if (username == null || username.isEmpty()) {
                return new ResponseEntity<>("Không thể trích xuất username từ JWT", HttpStatus.UNAUTHORIZED);
            }
            Optional<User> userOptional = userRepository.findByEmail(username).or(
                    () -> userRepository.findByUsername(username));
            if (!userOptional.isPresent()) {
                return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);
            }
            User user = userOptional.get();
            // Check and update ban status if expired
            if (Boolean.TRUE.equals(user.getForumInteractionBanned()) &&
                    user.getForumBanExpiresAt() != null &&
                    user.getForumBanExpiresAt().isBefore(LocalDateTime.now())) {
                user.setForumInteractionBanned(false);
                user.setForumBanReason(null);
                user.setForumBanExpiresAt(null);
                userRepository.save(user);
            }

            UserResponse userResponse = UserResponse.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .role(user.getRole().toString())
                    .urlAvatar(user.getUrlAvatar())
                    // Add all ban information
                    .forumInteractionBanned(user.getForumInteractionBanned())
                    .forumBanReason(user.getForumBanReason())
                    .forumBanExpiresAt(user.getForumBanExpiresAt())
                    // Add these missing fields
                    .forumCreationBanned(user.getForumCreationBanned())
                    .forumCreationBanReason(user.getForumCreationBanReason())
                    .forumCreationBanExpiresAt(user.getForumCreationBanExpiresAt())
                    //
                    .forumCommentBanned(user.getForumCommentBanned())
                    .forumCommentBanExpiresAt(user.getForumCommentBanExpiresAt())
                    .forumJoinBanned(user.getForumJoinBanned())
                    .forumJoinBanExpiresAt(user.getForumJoinBanExpiresAt())
                    .build();

            return new ResponseEntity<>(userResponse, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Có lỗi xảy ra: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest) {
        try {
            Optional<User> userOptional = userRepository.findById(id);
            if (!userOptional.isPresent()) {
                return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);
            }
            User userUpdate = userOptional.get();
            userUpdate.setFullName(userRequest.getFullName());
            userUpdate.setUsername(userRequest.getUsername());
            userRepository.save(userUpdate);
            return new ResponseEntity<>("Cập nhật người dùng thành công", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Có lỗi xảy ra: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            Optional<User> userOptional = userRepository.findById(Long.valueOf(id));
            if (!userOptional.isPresent()) {
                return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);
            }
            userRepository.delete(userOptional.get());
            return new ResponseEntity<>("Xóa người dùng thành công", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Có lỗi xảy ra: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //upload avatar lên s3 và db
    @PostMapping("/{userId}/upload-avatar")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> uploadAvatar(@ModelAttribute MultipartFile avatar, @PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).get();
            String urlAvatar = userService.uploadAvatar(avatar);
            user.setUrlAvatar(urlAvatar);
            userRepository.save(user);
            return new ResponseEntity<>("Upload avatar thành công", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Có lỗi xảy ra: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
//    reset password
    @PutMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable Long userId, @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            userService.resetPassword(userId, changePasswordRequest.getPassword(), changePasswordRequest.getNewPassword());
            return new ResponseEntity<>("Đổi mật khẩu thành công", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Có lỗi xảy ra: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
