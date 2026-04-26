package com.customermanagement.controller;

import com.customermanagement.dto.MobileRequest;
import com.customermanagement.entity.Address;
import com.customermanagement.entity.Customer;
import com.customermanagement.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer created = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id,
                                                   @Valid @RequestBody Customer customer) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/nic/{nic}")
    public ResponseEntity<Customer> getCustomerByNic(@PathVariable String nic) {
        return ResponseEntity.ok(customerService.searchByNic(nic));
    }

    @GetMapping
    public ResponseEntity<Page<Customer>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(customerService.getAllCustomers(page, size, sortBy, direction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/mobiles")
    public ResponseEntity<Customer> addMobile(@PathVariable Long customerId,
                                              @Valid @RequestBody MobileRequest request) {
        return ResponseEntity.ok(customerService.addMobile(customerId, request.getMobileNumber()));
    }

    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<Customer> addAddress(@PathVariable Long customerId,
                                               @Valid @RequestBody Address address) {
        return ResponseEntity.ok(customerService.addAddress(customerId, address));
    }

    @PostMapping("/{customerId}/family/{familyMemberId}")
    public ResponseEntity<Customer> addFamilyMember(@PathVariable Long customerId,
                                                    @PathVariable Long familyMemberId) {
        return ResponseEntity.ok(customerService.addFamilyMember(customerId, familyMemberId));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "File is empty");
            return ResponseEntity.badRequest().body(error);
        }
        return ResponseEntity.ok(customerService.bulkUpload(file));
    }
}