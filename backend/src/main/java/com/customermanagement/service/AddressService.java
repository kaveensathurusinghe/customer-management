package com.customermanagement.service;

import com.customermanagement.entity.Address;
import com.customermanagement.entity.City;
import com.customermanagement.entity.Country;
import com.customermanagement.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CountryService countryService;

    @Autowired
    private CityService cityService;

    public List<Address> getAddressesByCustomerId(Long customerId) {
        return addressRepository.findByCustomerId(customerId);
    }

    // Resolve country/city names to entities (reusable)
    public Address resolveAddress(Address address) {
        if (address.getCountry() != null && address.getCountry().getName() != null) {
            Country country = countryService.getCountryByName(address.getCountry().getName());
            address.setCountry(country);
        }

        if (address.getCity() != null && address.getCity().getName() != null) {
            Country country = address.getCountry();
            City city = cityService.getCityByNameAndCountry(
                    address.getCity().getName(),
                    country.getName()
            );
            address.setCity(city);
        }

        return address;
    }

    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new RuntimeException("Address not found with id: " + id);
        }
        addressRepository.deleteById(id);
    }
}