package com.customermanagement.service;

import com.customermanagement.entity.City;
import com.customermanagement.entity.Country;
import com.customermanagement.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryService countryService;

    // Cache all cities
    @Cacheable("cities")
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    // Cache cities grouped by country for fast lookups
    @Cacheable("cityMapByCountry")
    public Map<String, List<City>> getCitiesGroupedByCountry() {
        return cityRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCountry().getName().toLowerCase()
                ));
    }

    // Get cities by country name (uses cache)
    public List<City> getCitiesByCountryName(String countryName) {
        List<City> cities = getCitiesGroupedByCountry().get(countryName.toLowerCase());
        if (cities == null || cities.isEmpty()) {
            throw new RuntimeException("No cities found for country: " + countryName);
        }
        return cities;
    }

    // Get cities by country ID
    public List<City> getCitiesByCountryId(Long countryId) {
        Country country = countryService.getCountryById(countryId);
        return cityRepository.findByCountry(country);
    }

    // Get city by name and country
    public City getCityByNameAndCountry(String cityName, String countryName) {
        Country country = countryService.getCountryByName(countryName);
        return cityRepository.findByNameAndCountry(cityName, country)
                .orElseThrow(() -> new RuntimeException(
                        "City not found: " + cityName + " in " + countryName));
    }

    public City getCityById(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + id));
    }
}