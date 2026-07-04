package com.gymflow.pro.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        String secret = Base64.getEncoder().encodeToString("test-secret-key-must-be-long-enough-for-hs256".getBytes());
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpirationMs", 3_600_000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpirationMs", 86_400_000L);
        ReflectionTestUtils.invokeMethod(jwtTokenProvider, "init");
    }

    @Test
    void generateAccessToken_shouldProduceValidTokenWithClaims() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "jane@example.com", "ADMIN");

        assertThat(jwtTokenProvider.isValid(token)).isTrue();
        assertThat(jwtTokenProvider.getEmail(token)).isEqualTo("jane@example.com");
        assertThat(jwtTokenProvider.getRole(token)).isEqualTo("ADMIN");
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId);
    }

    @Test
    void generateRefreshToken_shouldProduceValidToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateRefreshToken(userId, "jane@example.com", "STUDENT");

        assertThat(jwtTokenProvider.isValid(token)).isTrue();
        Claims claims = jwtTokenProvider.parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo("jane@example.com");
    }

    @Test
    void isValid_shouldReturnFalse_forMalformedToken() {
        assertThat(jwtTokenProvider.isValid("not-a-valid-token")).isFalse();
    }

    @Test
    void getExpirationGetters_shouldReturnConfiguredValues() {
        assertThat(jwtTokenProvider.getAccessTokenExpirationMs()).isEqualTo(3_600_000L);
        assertThat(jwtTokenProvider.getRefreshTokenExpirationMs()).isEqualTo(86_400_000L);
    }
}
