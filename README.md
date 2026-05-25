# 🎬 Cinema Management System - Bishkek

Full-stack cinema booking and management system for Bishkek cinemas.

## Tech Stack

### Backend
- **Spring Boot:** 3.2.5
- **Java:** 17
- **Database:** PostgreSQL 14+
- **Security:** Spring Security + JWT
- **Payment:** Stripe API
- **Build Tool:** Maven

### Frontend
- **React:** 18
- **Vite:** 8.0
- **Axios:** HTTP client
- **Modern CSS:** Responsive design

## Features

- 🎬 Movie catalog with genres
- 🎫 Online ticket booking with seat selection
- 💳 Payment integration (Stripe, О!Деньги, Элсом)
- 🎁 Loyalty program with cashback
- 📱 QR code tickets
- 🌐 Multi-language support (Russian, Kyrgyz, English)
- 📊 Admin dashboard and analytics
- 🔐 JWT authentication with role-based access

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+ (or Docker)
- Node.js 18+
- npm or yarn
- Stripe account (for payments)

## Quick Start

### 1. Database Setup (Docker)

```bash
# Start PostgreSQL in Docker
docker compose up -d

# Check if running
docker ps
```

### 2. Backend Setup

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

Backend will start on `http://localhost:8080`

### 3. Frontend Setup

```bash
# Navigate to frontend
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend will start on `http://localhost:5173`

### 4. Access the Application

- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:8080/api
- **Database:** localhost:5432 (cinema_db)

## API Documentation

### Authentication

```
POST /api/auth/register - Register new user
POST /api/auth/login    - Login and get JWT token
GET  /api/auth/me       - Get current user info
```

### Movies

```
GET    /api/movies              - List all movies
GET    /api/movies/{id}         - Get movie details
GET    /api/movies/{id}/showtimes - Get showtimes for movie
POST   /api/movies              - Create movie (ADMIN)
PUT    /api/movies/{id}         - Update movie (ADMIN)
DELETE /api/movies/{id}         - Delete movie (ADMIN)
```

### Showtimes

```
GET    /api/showtimes           - List showtimes
GET    /api/showtimes/{id}      - Get showtime details
GET    /api/showtimes/{id}/seats - Get seat availability
POST   /api/showtimes           - Create showtime (MANAGER)
```

### Booking

```
POST /api/bookings/reserve  - Reserve seats
POST /api/bookings/confirm  - Confirm booking
GET  /api/bookings/my-orders - Get user's orders
```

### Payment

```
POST /api/payments/create-intent - Create Stripe payment intent
POST /api/payments/webhook       - Stripe webhook handler
```

## Project Structure

```
src/main/java/kg/cinema/
├── CinemaApplication.java
├── config/          # Configuration classes
├── entity/          # JPA entities
├── repository/      # Spring Data repositories
├── service/         # Business logic
├── controller/      # REST controllers
├── dto/             # Data transfer objects
├── exception/       # Custom exceptions
├── security/        # Security & JWT
└── util/            # Utility classes

src/main/resources/
├── application.yml
└── db/migration/    # Flyway migrations
    ├── V1__init_schema.sql
    └── V2__seed_data.sql
```

## Database Schema

18 tables organized in 6 blocks:

1. **Content** - Movies, Genres
2. **Infrastructure** - Cinemas, Halls, Seats
3. **Schedule** - Showtimes, Prices
4. **Users** - Users, Loyalty Accounts
5. **Sales** - Orders, Tickets, Payments, Promocodes
6. **Analytics** - Cart Sessions, Action Logs, Notifications

## Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=BookingServiceTest
```

## Deployment

See `SPRING_BOOT_PLAN.md` for detailed deployment checklist.

## License

MIT

## Authors

Bishkek Institute Database Course Project 2026
