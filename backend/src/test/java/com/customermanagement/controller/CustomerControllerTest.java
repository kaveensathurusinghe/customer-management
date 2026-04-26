package com.customermanagement.controller;

import com.customermanagement.dto.CustomerDTO;
import com.customermanagement.entity.Customer;
import com.customermanagement.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() throws Exception {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(1L);
        dto.setName("API Test");
        dto.setDob(new Date());
        dto.setNicNumber("API001");

        when(customerService.createCustomer(any(Customer.class))).thenReturn(dto);

        Customer customer = new Customer();
        customer.setName("API Test");
        customer.setDob(new Date());
        customer.setNicNumber("API001");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nicNumber").value("API001"));
    }

    @Test
    void shouldGetCustomerById() throws Exception {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(1L);
        dto.setName("API Test");
        dto.setNicNumber("API001");

        when(customerService.getCustomerById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("API Test"));
    }

    @Test
    void shouldReturn404WhenCustomerNotFound() throws Exception {
        when(customerService.getCustomerById(99L))
                .thenThrow(new RuntimeException("Customer not found"));

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetCustomerByNic() throws Exception {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(1L);
        dto.setName("NIC Search");
        dto.setNicNumber("API002");

        when(customerService.searchByNic("API002")).thenReturn(dto);

        mockMvc.perform(get("/api/customers/nic/API002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nicNumber").value("API002"));
    }
}