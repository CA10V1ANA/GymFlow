package com.gymflow.pro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymflow.pro.config.SecurityConfig;
import com.gymflow.pro.dto.request.LoginRequest;
import com.gymflow.pro.dto.request.RefreshTokenRequest;
import com.gymflow.pro.dto.response.AuthResponse;
import com.gymflow.pro.dto.response.UserResponse;
import com.gymflow.pro.entity.enums.UserRole;
import com.gymflow.pro.security.CustomUserDetailsService;
import com.gymflow.pro.security.JwtTokenProvider;
import com.gymflow.pro.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("secret123");
        return request;
    }

    private UserResponse userResponse() {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .name("Jane Doe")
                .email("user@example.com")
                .role(UserRole.ADMIN)
                .active(true)
                .build();
    }

    private AuthResponse authResponse() {
        return AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(userResponse())
                .build();
    }

    @Test
    void login_withValidCredentials_returnsOk() throws Exception {
        when(authService.login(any(LoginRequest.class), any())).thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void login_withMissingPassword_returnsBadRequest() throws Exception {
        LoginRequest invalid = loginRequest();
        invalid.setPassword(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void login_withInvalidEmailFormat_returnsBadRequest() throws Exception {
        LoginRequest invalid = loginRequest();
        invalid.setEmail("not-an-email");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void login_withBadCredentials_returnsUnauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class), any())).thenThrow(new BadCredentialsException("bad creds"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_withValidToken_returnsOk() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("some-refresh-token");
        when(authService.refresh(eq("some-refresh-token"), any())).thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void refresh_withMissingToken_returnsBadRequest() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.refreshToken").exists());
    }

    @Test
    void logout_withValidToken_returnsNoContent() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("some-refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "ADMIN")
    void me_whenAuthenticated_returnsOk() throws Exception {
        when(authService.me(eq("user@example.com"))).thenReturn(userResponse());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }
}
