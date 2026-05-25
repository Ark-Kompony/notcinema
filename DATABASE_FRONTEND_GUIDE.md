# Database & Frontend Setup Guide

## 🐳 Database Setup with Docker (Recommended)

### Why Docker?
- ✅ Easy setup (one command)
- ✅ Consistent environment
- ✅ Easy to reset/recreate
- ✅ No system pollution
- ✅ Works on any OS

### Setup Steps:

```bash
# 1. Make sure Docker is installed
docker --version

# 2. Start PostgreSQL
cd /home/shino/Desktop/notcinema
docker-compose up -d

# 3. Check if running
docker ps

# 4. View logs
docker-compose logs -f postgres

# 5. Connect to database (optional)
docker exec -it cinema-db psql -U postgres -d cinema_db
```

### Database Commands:

```bash
# Stop database
docker-compose down

# Stop and remove data (fresh start)
docker-compose down -v

# Restart database
docker-compose restart

# View logs
docker-compose logs postgres
```

### Connection Details:
- **Host:** localhost
- **Port:** 5432
- **Database:** cinema_db
- **Username:** postgres
- **Password:** postgres

---

## 🎨 Frontend Options

### Option 1: React (Recommended) ⭐

**Best for:**
- Modern, fast UI
- Large ecosystem
- Easy to learn
- Great for SPAs

**Tech Stack:**
- React 18
- React Router (navigation)
- Axios (API calls)
- Tailwind CSS (styling)
- React Query (data fetching)

**Estimated Time:** 2-3 weeks

**Features to Build:**
1. Home page with movie carousel
2. Movie details page
3. Showtime selection
4. Interactive seat map
5. Booking flow
6. Payment page
7. User profile
8. Order history
9. Admin dashboard

### Option 2: Vue.js

**Best for:**
- Easier learning curve than React
- Great documentation
- Progressive framework

**Tech Stack:**
- Vue 3
- Vue Router
- Axios
- Vuetify/PrimeVue (UI components)

**Estimated Time:** 2-3 weeks

### Option 3: Next.js (React Framework)

**Best for:**
- SEO optimization
- Server-side rendering
- Better performance

**Tech Stack:**
- Next.js 14
- TypeScript
- Tailwind CSS
- SWR (data fetching)

**Estimated Time:** 3-4 weeks

### Option 4: Simple HTML/CSS/JavaScript

**Best for:**
- Quick demo
- Learning basics
- Course presentation

**Tech Stack:**
- Vanilla JavaScript
- Bootstrap 5
- Fetch API

**Estimated Time:** 1 week

---

## 🚀 Recommended Approach for Your Course

### Phase 1: Backend Demo (Current) ✅
- Use Postman/curl to test APIs
- Show database design
- Demonstrate business logic

### Phase 2: Simple Frontend (1-2 weeks)
Build a **minimal but functional** frontend:

**Must-Have Pages:**
1. **Home** - List movies
2. **Movie Details** - Show movie info + showtimes
3. **Seat Selection** - Interactive seat map
4. **Checkout** - Payment form
5. **Login/Register** - Authentication

**Nice-to-Have:**
6. User profile
7. Order history
8. Admin panel

### Phase 3: Polish (1 week)
- Styling
- Error handling
- Loading states
- Responsive design

---

## 📱 Frontend Architecture

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── MovieCard.jsx
│   │   ├── SeatMap.jsx
│   │   ├── Navbar.jsx
│   │   └── ...
│   ├── pages/
│   │   ├── Home.jsx
│   │   ├── MovieDetails.jsx
│   │   ├── Booking.jsx
│   │   ├── Checkout.jsx
│   │   ├── Login.jsx
│   │   └── Profile.jsx
│   ├── services/
│   │   ├── api.js          (Axios setup)
│   │   ├── authService.js
│   │   ├── movieService.js
│   │   └── bookingService.js
│   ├── context/
│   │   └── AuthContext.jsx (User state)
│   ├── utils/
│   │   └── helpers.js
│   ├── App.jsx
│   └── main.jsx
└── package.json
```

---

## 🎯 My Recommendation

### For Your Course Project:

**Backend:** ✅ Already complete and excellent!

**Database:** 🐳 **Use Docker**
- Easy to demo
- Easy to reset
- Professional approach

**Frontend:** 🎨 **React with Tailwind CSS**

**Why?**
1. **React** is industry standard
2. **Tailwind** makes styling fast
3. **Modern** tech stack
4. **Portfolio-worthy**
5. **Easy to learn** with tons of tutorials

### Quick Start React Setup:

```bash
# 1. Create React app
cd /home/shino/Desktop
npx create-react-app cinema-frontend

# 2. Install dependencies
cd cinema-frontend
npm install axios react-router-dom @tanstack/react-query

# 3. Install Tailwind CSS
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p

# 4. Start development
npm start
```

---

## 🎬 Demo Strategy for Professors

### 1. Database Presentation (10 min)
- Show ER diagram
- Explain normalization
- Demonstrate triggers and views
- Show complex queries

### 2. Backend Demo (10 min)
- Show API endpoints in Postman
- Demonstrate booking flow
- Show payment integration
- Explain security (JWT)

### 3. Frontend Demo (10 min)
- Browse movies
- Book tickets
- Show seat selection
- Complete payment
- Scan QR code

### 4. Code Walkthrough (10 min)
- Show entity relationships
- Explain business logic
- Demonstrate transaction management
- Show security implementation

---

## 💡 Pro Tips

### For Database:
1. **Use Docker** - It's professional and practical
2. **Backup data** - `docker exec cinema-db pg_dump -U postgres cinema_db > backup.sql`
3. **Show pgAdmin** - Visual database tool for presentation

### For Frontend:
1. **Start simple** - Get basic flow working first
2. **Use UI library** - Material-UI or Ant Design for quick results
3. **Focus on UX** - Smooth booking flow is impressive
4. **Mobile-first** - Most users book on phones

### For Presentation:
1. **Live demo** - Show it working end-to-end
2. **Prepare backup** - Record video in case of issues
3. **Highlight complexity** - Explain seat locking, payment flow
4. **Show code quality** - Clean architecture, security

---

## 🎓 Timeline Suggestion

### Week 1: Database + Backend Polish
- ✅ Setup Docker database
- ✅ Test all endpoints
- ✅ Fix any bugs
- ✅ Add sample data

### Week 2-3: Frontend Development
- Day 1-2: Setup + Authentication
- Day 3-4: Movie browsing
- Day 5-7: Booking flow
- Day 8-10: Seat selection
- Day 11-14: Payment + Polish

### Week 4: Testing + Presentation
- Integration testing
- Bug fixes
- Presentation preparation
- Documentation

---

## 🚀 Next Steps

1. **Start Docker database:**
   ```bash
   docker-compose up -d
   ```

2. **Test backend:**
   ```bash
   mvn spring-boot:run
   ```

3. **Decide on frontend:**
   - React (recommended)
   - Vue
   - Simple HTML/JS

4. **Start building!**

---

## 📞 Need Help?

If you want me to help you:
1. ✅ Setup React frontend
2. ✅ Create seat selection component
3. ✅ Build booking flow
4. ✅ Integrate with backend

Just let me know! 🚀
