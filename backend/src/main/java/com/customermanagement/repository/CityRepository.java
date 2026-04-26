package com.customermanagement.repository;

import com.customermanagement.entity.City;
import com.customermanagement.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findByCountry(Country country);

    List<City> findByCountryId(Long countryId);

    Optional<City> findByNameAndCountry(String name, Country country);
}
