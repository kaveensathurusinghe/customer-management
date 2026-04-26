package com.customermanagement.service;

import com.customermanagement.entity.Mobile;
import com.customermanagement.repository.MobileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MobileService {

    @Autowired
    private MobileRepository mobileRepository;

    public List<Mobile> getMobilesByCustomerId(Long customerId) {
        return mobileRepository.findByCustomerId(customerId);
    }

    public void deleteMobile(Long id) {
        if (!mobileRepository.existsById(id)) {
            throw new RuntimeException("Mobile not found with id: " + id);
        }
        mobileRepository.deleteById(id);
    }

    public boolean isValidMobileNumber(String mobileNumber) {
        return mobileNumber != null && mobileNumber.matches("^\\+?[0-9]{10,15}$");
    }
}