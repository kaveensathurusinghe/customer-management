# Customer Management System

A robust, full-stack web application designed for comprehensive customer management. It allows users to manage customer records, including their mobile numbers, multiple addresses, and familial relationships between customers. The system also supports efficient bulk uploading of up to 1,000,000 customer records via Excel.

This project serves as a complete technical showcase of integrating a **Java 8 Spring Boot** backend with a **React JS** frontend, communicating with a **MariaDB** database.

---

## 📖 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Key Features](#-key-features)
- [Database Design](#-database-design)
- [API Endpoints](#-api-endpoints)
- [Prerequisites](#-prerequisites)
- [Setup & Installation](#-setup--installation)
    - [Option 1: Docker Compose (Recommended)](#option-1-docker-compose-recommended)
    - [Option 2: Local Development Setup](#option-2-local-development-setup)
- [Bulk Upload Guidelines](#-bulk-upload-guidelines)
- [Testing](#-testing)

---

## 🏛 Architecture Overview

The system is split into two primary components:
1. **Backend (`/backend`)**: A RESTful API built with Java 8 and Spring Boot. It uses Maven for dependency management and Spring Data JPA to interact with MariaDB. It focuses on minimal DB calls and handles massive data imports efficiently (e.g., using memory-optimized batch inserts for Excel parsing).
2. **Frontend (`/frontend`)**: A React JS Single Page Application (SPA) bootstrapped via Create React App. It utilizes Axios for API calls, Bootstrap for styling, and React Router for navigation.

---

## ✨ Key Features

- **Customer CRUD Operations**: Create, Read, Update, and Delete individual customers.
- **Complex Associations**: 
  - Store multiple mobile numbers per customer.
  - Store multiple addresses per customer (linked to a City and Country master dataset).
  - Link customers as family members (a self-referential many-to-many relationship mapping).
- **Master Data Management**: Cities and Countries are pre-populated master data, ensuring normalization and consistent location tracking.
- **Tabular Data View**: Comprehensive UI to list all customers with pagination and filtering support.
- **High-Performance Bulk Excel Import**: specialized API route to process and insert up to 1,000,000 records from an Excel `.xlsx` file, optimized to prevent timeouts and out-of-memory errors.

---

## 🗄 Database Design

The relational schema is fully normalized. The design splits the data into core master tables (`country`, `city`), entity tables (`customer`), and 1-to-many / many-to-many relationship tables (`mobile`, `address`, `customer_family`).

- **ER Diagram**:
  <br>
  ![ER Diagram](docs/ER-diagram.png)

### DB Scripts
All database initialization scripts reside in the `docs/` folder. When using Docker, these might be automatically mounted depending on configuration.
- **DDL (Schema definition)**: `docs/schema.sql`
- **DML (Seed data)**: `docs/data.sql`

---

## 🔌 API Endpoints

The core `CustomerController` exposes several RESTful endpoints under `/api/customers`:

| HTTP Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/customers` | Retrieve paginated list of all customers |
| `GET` | `/api/customers/{id}` | Retrieve specific customer details |
| `GET` | `/api/customers/nic/{nic}` | Find a customer by their unique NIC |
| `POST` | `/api/customers` | Create a new customer |
| `PUT` | `/api/customers/{id}` | Update an existing customer |
| `DELETE` | `/api/customers/{id}` | Delete a customer (cascades to addresses/mobile/family) |
| `POST` | `/api/customers/{id}/mobiles` | Add a mobile number to a customer |
| `POST` | `/api/customers/{id}/addresses`| Add an address to a customer |
| `POST` | `/api/customers/{id}/family/{fid}`| Link a family member (who must also be a customer) |
| `POST` | `/api/customers/bulk` | Upload an Excel file for bulk customer creation |

---

## ⚙️ Prerequisites

To run this application locally, ensure you have the following installed:
- **Java Development Kit (JDK) 8**
- **Node.js** (v14+ recommended) and **npm**
- **Docker** & **Docker Compose** (For containerized deployment)
- **Maven** (Optional, the wrapper `./mvnw` is provided)

---

## 🚀 Setup & Installation

You can run the application entirely through Docker or set up the stack locally for development.

### Option 1: Docker Compose (Recommended)

Docker Compose will build the frontend, backend, and spin up the MariaDB instance automatically. 

1. Start the Docker daemon on your machine.
2. From the root directory, execute:
   ```bash
   docker-compose up -d --build
   ```
3. Once the build is complete and containers are healthy:
   - **Frontend UI**: Open `http://localhost:3000`
   - **Backend API**: Running on `http://localhost:8080`
   - **MariaDB**: Accessible on port `3307` externally.

To view logs:
```bash
docker-compose logs -f
```
To bring down the application:
```bash
docker-compose down
```

### Option 2: Local Development Setup

If you wish to run the app natively to work on the code:

#### 1. Database
You must have MariaDB running on your system. Alternatively, spin up just the database container:
```bash
docker-compose up -d db
```
*(The backend expects a database named `customer_management` with user `springuser` and password `springpass`. This is configured in `backend/src/main/resources/application.properties`)*

#### 2. Backend API
1. Open a terminal and navigate to the backend folder:
   ```bash
   cd backend
   ```
2. Run the application using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```

#### 3. Frontend UI
1. Open a new terminal and navigate to the frontend folder:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the React development server:
   ```bash
   npm start
   ```

---

## 📊 Bulk Upload Guidelines

To use the `/api/customers/bulk` endpoint or the bulk upload UI:
- The file must be a standard `.xlsx` (Excel) file.
- The file is parsed efficiently to maintain low memory overhead, handling up to 1 million rows.
- Ensure the columns contain all mandatory fields: **Name, Date of Birth, NIC Number**. 

---

## 🧪 Testing

The backend includes JUnit tests to verify logic and database interactions. To run the test suite:

```bash
cd backend
./mvnw clean test
```
