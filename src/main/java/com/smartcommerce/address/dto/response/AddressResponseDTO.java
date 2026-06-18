package com.smartcommerce.address.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponseDTO {
    private Long id;
    private String street;
    private String addressLine2;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private boolean isDefault;
}
