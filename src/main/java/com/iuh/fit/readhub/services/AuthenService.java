package com.iuh.fit.readhub.services;


import com.iuh.fit.readhub.constants.ValidationConstants;
import com.iuh.fit.readhub.constants.ValidationMessages;
import com.iuh.fit.readhub.dto.RegistrationResponse;
import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.models.UserRole;
import com.iuh.fit.readhub.repositories.UserRepository;
import com.iuh.fit.readhub.security.JwtUtil;
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

    @Autowired
    private JwtUtil jwtUtil;


    public RegistrationResponse registerForUser(String email, String username, String password) {
        if (userRepository.existsByEmail(email)) {
            return new RegistrationResponse(false, ValidationMessages.EMAIL_ALREADY_EXISTS.getMessage(), null);
        }

        if (!Pattern.matches(ValidationConstants.EMAIL_REGEX, email)) {
            return new RegistrationResponse(false, ValidationMessages.EMAIL_INVALID.getMessage(), null);
        }

        if (username.length() < 6) {
            return new RegistrationResponse(false, ValidationMessages.USERNAME_INVALID.getMessage(), null);
        }

        if (!Pattern.matches(ValidationConstants.PASSWORD_REGEX, password)) {
            return new RegistrationResponse(false, ValidationMessages.PASSWORD_INVALID.getMessage(), null);
        }

        String encodedPassword = passwordEncoder.encode(password);
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(encodedPassword);
        newUser.setRole(UserRole.USER);
        newUser.setCreatedAt(LocalDateTime.now());

        userRepository.save(newUser);

        String token = jwtUtil.generateToken(username);

        return new RegistrationResponse(true, ValidationMessages.REGISTER_SUCCESS.getMessage(),token);
    }

}
