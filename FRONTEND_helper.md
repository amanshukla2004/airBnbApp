# 🚀 Comprehensive Frontend Integration Guide

This document maps all the API endpoints, exact data shapes, global configurations, and validation rules to help you wire up the frontend efficiently and robustly without guesswork.

> [!TIP]  
> All endpoints (unless labeled **Public**) require the `Authorization: Bearer <Your_JWT_Token>` Header, which you will receive upon a successful login. Refresh tokens are automatically stored via HTTP-only Cookies.

---

## 🌐 1. Global System Behaviors

### 1.1 Error Handling (`GlobalExceptionHandler`)
Whenever an API call fails (e.g., Validation Failure, Not Found, Unauthorized), the backend wraps the error inside a global `ApiResponse` structure. Your `Axios` interceptors should expect `.catch(error)` responses to look exactly like this:
```json
{
  "timeStamp": "2026-04-13T10:15:30.152",
  "data": null,
  "error": {
    "status": "NOT_FOUND", // or BAD_REQUEST, FORBIDDEN, UNAUTHORIZED, INTERNAL_SERVER_ERROR
    "message": "Hotel not found with Id 45",
    "subErrors": null
  }
}
```

### 1.2 Enumerations (Select Box Options)
When submitting data, ensure these exact uppercase strings are used:
- **`Gender`**: `"MALE"`, `"FEMALE"`, `"OTHER"`
- **`BookingStatus`**: `"RESERVED"`, `"GUESTS_ADDED"`, `"PAYMENTS_PENDING"`, `"CONFIRMED"`, `"CANCELLED"`, `"EXPIRED"`
- **`PaymentStatus`**: `"PENDING"`, `"CONFIRMED"`, `"CANCELLED"`
- **`Role`**: `"GUEST"`, `"HOTEL_MANAGER"`

---

## 🔐 2. Authentication (`/auth`)
*These endpoints are Public. No JWT required.*

### User Registration
- **`POST /api/v1/auth/user/register`**
- **Req Body (`SignUpRequestDto`)**:
  ```json
  { 
    "email": "user@example.com", // Must be a valid email format
    "password": "password123",   // Must be at least 6 characters
    "name": "John Doe"           // Cannot be blank
  }
  ```
- **Res Body (`UserDto`)**: RETURNS `201 Created` with standard User info.

### User Login
- **`POST /api/v1/auth/user/login`**
- **Req Body (`LoginDto`)**:
  ```json
  { 
    "email": "user@example.com", 
    "password": "password123" 
  }
  ```
- **Res Body (`LoginResponseDto`)**: 
  ```json
  { "accessToken": "JWT_HERE" } 
  // It also automatically sets an HttpOnly Cookie for `refreshToken`.
  ```

### Hotel Manager Registration & Login
- **`POST /api/v1/auth/manager/register`**: Identical payload as User Register.
- **`POST /api/v1/auth/manager/login`**: Identical payload as User Login.

### Refresh Token Loop
- **`POST /api/v1/auth/refresh`**
- **Action**: Empty body. Reads the `refreshToken` HttpOnly Cookie and returns a new `{ "accessToken": "JWT_HERE" }`. You should trigger this when a request fails with `401 Unauthorized` indicating an expired JWT.

---

## 👤 3. Profiles

### Shared Get Profile Structure (`UserDto`)
When fetching any profile, it returns:
  ```json
  { 
    "id": 1, 
    "email": "john@example.com", 
    "name": "John Doe", 
    "gender": "MALE", 
    "dateOfBirth": "1990-05-15" // ISO Format (YYYY-MM-DD)
  }
  ```

- **`GET /api/v1/users/profile`** *(Requires USER Role)*
- **`PATCH /api/v1/users/profile`**: Send `name`, `dateOfBirth`, or `gender`.
- **`GET /api/v1/admin/profile`** *(Requires HOTEL_MANAGER Role)*
- **`PATCH /api/v1/admin/profile`**

---

## 👨‍👩‍👧‍👦 4. Guests Management (`/guests`)
*Allows Users to pre-save their family/friends for quick booking.*

- **`POST /api/v1/guests`**: 
  - **Req Body**: `{ "name": "Jane", "gender": "FEMALE", "age": 28 }`
  - **Res Body**: Returns the saved Guest with assigned `"id"`.
- **`GET /api/v1/guests`**: Returns `List<GuestDto>`.
- **`PUT /api/v1/guests/{guestId}`**: Submit full Guest details.
- **`DELETE /api/v1/guests/{guestId}`**: Returns 204 No Content.

---

## 🏨 5. Public Hotel Browsing (`/hotels`)
*Public discovery endpoints.*

- **`GET /api/v1/hotels/search`**
  - **Query Params**: `?city=Mumbai&startDate=2026-05-10&endDate=2026-05-15&roomsCount=1&page=0&size=10`
  - **Returns**: A Spring Data paginated wrapper containing `HotelDto`s.
