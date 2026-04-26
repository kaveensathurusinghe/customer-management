--master tables
CREATE TABLE IF NOT EXISTS country (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       name VARCHAR(100) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS city (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    name VARCHAR(100) NOT NULL,
    country_id BIGINT NOT NULL,
    FOREIGN KEY (country_id) REFERENCES country(id),
    UNIQUE KEY uk_city_country (name, country_id)
    );

-- customer table
CREATE TABLE IF NOT EXISTS customer (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(200) NOT NULL,
    date_of_birth DATE NOT NULL,
    nic_number VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_nic (nic_number),
    INDEX idx_name (name)
    );

-- mobile numbers table
CREATE TABLE IF NOT EXISTS mobile (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      customer_id BIGINT NOT NULL,
                                      mobile_number VARCHAR(20) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    INDEX idx_mobile_customer (customer_id)
    );

-- addresses table
CREATE TABLE IF NOT EXISTS address (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       customer_id BIGINT NOT NULL,
                                       address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200),
    city_id BIGINT NOT NULL,
    country_id BIGINT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    FOREIGN KEY (city_id) REFERENCES city(id),
    FOREIGN KEY (country_id) REFERENCES country(id),
    INDEX idx_address_customer (customer_id)
    );

-- relationship table
CREATE TABLE IF NOT EXISTS customer_family (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               customer_id BIGINT NOT NULL,
                                               family_member_id BIGINT NOT NULL,
                                               FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    FOREIGN KEY (family_member_id) REFERENCES customer(id) ON DELETE CASCADE,
    UNIQUE KEY uk_family (customer_id, family_member_id),
    INDEX idx_customer_family (customer_id)
    );