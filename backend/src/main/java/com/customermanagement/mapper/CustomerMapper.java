package com.customermanagement.mapper;

import com.customermanagement.dto.*;
import com.customermanagement.entity.*;

import java.util.stream.Collectors;

public class CustomerMapper {

    public static CustomerDTO toDTO(Customer customer) {
        if (customer == null) return null;

        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setDob(customer.getDob());
        dto.setNicNumber(customer.getNicNumber());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());

        if (customer.getMobiles() != null) {
            dto.setMobiles(customer.getMobiles().stream()
                    .map(m -> {
                        MobileDTO md = new MobileDTO();
                        md.setId(m.getId());
                        md.setMobileNumber(m.getMobileNumber());
                        return md;
                    })
                    .collect(Collectors.toList()));
        }

        if (customer.getAddresses() != null) {
            dto.setAddresses(customer.getAddresses().stream()
                    .map(a -> {
                        AddressDTO ad = new AddressDTO();
                        ad.setId(a.getId());
                        ad.setAddressLine1(a.getAddressLine1());
                        ad.setAddressLine2(a.getAddressLine2());
                        ad.setCity(a.getCity() != null ? a.getCity().getName() : null);
                        ad.setCountry(a.getCountry() != null ? a.getCountry().getName() : null);
                        return ad;
                    })
                    .collect(Collectors.toList()));
        }

        if (customer.getFamily() != null) {
            dto.setFamily(customer.getFamily().stream()
                    .map(f -> {
                        FamilyMemberDTO fd = new FamilyMemberDTO();
                        fd.setId(f.getId());
                        fd.setName(f.getName());
                        fd.setNicNumber(f.getNicNumber());
                        return fd;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}