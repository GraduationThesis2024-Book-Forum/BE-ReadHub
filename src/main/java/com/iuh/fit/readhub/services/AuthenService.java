package com.iuh.fit.readhub.services;


import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.models.UserRole;
import com.iuh.fit.readhub.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthenService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerForUser(String email, String username, String password) {
//        if (userRepository.existsByEmail(email)) {
//            throw new IllegalStateException("Email đã được đăng ký");
//        }

        String encodedPassword = passwordEncoder.encode(password);
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(encodedPassword);
        newUser.setRole(UserRole.USER);
        newUser.setCreatedAt(LocalDateTime.now());

        userRepository.save(newUser);

        return "Đăng ký thành công!";
    }
}
