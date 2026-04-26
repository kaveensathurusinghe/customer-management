package com.customermanagement.service;

import com.customermanagement.dto.CustomerDTO;
import com.customermanagement.entity.Customer;
import com.customermanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CountryService countryService;

    @Mock
    private CityService cityService;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private CustomerDTO testCustomerDTO;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test User");
        testCustomer.setDob(new Date());
        testCustomer.setNicNumber("SERVICE001");

        testCustomerDTO = new CustomerDTO();
        testCustomerDTO.setId(1L);
        testCustomerDTO.setName("Test User");
        testCustomerDTO.setDob(new Date());
        testCustomerDTO.setNicNumber("SERVICE001");
    }

    @Test
    void shouldCreateCustomer() {
        when(customerRepository.existsByNicNumber("SERVICE001")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        CustomerDTO created = customerService.createCustomer(testCustomer);

        assertThat(created).isNotNull();
        assertThat(created.getNicNumber()).isEqualTo("SERVICE001");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenNicExists() {
        when(customerRepository.existsByNicNumber("SERVICE001")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(testCustomer))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldGetCustomerById() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        CustomerDTO found = customerService.getCustomerById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test User");
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldDeleteCustomer() {
        when(customerRepository.existsById(1L)).thenReturn(true);

        customerService.deleteCustomer(1L);

        verify(customerRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCustomer() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.deleteCustomer(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldThrowExceptionWhenAddingSelfAsFamily() {
        assertThatThrownBy(() -> customerService.addFamilyMember(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("can't add yourself");
    }

    @Test
    void shouldSearchCustomerByNic() {
        when(customerRepository.findByNicNumber("SERVICE001")).thenReturn(Optional.of(testCustomer));

        CustomerDTO found = customerService.searchByNic("SERVICE001");

        assertThat(found).isNotNull();
        assertThat(found.getNicNumber()).isEqualTo("SERVICE001");
    }
}