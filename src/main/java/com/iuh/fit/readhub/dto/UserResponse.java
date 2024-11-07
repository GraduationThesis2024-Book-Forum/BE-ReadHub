package com.iuh.fit.readhub.dto;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private String displayName;
    private String email;
    private String role;
    private String urlAvatar;
}
