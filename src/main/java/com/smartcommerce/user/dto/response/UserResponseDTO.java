package com.smartcommerce.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate birthDate;
    private String email;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;
}
