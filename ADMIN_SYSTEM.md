# 🎭 Admin System Documentation - Bishkek Cinema Management

## 📋 Role-Based Access Control

Your system has **3 distinct roles** with different permissions:

### 1. 👤 **USER** (Regular Customer)
**Purpose:** Book tickets and manage personal orders

**Permissions:**
- ✅ Browse movies
- ✅ View movie details
- ✅ Select showtimes
- ✅ Choose seats
- ✅ Book tickets
- ✅ View their own orders
- ❌ Cannot access admin features
- ❌ Cannot manage movies or users

**Test Account:**
- Email: `aibek@mail.kg`
- Password: `password123`

---

### 2. 🎬 **MANAGER** (Cinema Staff)
**Purpose:** Manage daily cinema operations

**Permissions:**
- ✅ Everything USER can do
- ✅ Create showtimes
- ✅ Update showtimes
- ✅ Delete showtimes
- ✅ Deactivate showtimes
- ✅ Scan tickets
- ✅ View showtime details
- ❌ Cannot manage movies
- ❌ Cannot manage users
- ❌ Cannot access admin dashboard

**Test Account:**
- Email: `manager@cinema.kg`
- Password: `password123`

**API Endpoints:**
```bash
POST   /api/showtimes              # Create showtime
PUT    /api/showtimes/{id}         # Update showtime
DELETE /api/showtimes/{id}         # Delete showtime
PATCH  /api/showtimes/{id}/deactivate  # Deactivate showtime
POST   /api/tickets/{id}/scan      # Scan ticket
```

---

### 3. 👑 **ADMIN** (System Administrator)
**Purpose:** Full system control and management

**Permissions:**
- ✅ Everything MANAGER can do
- ✅ **Movie Management:**
  - Create movies
  - Update movies
  - Delete movies
  - Manage genres
- ✅ **User Management:**
  - View all users
  - Change user roles (USER ↔ MANAGER ↔ ADMIN)
  - Activate/Deactivate users
- ✅ **Dashboard & Analytics:**
  - View today's statistics
  - View revenue reports
  - View all orders
  - Filter orders by status
- ✅ **Promocode Management:**
  - Create promocodes
  - Update promocodes
  - Deactivate promocodes

**Test Account:**
- Email: `admin@cinema.kg`
- Password: `password123`

---

## 🖥️ Admin Panel Features

### **Dashboard** 📊
Real-time statistics:
- Today's Orders
- Today's Revenue (KGS)
- Total Users
- Total Orders

**API Endpoint:**
```bash
GET /api/admin/dashboard
Authorization: Bearer {token}
```

**Response:**
```json
{
  "todayOrders": 5,
  "todayRevenue": 2400.00,
  "totalUsers": 8,
  "totalOrders": 15
}
```

---

### **Movie Management** 🎬

#### Create Movie
**Form Fields:**
- Title (required)
- Description (required)
- Age Rating (0+, 6+, 12+, 16+, 18+)
- Duration in minutes (required)
- Poster URL
- Trailer URL
- Status (coming_soon, in_theaters, archived)
- Release Date (required)
- Country
- Director
- IMDB Rating (0.0 - 10.0)

**API Endpoint:**
```bash
POST /api/movies
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "New Movie",
  "description": "Description here",
  "ageRating": "12+",
  "durationMin": 120,
  "posterUrl": "https://example.com/poster.jpg",
  "trailerUrl": "https://youtube.com/trailer",
  "status": "coming_soon",
  "releaseDate": "2026-06-15",
  "country": "Kyrgyzstan",
  "director": "Director Name",
  "imdbRating": 8.5
}
```

#### Update Movie
```bash
PUT /api/movies/{id}
Authorization: Bearer {token}
```

#### Delete Movie
```bash
DELETE /api/movies/{id}
Authorization: Bearer {token}
```

**Note:** Deleting a movie will cascade delete all related showtimes and bookings!

---

### **User Management** 👥

#### View All Users
Shows table with:
- User ID
- Name
- Email
- Current Role
- Active Status
- Actions

#### Change User Role
**Dropdown Options:**
- USER
- MANAGER
- ADMIN

**API Endpoint:**
```bash
PATCH /api/admin/users/{userId}/role
Authorization: Bearer {token}
Content-Type: application/json

{
  "role": "MANAGER"
}
```

**Use Cases:**
- Promote USER to MANAGER (cinema staff)
- Promote MANAGER to ADMIN (system admin)
- Demote ADMIN to MANAGER or USER

#### Deactivate User
Prevents user from logging in without deleting their data.

**API Endpoint:**
```bash
PATCH /api/admin/users/{userId}/deactivate
Authorization: Bearer {token}
```

#### Activate User
Re-enable a deactivated user.

**API Endpoint:**
```bash
PATCH /api/admin/users/{userId}/activate
Authorization: Bearer {token}
```

---

### **Order Management** 💰

#### View All Orders
Shows table with:
- Order ID
- User Email
- Final Amount (KGS)
- Payment Status
- Created Date

#### Filter by Status
**Payment Statuses:**
- PENDING
- PAID
- FAILED
- REFUNDED

**API Endpoint:**
```bash
GET /api/admin/orders/status/{status}
Authorization: Bearer {token}

# Example:
GET /api/admin/orders/status/PAID
```

---

### **Revenue Reports** 📈

**API Endpoint:**
```bash
GET /api/admin/reports/revenue?startDate=2026-05-01&endDate=2026-05-31
Authorization: Bearer {token}
```

**Response:**
```json
{
  "startDate": "2026-05-01",
  "endDate": "2026-05-31",
  "totalOrders": 45,
  "totalRevenue": 18000.00,
  "orders": [...]
}
```

