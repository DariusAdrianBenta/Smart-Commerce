package com.smartcommerce.user.dto.request;

import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phone;

    @Past
    private LocalDate birthDate;
}
