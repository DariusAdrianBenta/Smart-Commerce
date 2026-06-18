package com.smartcommerce.address.service.impl;

import com.smartcommerce.address.dto.request.AddressRequest;
import com.smartcommerce.address.dto.response.AddressResponseDTO;
import com.smartcommerce.address.entity.Address;
import com.smartcommerce.address.mapper.AddressMapper;
import com.smartcommerce.address.repository.AddressRepository;
import com.smartcommerce.address.service.AddressService;
import com.smartcommerce.exception.AddressNotFoundException;
import com.smartcommerce.exception.CannotDeleteDefaultAddressException;
import com.smartcommerce.exception.UserNotFoundException;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    public List<AddressResponseDTO> getMyAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(addressMapper::toDTO)
                .toList();
    }

    @Override
    public AddressResponseDTO addAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        boolean isFirst = !addressRepository.existsByUserId(userId);

        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .province(request.getProvince())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(isFirst)
                .build();

        return addressMapper.toDTO(addressRepository.save(address));
    }

    @Override
    public AddressResponseDTO updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = getAddressOwnedByUser(userId, addressId);
        address.setStreet(request.getStreet());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setProvince(request.getProvince());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        return addressMapper.toDTO(addressRepository.save(address));
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        Address address = getAddressOwnedByUser(userId, addressId);
        if (address.isDefault()) {
            throw new CannotDeleteDefaultAddressException();
        }
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        Address address = getAddressOwnedByUser(userId, addressId);
        if (address.isDefault()) {
            return;
        }
        addressRepository.clearDefaultForUser(userId);
        address.setDefault(true);
        addressRepository.save(address);
    }

    private Address getAddressOwnedByUser(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        if (!address.getUser().getId().equals(userId)) {
            throw new AddressNotFoundException(addressId);
        }
        return address;
    }
}
