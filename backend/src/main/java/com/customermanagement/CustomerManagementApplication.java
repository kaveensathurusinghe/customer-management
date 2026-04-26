package com.customermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CustomerManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerManagementApplication.class, args);
    }
}