package com.customermanagement.service;

import com.customermanagement.entity.Country;
import com.customermanagement.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CountryService {

    @Autowired
    private CountryRepository countryRepository;

    @Cacheable("countries")
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    @Cacheable("countryMap")
    public Map<String, Country> getCountryMap() {
        return countryRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        c -> c.getName().toLowerCase(),
                        c -> c
                ));
    }

    public Country getCountryByName(String name) {
        Country country = getCountryMap().get(name.toLowerCase());
        if (country == null) {
            throw new RuntimeException("Country not found: " + name);
        }
        return country;
    }

    public Country getCountryById(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with id: " + id));
    }
}