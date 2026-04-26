package com.customermanagement.service;

import com.customermanagement.dto.CustomerDTO;
import com.customermanagement.entity.*;
import com.customermanagement.mapper.CustomerMapper;
import com.customermanagement.repository.*;
import com.customermanagement.util.ExcelStreamReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.Async;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public CustomerDTO createCustomer(Customer customer) {
        if (customerRepository.existsByNicNumber(customer.getNicNumber())) {
            throw new RuntimeException("customer with nic " + customer.getNicNumber() + " already exists");
        }

        List<Mobile> mobiles = new ArrayList<>();
        if (customer.getMobiles() != null) {
            mobiles.addAll(customer.getMobiles());
        }

        List<Address> addresses = new ArrayList<>();
        if (customer.getAddresses() != null) {
            addresses.addAll(customer.getAddresses());
        }

        customer.setMobiles(new ArrayList<>());
        customer.setAddresses(new ArrayList<>());

        for (Mobile m : mobiles) {
            if (m.getMobileNumber() != null && !m.getMobileNumber().trim().isEmpty()) {
                customer.addMobile(m);
            }
        }

        for (Address a : addresses) {
            if (a.getAddressLine1() != null && !a.getAddressLine1().trim().isEmpty()
                    && a.getCountry() != null && a.getCountry().getName() != null) {
                resolveAddressMasterData(a);
                customer.addAddress(a);
            }
        }

        Customer saved = customerRepository.save(customer);
        return CustomerMapper.toDTO(saved);
    }

    public CustomerDTO updateCustomer(Long id, Customer updatedCustomer) {
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

        existing.getMobiles().clear();
        if (updatedCustomer.getMobiles() != null) {
            for (Mobile m : updatedCustomer.getMobiles()) {
                if (m.getMobileNumber() != null && !m.getMobileNumber().trim().isEmpty()) {
                    existing.addMobile(m);
                }
            }
        }

        existing.getAddresses().clear();
        if (updatedCustomer.getAddresses() != null) {
            for (Address a : updatedCustomer.getAddresses()) {
                if (a.getAddressLine1() != null && !a.getAddressLine1().trim().isEmpty()
                        && a.getCountry() != null && a.getCountry().getName() != null) {
                    resolveAddressMasterData(a);
                    existing.addAddress(a);
                }
            }
        }

        Customer saved = customerRepository.save(existing);
        return CustomerMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("customer not found with id: " + id));
        return CustomerMapper.toDTO(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDTO searchByNic(String nic) {
        Customer customer = customerRepository.findByNicNumber(nic)
                .orElseThrow(() -> new RuntimeException("customer not found with nic: " + nic));
        return CustomerMapper.toDTO(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        return customerPage.map(CustomerMapper::toDTO);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    public CustomerDTO addMobile(Long customerId, String mobileNumber) {
        Customer customer = getCustomerEntity(customerId);
        Mobile mobile = new Mobile(mobileNumber);
        customer.addMobile(mobile);
        Customer saved = customerRepository.save(customer);
        return CustomerMapper.toDTO(saved);
    }

    public CustomerDTO addAddress(Long customerId, Address address) {
        Customer customer = getCustomerEntity(customerId);
        resolveAddressMasterData(address);
        customer.addAddress(address);
        Customer saved = customerRepository.save(customer);
        return CustomerMapper.toDTO(saved);
    }

    public CustomerDTO addFamilyMember(Long customerId, Long familyMemberId) {
        if (customerId.equals(familyMemberId)) {
            throw new RuntimeException("can't add yourself as family member");
        }
        Customer customer = getCustomerEntity(customerId);
        Customer familyMember = getCustomerEntity(familyMemberId);
        customer.addFamily(familyMember);
        Customer saved = customerRepository.save(customer);
        return CustomerMapper.toDTO(saved);
    }

    public Map<String, Object> bulkUpload(MultipartFile file) {
        Map<String, Object> finalResult = new LinkedHashMap<>();
        List<String> allErrors = Collections.synchronizedList(new ArrayList<>());
        int[] totalSuccess = {0};
        int[] totalErrors = {0};

        try (InputStream inputStream = file.getInputStream()) {

            ExcelStreamReader.processInChunks(inputStream, chunk -> {
                List<Customer> batch = new ArrayList<>(chunk.size());
                Set<String> currentChunkNics = new HashSet<>();

                for (String[] row : chunk) {
                    try {
                        String name = row[0];
                        String dateStr = row[1];
                        String nic = row[2];

                        if (name == null || name.isEmpty()) throw new RuntimeException("name missing");
                        if (dateStr == null) throw new RuntimeException("dob missing");
                        if (nic == null || nic.isEmpty()) throw new RuntimeException("nic missing");

                        nic = nic.trim();
                        if (!currentChunkNics.add(nic)) {
                            throw new RuntimeException("duplicate nic within file: " + nic);
                        }

                        Customer customer = new Customer();
                        customer.setName(name.trim());
                        customer.setDob(new SimpleDateFormat("yyyy-MM-dd").parse(dateStr.trim()));
                        customer.setNicNumber(nic);
                        batch.add(customer);

                    } catch (Exception e) {
                        totalErrors[0]++;
                        allErrors.add(e.getMessage());
                    }
                }

                if (!batch.isEmpty()) {
                    String sql = "INSERT INTO customer (name, date_of_birth, nic_number, created_at, updated_at) " +
                                 "VALUES (?, ?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE name = VALUES(name), date_of_birth = VALUES(date_of_birth), updated_at = VALUES(updated_at)";
                    
                    jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Customer c = batch.get(i);
                            ps.setString(1, c.getName());
                            ps.setDate(2, new java.sql.Date(c.getDob().getTime()));
                            ps.setString(3, c.getNicNumber());
                            Timestamp now = new Timestamp(System.currentTimeMillis());
                            ps.setTimestamp(4, now);
                            ps.setTimestamp(5, now);
                        }

                        @Override
                        public int getBatchSize() {
                            return batch.size();
                        }
                    });
                    totalSuccess[0] += batch.size();
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("failed to process file: " + e.getMessage());
        }

        finalResult.put("successCount", totalSuccess[0]);
        finalResult.put("errorCount", totalErrors[0]);
        finalResult.put("errors", allErrors.subList(0, Math.min(allErrors.size(), 100)));
        return finalResult;
    }

    private Customer getCustomerEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("customer not found with id: " + id));
    }

    private void resolveAddressMasterData(Address address) {
        address.setCountry(countryService.getCountryByName(address.getCountry().getName()));
        address.setCity(cityService.getCityByNameAndCountry(
                address.getCity().getName(),
                address.getCountry().getName()
        ));
    }

    private Customer mapRowToCustomer(Row row, Set<String> existingNics) {
        String name = getCellStringValue(row.getCell(0));
        if (name == null || name.isEmpty()) throw new RuntimeException("name is mandatory");

        Cell dateCell = row.getCell(1);
        if (dateCell == null) {
            throw new RuntimeException("dob cell is empty");
        }
        Date dob = getCellDateValue(dateCell);
        if (dob == null) {
            String cellContent = getCellStringValue(dateCell);
            throw new RuntimeException("dob is mandatory - cell type: " + dateCell.getCellType() +
                    ", value: " + (cellContent != null ? cellContent : "null"));
        }
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
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue();
                    }
                    break;
                case STRING:
                    String dateStr = cell.getStringCellValue().trim();
                    if (!dateStr.isEmpty()) {
                        return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println("failed to parse date from cell: " + e.getMessage());
        }
        return null;
    }
}