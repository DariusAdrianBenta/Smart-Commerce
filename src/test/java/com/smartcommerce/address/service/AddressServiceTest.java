package com.smartcommerce.address.service;

import com.smartcommerce.address.dto.request.AddressRequest;
import com.smartcommerce.address.dto.response.AddressResponseDTO;
import com.smartcommerce.address.entity.Address;
import com.smartcommerce.address.mapper.AddressMapper;
import com.smartcommerce.address.repository.AddressRepository;
import com.smartcommerce.address.service.impl.AddressServiceImpl;
import com.smartcommerce.exception.AddressNotFoundException;
import com.smartcommerce.exception.CannotDeleteDefaultAddressException;
import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock AddressRepository addressRepository;
    @Mock UserRepository userRepository;
    @Mock AddressMapper addressMapper;
    @InjectMocks AddressServiceImpl addressService;

    private User mockUser(Long id) {
        return User.builder().id(id).email("u@test.com").role(Role.USER).active(true).build();
    }

    private AddressRequest buildRequest() {
        AddressRequest req = new AddressRequest();
        req.setStreet("Calle Mayor 1");
        req.setAddressLine2("3º B");
        req.setCity("Madrid");
        req.setProvince("Madrid");
        req.setPostalCode("28001");
        req.setCountry("Spain");
        return req;
    }

    @Test
    void getMyAddresses_returnsListForUser() {
        Address addr = Address.builder().id(1L).city("Madrid").build();
        AddressResponseDTO dto = AddressResponseDTO.builder().id(1L).city("Madrid").build();
        when(addressRepository.findByUserId(1L)).thenReturn(List.of(addr));
        when(addressMapper.toDTO(addr)).thenReturn(dto);

        List<AddressResponseDTO> result = addressService.getMyAddresses(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("Madrid");
    }

    @Test
    void addAddress_firstAddress_becomesDefault() {
        User user = mockUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.existsByUserId(1L)).thenReturn(false);
        Address saved = Address.builder().id(1L).isDefault(true).build();
        AddressResponseDTO dto = AddressResponseDTO.builder().id(1L).isDefault(true).build();
        when(addressRepository.save(any(Address.class))).thenReturn(saved);
        when(addressMapper.toDTO(saved)).thenReturn(dto);

        AddressResponseDTO result = addressService.addAddress(1L, buildRequest());

        assertThat(result.isDefault()).isTrue();
    }

    @Test
    void addAddress_subsequentAddress_isNotDefault() {
        User user = mockUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.existsByUserId(1L)).thenReturn(true);
        Address saved = Address.builder().id(2L).isDefault(false).build();
        AddressResponseDTO dto = AddressResponseDTO.builder().id(2L).isDefault(false).build();
        when(addressRepository.save(any(Address.class))).thenReturn(saved);
        when(addressMapper.toDTO(saved)).thenReturn(dto);

        AddressResponseDTO result = addressService.addAddress(1L, buildRequest());

        assertThat(result.isDefault()).isFalse();
    }

    @Test
    void setDefault_clearsOldDefaultAndSetsNew() {
        User user = mockUser(1L);
        Address address = Address.builder().id(2L).user(user).isDefault(false).build();
        when(addressRepository.findById(2L)).thenReturn(Optional.of(address));

        addressService.setDefault(1L, 2L);

        verify(addressRepository).clearDefaultForUser(1L);
        assertThat(address.isDefault()).isTrue();
        verify(addressRepository).save(address);
    }

    @Test
    void setDefault_whenAlreadyDefault_isNoOp() {
        User user = mockUser(1L);
        Address address = Address.builder().id(2L).user(user).isDefault(true).build();
        when(addressRepository.findById(2L)).thenReturn(Optional.of(address));

        addressService.setDefault(1L, 2L);

        verify(addressRepository, never()).clearDefaultForUser(any());
        verify(addressRepository, never()).save(any());
    }

    @Test
    void deleteAddress_whenDefault_throwsCannotDeleteDefaultAddressException() {
        User user = mockUser(1L);
        Address address = Address.builder().id(1L).user(user).isDefault(true).build();
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> addressService.deleteAddress(1L, 1L))
                .isInstanceOf(CannotDeleteDefaultAddressException.class);
        verify(addressRepository, never()).delete(any());
    }

    @Test
    void deleteAddress_whenNotOwner_throwsAddressNotFoundException() {
        Address address = Address.builder().id(1L)
                .user(User.builder().id(99L).build()).isDefault(false).build();
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> addressService.deleteAddress(1L, 1L))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void updateAddress_updatesAllFields() {
        User user = mockUser(1L);
        Address address = Address.builder().id(1L).user(user).city("Barcelona").build();
        Address saved = Address.builder().id(1L).city("Madrid").build();
        AddressResponseDTO dto = AddressResponseDTO.builder().id(1L).city("Madrid").build();
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(addressRepository.save(address)).thenReturn(saved);
        when(addressMapper.toDTO(saved)).thenReturn(dto);

        AddressResponseDTO result = addressService.updateAddress(1L, 1L, buildRequest());

        assertThat(result.getCity()).isEqualTo("Madrid");
        verify(addressRepository).save(address);
    }

    @Test
    void updateAddress_whenNotOwner_throwsAddressNotFoundException() {
        Address address = Address.builder().id(1L)
                .user(User.builder().id(99L).build()).build();
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> addressService.updateAddress(1L, 1L, buildRequest()))
                .isInstanceOf(AddressNotFoundException.class);
    }
}
