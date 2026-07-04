package com.gymflow.pro.dto.response;

import com.gymflow.pro.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private String phone;
    private String avatarUrl;
    private boolean active;
    private LocalDateTime createdAt;
}
