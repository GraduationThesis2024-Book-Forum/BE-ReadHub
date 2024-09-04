package com.iuh.fit.readhub.services;


import com.iuh.fit.readhub.constants.ValidationConstants;
import com.iuh.fit.readhub.dto.RegistrationResponse;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.models.UserRole;
import com.iuh.fit.readhub.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class AuthenService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegistrationResponse registerForUser(String email, String username, String password) {
        if (userRepository.existsByEmail(email)) {
            return new RegistrationResponse(false, "Email đã được đăng ký");
        }

        if (!Pattern.matches(ValidationConstants.EMAIL_REGEX, email)) {
            return new RegistrationResponse(false, "Email không hợp lệ");
        }

        if (!Pattern.matches(ValidationConstants.PASSWORD_REGEX, password)) {
            return new RegistrationResponse(false, "Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm chữ cái, chữ số và ký tự đặc biệt");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(encodedPassword);
        newUser.setRole(UserRole.USER);
        newUser.setCreatedAt(LocalDateTime.now());

        userRepository.save(newUser);

        return new RegistrationResponse(true, "Đăng ký thành công!");
    }

}
