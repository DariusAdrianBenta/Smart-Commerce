package com.smartcommerce.address.service;

import com.smartcommerce.address.dto.request.AddressRequest;
import com.smartcommerce.address.dto.response.AddressResponseDTO;

import java.util.List;

public interface AddressService {
    List<AddressResponseDTO> getMyAddresses(Long userId);
    AddressResponseDTO addAddress(Long userId, AddressRequest request);
    AddressResponseDTO updateAddress(Long userId, Long addressId, AddressRequest request);
    void deleteAddress(Long userId, Long addressId);
    void setDefault(Long userId, Long addressId);
}
