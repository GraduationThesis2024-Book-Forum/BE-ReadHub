package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.models.UserRole;
import com.iuh.fit.readhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllAdmins() {
        return userRepository.findByRole(UserRole.ADMIN);
    }

    public User getCurrentUser(Authentication authentication) {
        String identifier = authentication.getName();

        User user = userRepository.findByUsernameIgnoreCase(identifier)
                .orElseGet(() -> userRepository.findByEmailIgnoreCase(identifier)
                        .orElseThrow(() -> new RuntimeException("User not found")));

        System.out.println("Getting user for username: {}"+ user.getUserId());
        return user;
    }

    public String uploadAvatar(MultipartFile file) {
        return s3Service.uploadFile(file);
    }
//    reset password
    public void resetPassword(Long userId, String password, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
