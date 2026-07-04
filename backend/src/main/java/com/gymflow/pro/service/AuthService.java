package com.gymflow.pro.service;

import com.gymflow.pro.dto.request.LoginRequest;
import com.gymflow.pro.dto.response.AuthResponse;
import com.gymflow.pro.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    AuthResponse refresh(String refreshToken, HttpServletRequest httpRequest);

    void logout(String refreshToken, HttpServletRequest httpRequest);

    UserResponse me(String email);
}
