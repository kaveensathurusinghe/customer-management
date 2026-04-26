package com.customermanagement.repository;

import com.customermanagement.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void saveCustomer() {
        Customer customer = new Customer();
        customer.setName("Kaveen Test");
        customer.setDob(new Date());
        customer.setNicNumber("200221200951");
        Customer saved = customerRepository.save(customer);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findByNicNumber() {
        Customer customer = new Customer();
        customer.setName("Kaveen NIC");
        customer.setDob(new Date());
        customer.setNicNumber("200320021651");
        customerRepository.save(customer);
        Optional<Customer> found = customerRepository.findByNicNumber("200320021651");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Kaveen NIC");
    }

    @Test
    void wrongNicNumber() {
        Optional<Customer> found = customerRepository.findByNicNumber("20000434343454");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByNicNumber() {
        Customer customer = new Customer();
        customer.setName("Exists Test");
        customer.setDob(new Date());
        customer.setNicNumber("19002122951");
        customerRepository.save(customer);
        assertThat(customerRepository.existsByNicNumber("19002122951")).isTrue();
        assertThat(customerRepository.existsByNicNumber("20000434343454")).isFalse();
    }

}