package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.UserRepository;
import com.iuh.fit.readhub.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public User getCurrentUser(Authentication authentication) {
        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseGet(() -> userRepository.findByUsernameIgnoreCase(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng")));
        return user;
    }

    public String uploadAvatar(MultipartFile file) {
        return s3Service.uploadFile(file);
    }
}
