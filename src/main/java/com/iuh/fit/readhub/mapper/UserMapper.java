package com.iuh.fit.readhub.mapper;

import com.iuh.fit.readhub.dto.UserDTO;
import com.iuh.fit.readhub.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) return null;

        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .urlAvatar(user.getUrlAvatar())
                .build();
    }
}