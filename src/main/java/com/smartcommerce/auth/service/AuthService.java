package com.smartcommerce.auth.service;

import com.smartcommerce.auth.dto.request.LoginRequest;
import com.smartcommerce.auth.dto.request.RegisterRequest;
import com.smartcommerce.auth.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
