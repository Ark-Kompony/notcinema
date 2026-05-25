# 🚀 Quick Start Guide

## Prerequisites Check

```bash
# Check Java version (need 17+)
java -version

# Check Maven
mvn -version

# Check Node.js (need 18+)
node -v

# Check npm
npm -v

# Check Docker
docker --version
```

## Step-by-Step Setup

### 1️⃣ Start Database (5 seconds)

```bash
cd /home/shino/Desktop/notcinema
docker compose up -d
```

✅ Database running on `localhost:5432`

### 2️⃣ Start Backend (20 seconds)

```bash
# In the project root
mvn spring-boot:run
```

✅ Backend running on `http://localhost:8080`

### 3️⃣ Start Frontend (10 seconds)

```bash
# Open new terminal
cd /home/shino/Desktop/notcinema/frontend
npm install  # First time only
npm run dev
```

✅ Frontend running on `http://localhost:5173`

### 4️⃣ Open Browser

Go to: **http://localhost:5173**

## 🎬 Test the Application

### Browse Movies
- See "Now Showing" movies
- See "Coming Soon" movies
- Click on any movie for details

### Register Account
1. Click "Register"
2. Fill in the form:
   - First Name: Test
   - Last Name: User
   - Email: test@cinema.kg
   - Phone: +996555123456
   - Password: password123
3. Click "Register"

### Login
1. Click "Login"
2. Use credentials:
   - Email: user@cinema.kg
   - Password: password123

## 🛑 Stop Services

```bash
# Stop frontend (Ctrl+C in terminal)

# Stop backend (Ctrl+C in terminal)

# Stop database
docker compose down
```

## 🔧 Troubleshooting

### Backend won't start
```bash
# Check if port 8080 is free
lsof -ti:8080

# Kill process if needed
kill $(lsof -ti:8080)
```

### Frontend won't start
```bash
# Check if port 5173 is free
lsof -ti:5173

# Kill process if needed
kill $(lsof -ti:5173)
```

### Database connection error
```bash
# Check if Docker is running
docker ps

# Restart database
docker compose down
docker compose up -d
```

## 📊 Test Data

### Movies
- **Dune: Part Three** - Action, Sci-Fi (In Theaters)
- **Oppenheimer 2** - Drama (In Theaters)
- **Avatar 4** - Action, Sci-Fi (Coming Soon)

### Test Users
- **Admin:** admin@cinema.kg / password123
- **User:** user@cinema.kg / password123

### Cinemas
- Vefa Center (Chui Ave 154, Bishkek)
- Asia Mall (Ibraimov St 115, Bishkek)

## 🎯 Next Steps

1. ✅ Test movie browsing
2. ✅ Register a new account
3. ✅ Login with test user
4. ✅ View movie details
5. 🔜 Implement booking flow
6. 🔜 Add payment processing
7. 🔜 Deploy to production

## 📝 Notes

- Backend uses port **8080**
- Frontend uses port **5173**
- Database uses port **5432**
- CORS is configured for localhost:5173
- JWT tokens expire after 24 hours

---

**Ready to present!** 🎉
