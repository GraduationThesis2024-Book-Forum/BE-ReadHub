package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.dto.UserResponse;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.UserRepository;
import com.iuh.fit.readhub.security.JwtUtil;
import com.iuh.fit.readhub.services.AuthenService;
import com.iuh.fit.readhub.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/user")
public class UserController {
    @Autowired
    private AuthenService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
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


}
