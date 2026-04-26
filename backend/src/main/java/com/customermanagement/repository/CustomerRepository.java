package com.customermanagement.repository;

import com.customermanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNicNumber(String nicNumber);
    boolean existsByNicNumber(String nicNumber);

    @Query("SELECT c.nicNumber FROM Customer c")
    List<String> findAllNicNumbers();
}