---

### **Promocode Management** 🎁

#### View All Promocodes
```bash
GET /api/admin/promocodes
Authorization: Bearer {token}
```

#### Create Promocode
```bash
POST /api/admin/promocodes
Authorization: Bearer {token}
Content-Type: application/json

{
  "code": "SUMMER2026",
  "discountType": "PERCENTAGE",
  "discountValue": 20.00,
  "validFrom": "2026-06-01T00:00:00",
  "validUntil": "2026-08-31T23:59:59",
  "usageLimit": 100,
  "minOrderAmount": 500.00,
  "isActive": true
}
```

#### Deactivate Promocode
```bash
PATCH /api/admin/promocodes/{id}/deactivate
Authorization: Bearer {token}
```

---

## 🔐 Security & Access Control

### How It Works:

1. **JWT Token Authentication**
   - User logs in → receives JWT token
   - Token contains user ID, email, and role
   - Token must be sent in Authorization header

2. **Role Checking**
   - Backend uses `@PreAuthorize` annotations
   - Checks user role before allowing access
   - Returns 403 Forbidden if insufficient permissions

3. **Frontend Protection**
   - Admin button only shows for ADMIN role
   - Admin panel checks `user?.role === 'ADMIN'`
   - Redirects to login if not authenticated

### Example Security Flow:

```
User clicks "Admin" button
  ↓
Frontend checks: user.role === 'ADMIN'?
  ↓ YES
Shows Admin Panel
  ↓
User clicks "Create Movie"
  ↓
Frontend sends: POST /api/movies with JWT token
  ↓
Backend checks: @PreAuthorize("hasRole('ADMIN')")
  ↓ YES
Creates movie and returns response
```

---

## 🎯 Common Admin Tasks

### Task 1: Add New Movie to Catalog
1. Login as ADMIN
2. Click "Admin" button
3. Go to "Movies" tab
4. Fill in movie form
5. Click "Create Movie"
6. ✅ Movie appears in catalog

### Task 2: Promote User to Manager
1. Login as ADMIN
2. Go to "Users" tab
3. Find user in table
4. Change role dropdown to "MANAGER"
5. ✅ User can now manage showtimes

### Task 3: View Today's Revenue
1. Login as ADMIN
2. Dashboard shows automatically
3. See "Today's Revenue" card
4. ✅ Real-time statistics

### Task 4: Deactivate Problematic User
1. Login as ADMIN
2. Go to "Users" tab
3. Find user
4. Click "Deactivate" button
5. ✅ User cannot login anymore

---

## 🧪 Testing Admin Features

### Test 1: Dashboard Statistics
```bash
# Login as admin
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@cinema.kg","password":"password123"}' \
  | jq -r '.accessToken')

# Get dashboard
curl -s http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Test 2: Create Movie
```bash
curl -s -X POST http://localhost:8080/api/movies \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Movie",
    "description": "Test Description",
    "ageRating": "12+",
    "durationMin": 120,
    "status": "coming_soon",
    "releaseDate": "2026-07-01",
    "country": "Kyrgyzstan",
    "director": "Test Director",
    "imdbRating": 7.5
  }' | jq
```

### Test 3: Change User Role
```bash
curl -s -X PATCH http://localhost:8080/api/admin/users/3/role \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"MANAGER"}' | jq
```

---

## 📊 Database Role Structure

```sql
-- Check all users and their roles
SELECT id, email, first_name, role, is_active 
FROM users 
ORDER BY role, id;

-- Count users by role
SELECT role, COUNT(*) as count 
FROM users 
GROUP BY role;

-- Find all admins
SELECT * FROM users WHERE role = 'ADMIN';
```

---

## 🚀 Quick Start Guide

### For Admins:
1. Open http://localhost:5173
2. Click "Login"
3. Enter: `admin@cinema.kg` / `password123`
4. Click "Admin" button in header
5. Explore Dashboard, Movies, Users, Orders tabs

### For Managers:
1. Login with manager account
2. Use API endpoints to manage showtimes
3. Scan tickets at cinema entrance

### For Users:
1. Register or login
2. Browse movies
3. Book tickets
4. View orders

---

## 🎓 Role Hierarchy

```
ADMIN (Full Control)
  ↓ can do everything MANAGER can do
MANAGER (Cinema Operations)
  ↓ can do everything USER can do
USER (Customer)
```

**Important:** 
- ADMIN can promote/demote any user
- MANAGER cannot change roles
- USER has no admin access

---

## 📝 Best Practices

### For Admins:
- ✅ Regularly check dashboard statistics
- ✅ Review orders for anomalies
- ✅ Keep movie catalog up-to-date
- ✅ Deactivate inactive users
- ✅ Monitor promocode usage

### For Managers:
- ✅ Create showtimes in advance
- ✅ Verify hall availability
- ✅ Set appropriate pricing
- ✅ Scan tickets accurately

### Security:
- ✅ Never share admin credentials
- ✅ Use strong passwords
- ✅ Regularly audit user roles
- ✅ Deactivate users who leave
- ✅ Monitor suspicious activity

---

## 🆘 Troubleshooting

### "Access Denied" Error
- Check if logged in as ADMIN
- Verify JWT token is valid
- Check token expiration (24 hours)

### Admin Panel Not Showing
- Verify user.role === 'ADMIN'
- Check browser console for errors
- Ensure backend is running

### Cannot Create Movie
- Check all required fields filled
- Verify date format (YYYY-MM-DD)
- Check IMDB rating (0.0 - 10.0)
- Ensure logged in as ADMIN

---

**Status:** ✅ Fully Functional Admin System  
**Last Updated:** May 28, 2026
