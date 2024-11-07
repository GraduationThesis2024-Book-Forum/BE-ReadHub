package com.iuh.fit.readhub.dto;

import com.iuh.fit.readhub.models.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String username;
    private String displayName;
    private String email;
    private String urlAvatar;
    private UserRole role;
    private LocalDateTime createdAt;
}