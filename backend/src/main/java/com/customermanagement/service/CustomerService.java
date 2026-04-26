package com.customermanagement.service;

import com.customermanagement.entity.*;
import com.customermanagement.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class CustomerService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CountryService countryService;

    @Autowired
    private CityService cityService;

    public Customer createCustomer(Customer customer) {
        if (customerRepository.existsByNicNumber(customer.getNicNumber())) {
            throw new RuntimeException("customer with nic " + customer.getNicNumber() + " already exists");
        }
        processAddresses(customer);
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("customer not found with id: " + id));

        if (!existing.getNicNumber().equals(updatedCustomer.getNicNumber())) {
            if (customerRepository.existsByNicNumber(updatedCustomer.getNicNumber())) {
                throw new RuntimeException("nic already exists: " + updatedCustomer.getNicNumber());
            }
        }

        existing.setName(updatedCustomer.getName());
        existing.setDob(updatedCustomer.getDob());
        existing.setNicNumber(updatedCustomer.getNicNumber());
        processAddresses(existing);
        return customerRepository.save(existing);
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("customer not found with id: " + id));
    }

    public Customer searchByNic(String nic) {
        return customerRepository.findByNicNumber(nic)
                .orElseThrow(() -> new RuntimeException("customer not found with nic: " + nic));
    }

    public Page<Customer> getAllCustomers(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return customerRepository.findAll(pageable);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    public Customer addMobile(Long customerId, String mobileNumber) {
        Customer customer = getCustomerById(customerId);
        Mobile mobile = new Mobile(mobileNumber);
        customer.addMobile(mobile);
        return customerRepository.save(customer);
    }

    public Customer addAddress(Long customerId, Address address) {
        Customer customer = getCustomerById(customerId);
        resolveAddressMasterData(address);
        customer.addAddress(address);
        return customerRepository.save(customer);
    }

    public Customer addFamilyMember(Long customerId, Long familyMemberId) {
        if (customerId.equals(familyMemberId)) {
            throw new RuntimeException("can't add yourself as family member");
        }
        Customer customer = getCustomerById(customerId);
        Customer familyMember = getCustomerById(familyMemberId);
        customer.addFamily(familyMember);
        return customerRepository.save(customer);
    }

    public Map<String, Object> bulkUpload(MultipartFile file) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        final int BATCH_SIZE = 50;
        Set<String> existingNics = new HashSet<>(customerRepository.findAllNicNumbers());
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Customer> batch = new ArrayList<>(BATCH_SIZE);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                try {
                    Customer customer = mapRowToCustomer(row, existingNics);
                    batch.add(customer);
                    successCount++;
                    if (batch.size() >= BATCH_SIZE) {
                        customerRepository.saveAll(batch);
                        entityManager.flush();
                        entityManager.clear();
                        batch.clear();
                    }
                } catch (Exception e) {
                    errorCount++;
                    errors.add("Row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }
            if (!batch.isEmpty()) {
                customerRepository.saveAll(batch);
                entityManager.flush();
                entityManager.clear();
            }
        } catch (Exception e) {
            throw new RuntimeException("failed to process excel file: " + e.getMessage());
        }
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);
        return result;
    }

    private void resolveAddressMasterData(Address address) {
        address.setCountry(countryService.getCountryByName(address.getCountry().getName()));
        address.setCity(cityService.getCityByNameAndCountry(
                address.getCity().getName(),
                address.getCountry().getName()
        ));
    }

    private void processAddresses(Customer customer) {
        if (customer.getAddresses() != null) {
            customer.getAddresses().forEach(this::resolveAddressMasterData);
        }
    }

    private Customer mapRowToCustomer(Row row, Set<String> existingNics) {
        String name = getCellStringValue(row.getCell(0));
        if (name == null || name.isEmpty()) throw new RuntimeException("name is mandatory");
        Date dob = getCellDateValue(row.getCell(1));
        if (dob == null) throw new RuntimeException("dob is mandatory");
        String nic = getCellStringValue(row.getCell(2));
        if (nic == null || nic.isEmpty()) throw new RuntimeException("nic is mandatory");
        if (existingNics.contains(nic)) throw new RuntimeException("nic already exists: " + nic);
        existingNics.add(nic);
        Customer customer = new Customer();
        customer.setName(name);
        customer.setDob(dob);
        customer.setNicNumber(nic);
        return customer;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return null;
        }
    }

    private Date getCellDateValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue();
            String dateStr = getCellStringValue(cell);
            return dateStr != null ? new SimpleDateFormat("yyyy-MM-dd").parse(dateStr) : null;
        } catch (Exception e) {
            return null;
        }
    }
}