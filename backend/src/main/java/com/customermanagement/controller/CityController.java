package com.customermanagement.controller;

import com.customermanagement.entity.City;
import com.customermanagement.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@CrossOrigin(origins = "*")
public class CityController {

    @Autowired
    private CityService cityService;

    @GetMapping
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @GetMapping("/by-country/{countryId}")
    public ResponseEntity<List<City>> getCitiesByCountryId(@PathVariable Long countryId) {
        return ResponseEntity.ok(cityService.getCitiesByCountryId(countryId));
    }

    @GetMapping("/by-country-name")
    public ResponseEntity<List<City>> getCitiesByCountryName(@RequestParam String countryName) {
        return ResponseEntity.ok(cityService.getCitiesByCountryName(countryName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<City> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getCityById(id));
    }
}