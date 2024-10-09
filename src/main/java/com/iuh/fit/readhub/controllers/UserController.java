package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.ApiResponse;
import com.iuh.fit.readhub.dto.UserResponse;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

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
    @PreAuthorize("hasRole('ROLE_ADMIṆ') || hasRole('ROLE_USER')")
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
            UserResponse userResponse = UserResponse.builder()
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .role(user.getRole().toString())
                    .urlAvatar(user.getUrlAvatar())
                    .build();

            return new ResponseEntity<>(userResponse, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Có lỗi xảy ra: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIṆ') || hasRole('ROLE_USER')")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody User user) {
        try {
            Optional<User> userOptional = userRepository.findById(Long.valueOf(id));
            if (!userOptional.isPresent()) {
                return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);
            }
            User userUpdate = userOptional.get();
            userUpdate.setEmail(user.getEmail());
            userUpdate.setUsername(user.getUsername());
            userUpdate.setUrlAvatar(user.getUrlAvatar());
            userUpdate.setRole(user.getRole());
            userRepository.save(userUpdate);
            return new ResponseEntity<>("Cập nhật người dùng thành công", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Có lỗi xảy ra: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
