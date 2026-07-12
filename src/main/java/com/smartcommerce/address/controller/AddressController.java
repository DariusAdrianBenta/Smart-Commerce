package com.smartcommerce.address.controller;

import com.smartcommerce.address.dto.request.AddressRequest;
import com.smartcommerce.address.dto.response.AddressResponseDTO;
import com.smartcommerce.address.service.AddressService;
import com.smartcommerce.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getMyAddresses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(addressService.getMyAddresses(user.getId()));
    }

    @PostMapping
    public ResponseEntity<AddressResponseDTO> addAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.addAddress(user.getId(), request));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            Authentication authentication,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(addressService.updateAddress(user.getId(), addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable Long addressId) {
        User user = (User) authentication.getPrincipal();
        addressService.deleteAddress(user.getId(), addressId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefault(
            Authentication authentication,
            @PathVariable Long addressId) {
        User user = (User) authentication.getPrincipal();
        addressService.setDefault(user.getId(), addressId);
        return ResponseEntity.noContent().build();
    }
}
