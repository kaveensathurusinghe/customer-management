package com.customermanagement.repository;

import com.customermanagement.entity.Mobile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MobileRepository extends JpaRepository<Mobile, Long> {

    List<Mobile> findByCustomerId(Long customerId);
}