- **`GET /api/v1/hotels/{hotelId}/info`**
  - **Returns**: Combines Hotel specs and nested Rooms:
  ```json
  {
    "hotel": { "...HotelDto..." },
    "rooms": [ { "...RoomDto..." } ]
  }
  ```

---

## 📅 6. Bookings Flow (`/bookings`)

1. **`GET /api/v1/users/myBookings`**: Returns all previous and active bookings.
2. **`POST /api/v1/bookings/init`**: Stage 1
   ```json
   {
     "hotelId": 1,
     "roomId": 1,
     "checkInDate": "2026-05-10",
     "checkOutDate": "2026-05-12",
     "roomsCount": 1
   }
   ```
3. **`POST /api/v1/bookings/{bookingId}/addGuests`**: Attach saved `guestIds`.
   - **Req Body**: `[ 4, 7, 9 ]`
4. **`POST /api/v1/bookings/{bookingId}/payments`**: Stage 2 (Starts Stripe Intent).
   - **Returns**: A Stripe Checkout Session URL. Redirect the user to this URL for payment.
5. **`POST /api/v1/bookings/{bookingId}/cancel`**: Trigger abort logic.
6. **`POST /api/v1/bookings/{bookingId}/status`**: 
   - **Polling Endpoint**: Use this to check the booking status after the user returns from the Stripe payment page.
   - **Returns**: A JSON object containing the status string:
     ```json
     { "status": "CONFIRMED" }
     ```

---

## 🛠️ 7. Manager Operations (`/admin/hotels`)
*Accessible **only** by `HOTEL_MANAGER`.*

### Hotel Object (`HotelDto`) Definition
```json
{
  "id": 1,
  "name": "Grand Palace",
  "city": "Mumbai",
  "photos": ["https://url1.jpg", "https://url2.jpg"],
  "amenities": ["WIFI", "POOL", "SPA"],
  "active": true,
  "contactInfo": {
    "address": "123 Main St",
    "phoneNumber": "+919876543210",
    "email": "contact@grandpalace.com",
    "location": "18.9220,72.8347" // Latitude, Longitude mapped
  }
}
```
- **`POST /api/v1/admin/hotels`**: Pass `HotelDto` without `id`.
- **`GET /api/v1/admin/hotels`**: Get all manager's owned hotels.
- **`PUT /api/v1/admin/hotels/{hotelId}`**: Overwrite Hotel specifications.
- **`DELETE /api/v1/admin/hotels/{hotelId}`**
- **`PATCH /api/v1/admin/hotels/{hotelId}/activate`**: Send no body; auto-toggles the `active` boolean state on the server.

### Analytics 
- **`GET /api/v1/admin/hotels/{hotelId}/bookings`**: All active/past bookings.
- **`GET /api/v1/admin/hotels/{hotelId}/reports`**
  - **Query Params**: `?startDate=2026-01-01&endDate=2026-04-13`
  - **Returns**: `{ "bookingCount": 54, "totalRevenue": 15400.00, "avgRevenue": 285.18 }`

---

## 🛏️ 8. Room & Dynamic Inventory (`/admin/.../rooms`)

### Room Object (`RoomDto`)
```json
{
  "id": 10,
  "type": "DELUXE",
  "basePrice": 150.00, // Changing this cascades to ALL future unbooked inventories automatically!
  "photos": ["image.jpg"],
  "amenities": ["AC", "MINIBAR"],
  "totalCount": 10, // Max quantity of this room type
  "capacity": 2     // Guest limit per room
}
```
- **`POST /api/v1/admin/hotels/{hotelId}/rooms`**: Create a room type.
- **`PUT /api/v1/admin/hotels/{hotelId}/rooms/{roomId}`**: Update room type.
- **`GET /api/v1/admin/hotels/{hotelId}/rooms`**: Returns all room variants.

### Daily Inventory (`InventoryDto`)
- **`GET /api/v1/admin/inventory/rooms/{roomId}`**: Returns an array of every single active future date showing `bookedCount`, `reservedCount`, `price`, `surgeFactor`, etc.
- **`PATCH /api/v1/admin/inventory/rooms/{roomId}`**: Apply custom surge/closure limits to specific Date blocks:
  ```json
  {
    "startDate": "2026-05-10",
    "endDate": "2026-05-15",
    "surgeFactor": 1.5, // 1.5x price multiplier
    "price": 175.00,    // Force an override independent of the Room's BasePrice
    "closed": false     // Instantly lock out a date from being booked
  }
  ```

---

## ⚙️ 9. Public Configurations (`/public`)
*These endpoints are Public. No JWT required.*

### System Config
- **`GET /api/v1/public/config`**
- **Action**: Returns global configuration values like support emails.
- **Res Body**:
  ```json
  {
    "supportEmail": "support@noxplatform.com"
  }
  ```
