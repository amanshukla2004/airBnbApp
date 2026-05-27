# 🏨 Nox: Enterprise Hotel Management Platform (Spring Boot)

A robust, enterprise-grade RESTful API serving as the backend for **Nox**, a premium hotel booking and management platform. It features comprehensive dual-role authentication (Guests vs. Hotel Managers), dynamic algorithmic pricing, granular inventory management, and hardened Stripe payment integration.

---

## 🛠️ Tech Stack & Technologies
*   **Java 21 & Spring Boot 3.x**
*   **Spring Data JPA / Hibernate** (Data persistence and strict locking)
*   **Spring Security & JWT** (Stateless authentication with HttpOnly cookies)
*   **PostgreSQL** (Relational database)
*   **Stripe SDK** (Payment processing & webhook intent handling)
*   **Lombok & ModelMapper** (Boilerplate reduction and object serialization)
*   **Jakarta Validation** (Strict payload validation and boundary checks)

---

## 🔥 Key Features

### 1. Dual-Portal Authentication 
Separated pathways for **Users (GUESTS)** and **Property Owners (HOTEL_MANAGERS)**. The architecture employs strict Role-Based Access Control (RBAC), automatically preventing unauthorized access between domains. Uses short-lived Access Tokens + persistent HttpOnly Refresh Tokens.

### 2. Advanced Algorithmic Pricing
Includes a highly dynamic `HolidayPricingStrategy` that intercepts queries and actively surges prices (e.g., 25% inflation) for configured major calendar holidays or weekends. Base prices can be managed at the `Room` level and cascade forward to specific Unbooked dates natively.

### 3. Granular Inventory Management & Cascading Integrity
Managers can configure properties, map complex amenity lists, define base room models, and individually tailor **daily surge factors**. The system ensures physical data integrity through cascading deletions—removing a property automatically handles associated unconfirmed bookings and inventory blocks.

### 4. Case-Insensitive Global Search
The platform implements normalized city-based searching, allowing users to find properties regardless of character casing (e.g., "MUMBAI", "Mumbai", and "mumbai" all yield the same high-performance results).

### 5. Concurrency & Booking Safe-Guards
Built using exact **Pessimistic Writing Locks** (`LockModeType.PESSIMISTIC_WRITE`) alongside native SQL `COALESCE` handling to ensure simultaneous bookings on high-traffic variants physically cannot exceed the actual physical capacity limits. Bookings are staged with a **10-minute auto-expiry** to free up held inventory.

### 5. Automated Guest Management 
Authorized users can register, assign, update, and manage persistent profiles for their frequent travel guests (family/friends), assigning them to Bookings seamlessly.

---

## 🚀 Getting Started

### Prerequisites
*   **JavaJDK 21+**
*   **Maven**
*   **PostgreSQL** Database running at `localhost:5432`

### Installation & Execution

1.  **Configure Database:** Ensure PostgreSQL is active. Update your credentials in `src/main/resources/application.properties` if they differ from the defaults:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/airBnb
    spring.datasource.username=postgres
    spring.datasource.password=password
    ```

2.  **Stripe Environment Vars:** Apply your Stripe API keys to process real intent payments.
    ```properties
    stripe.secret.key=sk_test_...
    stripe.webhook.secret=whsec_...
    ```

3.  **Compile the Project:**
    Open the terminal at the root directory and execute Maven to compile and resolve dependencies.
    ```bash
    ./mvnw clean compile
    ```

4.  **Run the Spring Application:**
    Boot the app locally across port `9091`.
    ```bash
    ./mvnw spring-boot:run
    ```

---

## 🧩 Project Architectural Layout

```text
src/
 ├─ main/
 │   ├─ java/com/aman/project/airBnbApp/
 │   │   ├─ advice/        # Centralized ApiError and GlobalExceptionHandler triggers
 │   │   ├─ controller/    # All REST Endpoint exposure (Split by Domain)
 │   │   ├─ dto/           # Safe Data Transfer Objects restricting raw Entity access
 │   │   ├─ entity/        # Direct JPA SQL Mappings & Enum structures
 │   │   ├─ repository/    # Direct DB execution, heavily custom SQL @Query mapping
 │   │   ├─ security/      # JWT Filter Chains & AuthService Logic 
 │   │   ├─ service/       # The Business Logic Layer (RoomService, InventoryService)
 │   │   └─ strategy/      # Design pattern configurations for dynamic pricing (Holiday surges)
 │   └─ resources/
 │       └─ application.properties  # Central configuration file
```

---

## 🔗 Frontend Guide

If you are developing the client-side UI, please reference the dedicated `FRONTEND_helper.md` located in the root directory. It contains granular API mapping, exact enum spellings, JSON payload constraints, and `ApiError` shapes to optimize Axios/Fetch hookups.

---

## 🛡️ Exception Handling
Instead of throwing generic HTML Spring errors, all exceptions (Authentication errors, 404 Not Found, Role checks) are caught by the `@RestControllerAdvice` and parsed into a clean JSON layout:
```json
{
  "timeStamp": "2026-04..."
  "data": null,
  "error": {
    "status": "UNAUTHORIZED",
    "message": "Token has expired."
  }
}
```
