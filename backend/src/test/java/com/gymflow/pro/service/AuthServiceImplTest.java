package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.LoginRequest;
import com.gymflow.pro.dto.response.AuthResponse;
import com.gymflow.pro.dto.response.UserResponse;
import com.gymflow.pro.entity.RefreshToken;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.entity.enums.UserRole;
import com.gymflow.pro.exception.ResourceNotFoundException;
import com.gymflow.pro.mapper.UserMapper;
import com.gymflow.pro.repository.RefreshTokenRepository;
import com.gymflow.pro.repository.UserRepository;
import com.gymflow.pro.security.JwtTokenProvider;
import com.gymflow.pro.service.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .name("Jane Doe")
                .email("jane@example.com")
                .passwordHash("hashed-password")
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        httpServletRequest = mock(HttpServletRequest.class);
    }

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail(user.getEmail());
        request.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), user.getRole().name())).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(600_000L);
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(300_000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().id(user.getId()).email(user.getEmail()).build());

        AuthResponse response = authService.login(request, httpServletRequest);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(300);
        assertThat(response.getUser().getEmail()).isEqualTo(user.getEmail());

        verify(auditService).log(eq(user), eq("LOGIN_SUCCESS"), eq("User"), eq(user.getId().toString()), anyString(), eq(httpServletRequest));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_shouldThrowBadCredentialsException_andAuditFailure_whenCredentialsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jane@example.com");
        request.setPassword("wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request, httpServletRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(auditService).log(eq("jane@example.com"), eq("LOGIN_FAILED"), eq("User"), isNull(), anyString(), eq(httpServletRequest));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_shouldReturnNewAuthResponse_andRevokeOldToken_whenTokenValid() {
        String rawRefreshToken = "raw-refresh-token";
        RefreshToken storedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash(hash(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))).thenReturn(Optional.of(storedToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name())).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), user.getRole().name())).thenReturn("new-refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(600_000L);
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(300_000L);
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().id(user.getId()).build());

        AuthResponse response = authService.refresh(rawRefreshToken, httpServletRequest);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(storedToken.isRevoked()).isTrue();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0)).isEqualTo(storedToken);
        verify(auditService).log(eq(user), eq("TOKEN_REFRESH"), eq("User"), eq(user.getId().toString()), anyString(), eq(httpServletRequest));
    }

    @Test
    void refresh_shouldThrowBadCredentialsException_whenTokenNotFound() {
        String rawRefreshToken = "unknown-token";
        when(refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(rawRefreshToken, httpServletRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_shouldThrowBadCredentialsException_whenTokenRevoked() {
        String rawRefreshToken = "revoked-token";
        RefreshToken storedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash(hash(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(true)
                .build();
        when(refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refresh(rawRefreshToken, httpServletRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("expired or revoked");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_shouldThrowBadCredentialsException_whenTokenExpired() {
        String rawRefreshToken = "expired-token";
        RefreshToken storedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash(hash(rawRefreshToken))
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refresh(rawRefreshToken, httpServletRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("expired or revoked");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logout_shouldRevokeToken_andAudit_whenTokenExists() {
        String rawRefreshToken = "logout-token";
        RefreshToken storedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash(hash(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))).thenReturn(Optional.of(storedToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.logout(rawRefreshToken, httpServletRequest);

        assertThat(storedToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(storedToken);
        verify(auditService).log(eq(user), eq("LOGOUT"), eq("User"), eq(user.getId().toString()), anyString(), eq(httpServletRequest));
    }

    @Test
    void logout_shouldDoNothing_whenTokenNotFound() {
        String rawRefreshToken = "unknown-token";
        when(refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))).thenReturn(Optional.empty());

        authService.logout(rawRefreshToken, httpServletRequest);

        verify(refreshTokenRepository, never()).save(any());
        verify(auditService, never()).log(any(User.class), anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void me_shouldReturnUserResponse_whenUserExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().id(user.getId()).email(user.getEmail()).build());

        var response = authService.me(user.getEmail());

        assertThat(response.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void me_shouldThrowResourceNotFoundException_whenUserMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.me("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
