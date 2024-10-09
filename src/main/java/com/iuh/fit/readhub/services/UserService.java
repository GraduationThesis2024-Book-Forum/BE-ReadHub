package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.UserRepository;
import com.iuh.fit.readhub.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public User findUserProfileByJwt(String jwt) {
        jwt = jwt.substring(7);
        String email = jwtUtil.extractUsername(jwt);
        User user = userRepository.findByEmail(email).orElse(null);
        return user;
    }
}
