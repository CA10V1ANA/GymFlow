package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.UserResponse;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .name("Alice")
                .email("alice@example.com")
                .role(UserRole.ADMIN)
                .phone("11999998888")
                .active(true)
                .build();

        UserResponse response = userMapper.toResponse(user);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void toResponse_shouldReturnNull_whenUserNull() {
        assertThat(userMapper.toResponse(null)).isNull();
    }
}
