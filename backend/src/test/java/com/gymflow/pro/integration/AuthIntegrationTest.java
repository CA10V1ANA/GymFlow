package com.gymflow.pro.integration;

import com.gymflow.pro.dto.request.LoginRequest;
import com.gymflow.pro.dto.request.RefreshTokenRequest;
import com.gymflow.pro.dto.response.AuthResponse;
import com.gymflow.pro.exception.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full login/refresh/me flow exercised over real HTTP against the random port,
 * using the admin user seeded by V7__seed_data.sql (admin@gymflow.com / Admin@123).
 */
class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    @Test
    void login_withSeededAdminCredentials_returnsAccessAndRefreshTokens() {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest("admin@gymflow.com", "Admin@123"), AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getAccessToken()).isNotBlank();
        assertThat(body.getRefreshToken()).isNotBlank();
        assertThat(body.getTokenType()).isEqualTo("Bearer");
        assertThat(body.getExpiresIn()).isPositive();
        assertThat(body.getUser()).isNotNull();
        assertThat(body.getUser().getEmail()).isEqualTo("admin@gymflow.com");
        assertThat(body.getUser().getRole().name()).isEqualTo("ADMIN");
    }

    @Test
    void login_withBadCredentials_returnsUnauthorized() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest("admin@gymflow.com", "wrong-password"), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    void login_withUnknownEmail_returnsUnauthorized() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest("nobody@gymflow.com", "Admin@123"), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_withInvalidEmailFormat_returnsBadRequest() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest("not-an-email", "Admin@123"), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).containsKey("email");
    }

    @Test
    void me_withBearerTokenFromLogin_returnsAuthenticatedUser() {
        AuthResponse auth = login("admin@gymflow.com", "Admin@123");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getAccessToken());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/auth/me", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("admin@gymflow.com").contains("ADMIN");
    }

    @Test
    void me_withoutBearerToken_isUnauthorized() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/auth/me", String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void refresh_withValidRefreshToken_returnsNewTokenPair() {
        AuthResponse auth = login("admin@gymflow.com", "Admin@123");

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(auth.getRefreshToken());

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/refresh", refreshRequest, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getAccessToken()).isNotBlank();
        assertThat(body.getRefreshToken()).isNotBlank();
        assertThat(body.getRefreshToken()).isNotEqualTo(auth.getRefreshToken());
    }

    @Test
    void refresh_withAlreadyUsedRefreshToken_isRejected() {
        AuthResponse auth = login("admin@gymflow.com", "Admin@123");

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(auth.getRefreshToken());

        // First use revokes the token.
        ResponseEntity<AuthResponse> first = restTemplate.postForEntity(
                "/api/auth/refresh", refreshRequest, AuthResponse.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Second use of the same (now revoked) refresh token must fail.
        ResponseEntity<ErrorResponse> second = restTemplate.postForEntity(
                "/api/auth/refresh", refreshRequest, ErrorResponse.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private AuthResponse login(String email, String password) {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest(email, password), AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }
}
