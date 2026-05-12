package com.smartcommerce.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private long expiresIn;
    private Long userId;
    private String email;
    private String role;
}
