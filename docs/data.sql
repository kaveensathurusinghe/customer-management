
-- countries
INSERT IGNORE INTO country (name) VALUES
('Sri Lanka'),
('China'),
('United States'),
('United Kingdom'),
('Australia'),
('Canada'),
('Singapore'),
('United Arab Emirates');

-- cities
INSERT IGNORE INTO city (name, country_id) VALUES
('Colombo', 1),
('Kandy', 1),
('Galle', 1),
('Jaffna', 1),
('Negombo', 1);

INSERT IGNORE INTO city (name, country_id) VALUES
('Beijing', 2),
('Shanghai', 2),
('Shenzhen', 2),
('Chengdu', 2),
('Wuhan', 2);

INSERT IGNORE INTO city (name, country_id) VALUES
('New York', 3),
('Los Angeles', 3),
('Chicago', 3),
('Houston', 3),
('San Francisco', 3);

INSERT IGNORE INTO city (name, country_id) VALUES
('London', 4),
('Manchester', 4),
('Birmingham', 4),
('Liverpool', 4),
('Edinburgh', 4);

INSERT IGNORE INTO city (name, country_id) VALUES
('Sydney', 5),
('Melbourne', 5),
('Brisbane', 5),
('Perth', 5),
('Adelaide', 5);

INSERT IGNORE INTO city (name, country_id) VALUES
('Toronto', 6),
('Vancouver', 6),
('Montreal', 6),
('Calgary', 6),
('Ottawa', 6);

INSERT IGNORE INTO city (name, country_id) VALUES
('Singapore', 7),
('Jurong', 7),
('Tampines', 7);

INSERT IGNORE INTO city (name, country_id) VALUES
('Dubai', 8),
('Abu Dhabi', 8),
('Sharjah', 8);