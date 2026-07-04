package com.gymflow.pro.entity;

import com.gymflow.pro.entity.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EntityLogicTest {

    @Test
    void attendance_getPermanenceDuration_shouldReturnNull_whenCheckOutMissing() {
        Attendance attendance = Attendance.builder()
                .checkIn(LocalDateTime.now())
                .build();

        assertThat(attendance.getPermanenceDuration()).isNull();
    }

    @Test
    void attendance_getPermanenceDuration_shouldComputeDuration_whenCheckOutPresent() {
        LocalDateTime checkIn = LocalDateTime.of(2026, 1, 1, 8, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 1, 1, 9, 15);
        Attendance attendance = Attendance.builder().checkIn(checkIn).checkOut(checkOut).build();

        assertThat(attendance.getPermanenceDuration()).isEqualTo(Duration.ofMinutes(75));
    }

    @Test
    void product_isLowStock_shouldReturnTrue_whenStockAtOrBelowMinimum() {
        Product product = Product.builder().stockQuantity(5).minStock(5).build();

        assertThat(product.isLowStock()).isTrue();
    }

    @Test
    void product_isLowStock_shouldReturnFalse_whenStockAboveMinimum() {
        Product product = Product.builder().stockQuantity(10).minStock(5).build();

        assertThat(product.isLowStock()).isFalse();
    }

    @Test
    void refreshToken_isValid_shouldReturnFalse_whenRevoked() {
        RefreshToken token = RefreshToken.builder()
                .revoked(true)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        assertThat(token.isValid()).isFalse();
    }

    @Test
    void refreshToken_isValid_shouldReturnFalse_whenExpired() {
        RefreshToken token = RefreshToken.builder()
                .revoked(false)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        assertThat(token.isValid()).isFalse();
    }

    @Test
    void refreshToken_isValid_shouldReturnTrue_whenActiveAndNotExpired() {
        RefreshToken token = RefreshToken.builder()
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        assertThat(token.isValid()).isTrue();
    }

    @Test
    void user_userDetailsMethods_shouldReflectRoleAndActiveState() {
        User user = User.builder()
                .email("jane@example.com")
                .passwordHash("hashed")
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        assertThat(user.getUsername()).isEqualTo("jane@example.com");
        assertThat(user.getPassword()).isEqualTo("hashed");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_ADMIN");
    }

    @Test
    void user_isEnabled_shouldReflectInactiveState() {
        User user = User.builder().email("x@example.com").role(UserRole.STUDENT).active(false).build();

        assertThat(user.isEnabled()).isFalse();
    }
}
