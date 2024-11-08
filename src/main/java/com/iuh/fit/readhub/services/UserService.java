package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.UserRepository;
import com.iuh.fit.readhub.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseGet(() -> userRepository.findByUsernameIgnoreCase(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng")));
        return user;
    }
}
