import { useState, useEffect } from 'react'
import axios from 'axios'
import './App.css'
import './AdminPanel.css'
import AdminPanel from './AdminPanel'

const API_URL = 'http://localhost:8080/api'

function App() {
  const [movies, setMovies] = useState([])
  const [selectedMovie, setSelectedMovie] = useState(null)
  const [showtimes, setShowtimes] = useState([])
  const [selectedShowtime, setSelectedShowtime] = useState(null)
  const [seats, setSeats] = useState([])
  const [selectedSeats, setSelectedSeats] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [view, setView] = useState('movies') // 'movies', 'details', 'showtimes', 'seats', 'register', 'login', 'admin'
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(localStorage.getItem('token'))
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: ''
  })

  useEffect(() => {
    fetchMovies()
    if (token) {
      fetchCurrentUser()
    }
  }, [])

  const fetchMovies = async () => {
    try {
      setLoading(true)
      const response = await axios.get(`${API_URL}/movies`)
      setMovies(response.data)
      setError(null)
    } catch (err) {
      setError('Failed to load movies. Make sure backend is running on port 8080.')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const fetchCurrentUser = async () => {
    try {
      const response = await axios.get(`${API_URL}/auth/me`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      setUser(response.data)
    } catch (err) {
      console.error('Failed to fetch user', err)
      localStorage.removeItem('token')
      setToken(null)
    }
  }

  const fetchMovieDetails = async (id) => {
    try {
      const response = await axios.get(`${API_URL}/movies/${id}`)
      setSelectedMovie(response.data)
      setView('details')
    } catch (err) {
      setError('Failed to load movie details')
      console.error(err)
    }
  }

  const fetchShowtimes = async (movieId) => {
    try {
      const response = await axios.get(`${API_URL}/showtimes/movie/${movieId}`)
      setShowtimes(response.data)
      setView('showtimes')
    } catch (err) {
      setError('Failed to load showtimes')
      console.error(err)
    }
  }

  const fetchSeats = async (showtimeId) => {
    try {
      const response = await axios.get(`${API_URL}/showtimes/${showtimeId}/seats`)
      setSeats(response.data)
      setView('seats')
    } catch (err) {
      setError('Failed to load seats')
      console.error(err)
    }
  }

  const handleSeatClick = (seat) => {
    if (seat.isAvailable) {
      if (selectedSeats.find(s => s.id === seat.id)) {
        setSelectedSeats(selectedSeats.filter(s => s.id !== seat.id))
      } else {
        setSelectedSeats([...selectedSeats, seat])
      }
    }
  }

  const handleBooking = async () => {
    if (!user) {
      alert('Please login to book tickets')
      setView('login')
      return
    }

    if (selectedSeats.length === 0) {
      alert('Please select at least one seat')
      return
    }

    try {
      const response = await axios.post(`${API_URL}/bookings/reserve`, {
        showtimeId: selectedShowtime.id,
        seatIds: selectedSeats.map(s => s.id)
      }, {
        headers: { Authorization: `Bearer ${token}` }
      })

      alert(`Booking successful! Total: ${response.data.totalAmount} KGS`)
      setSelectedSeats([])
      setView('movies')
    } catch (err) {
      alert('Booking failed: ' + (err.response?.data?.message || err.message))
    }
  }

  const handleRegister = async (e) => {
    e.preventDefault()
    try {
      await axios.post(`${API_URL}/auth/register`, {
        email: formData.email,
        password: formData.password,
        firstName: formData.firstName,
        lastName: formData.lastName,
        phone: formData.phone
      })
      alert('Registration successful! You can now login.')
      setView('login')
      setFormData({ email: '', password: '', firstName: '', lastName: '', phone: '' })
    } catch (err) {
      alert('Registration failed: ' + (err.response?.data?.message || err.message))
    }
  }

  const handleLogin = async (e) => {
    e.preventDefault()
    try {
      const response = await axios.post(`${API_URL}/auth/login`, {
        email: formData.email,
        password: formData.password
      })
      const newToken = response.data.accessToken
      setToken(newToken)
      localStorage.setItem('token', newToken)

      // Fetch user details
      const userResponse = await axios.get(`${API_URL}/auth/me`, {
        headers: { Authorization: `Bearer ${newToken}` }
      })
      setUser(userResponse.data)

      alert('Login successful!')
      setView('movies')
      setFormData({ email: '', password: '', firstName: '', lastName: '', phone: '' })
    } catch (err) {
      alert('Login failed: ' + (err.response?.data?.message || err.message))
    }
  }

  const handleLogout = () => {
    setUser(null)
    setToken(null)
    localStorage.removeItem('token')
    setView('movies')
  }

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  if (loading) {
    return (
      <div className="app">
        <div className="loading">Loading movies...</div>
      </div>
    )
  }

  return (
    <div className="app">
      <header className="header">
        <h1>🎬 Cinema Bishkek</h1>
        <nav>
          <button onClick={() => setView('movies')}>Movies</button>
          {!user ? (
            <>
              <button onClick={() => setView('login')}>Login</button>
              <button onClick={() => setView('register')}>Register</button>
            </>
          ) : (
            <>
              <span>Welcome, {user.firstName}! ({user.role})</span>
              {user.role === 'ADMIN' && (
                <button onClick={() => setView('admin')}>Admin</button>
              )}
              <button onClick={handleLogout}>Logout</button>
            </>
          )}
        </nav>
      </header>

      {error && <div className="error">{error}</div>}

      {view === 'movies' && (
        <main className="main">
          <h2>Now Showing</h2>
          <div className="movies-grid">
            {movies.filter(m => m.status === 'in_theaters').map(movie => (
              <div key={movie.id} className="movie-card" onClick={() => fetchMovieDetails(movie.id)}>
                <div className="movie-poster">
                  {movie.posterUrl ? (
                    <img src={movie.posterUrl} alt={movie.title} />
                  ) : (
                    <div className="poster-placeholder">🎬</div>
                  )}
                </div>
                <div className="movie-info">
                  <h3>{movie.title}</h3>
                  <p className="movie-meta">
                    {movie.ageRating} • {movie.durationMin} min
                  </p>
                  <p className="movie-rating">⭐ {movie.imdbRating}/10</p>
                  <div className="movie-genres">
                    {movie.genres?.map(g => (
                      <span key={g.id} className="genre-tag">{g.name}</span>
                    ))}
                  </div>
                </div>
              </div>
            ))}
          </div>

          <h2>Coming Soon</h2>
          <div className="movies-grid">
            {movies.filter(m => m.status === 'coming_soon').map(movie => (
              <div key={movie.id} className="movie-card" onClick={() => fetchMovieDetails(movie.id)}>
                <div className="movie-poster">
                  {movie.posterUrl ? (
                    <img src={movie.posterUrl} alt={movie.title} />
                  ) : (
                    <div className="poster-placeholder">🎬</div>
                  )}
                </div>
                <div className="movie-info">
                  <h3>{movie.title}</h3>
                  <p className="movie-meta">
                    {movie.ageRating} • {movie.durationMin} min
                  </p>
                  <p className="movie-rating">⭐ {movie.imdbRating}/10</p>
                  <p className="release-date">Release: {movie.releaseDate}</p>
                </div>
              </div>
            ))}
          </div>
        </main>
      )}

      {view === 'details' && selectedMovie && (
        <main className="main">
          <button className="back-btn" onClick={() => setView('movies')}>← Back to Movies</button>
          <div className="movie-details">
            <div className="details-poster">
              {selectedMovie.posterUrl ? (
                <img src={selectedMovie.posterUrl} alt={selectedMovie.title} />
              ) : (
                <div className="poster-placeholder-large">🎬</div>
              )}
            </div>
            <div className="details-info">
              <h2>{selectedMovie.title}</h2>
              <p className="movie-meta">
                {selectedMovie.ageRating} • {selectedMovie.durationMin} min • {selectedMovie.country}
              </p>
              <p className="movie-rating">⭐ {selectedMovie.imdbRating}/10</p>
              <div className="movie-genres">
                {selectedMovie.genres?.map(g => (
                  <span key={g.id} className="genre-tag">{g.name}</span>
                ))}
              </div>
              <p className="director">Director: {selectedMovie.director}</p>
              <p className="description">{selectedMovie.description}</p>
              {selectedMovie.status === 'in_theaters' && (
                <button className="book-btn" onClick={() => fetchShowtimes(selectedMovie.id)}>
                  Book Tickets
                </button>
              )}
              {selectedMovie.status === 'coming_soon' && (
                <p className="coming-soon-label">Coming {selectedMovie.releaseDate}</p>
              )}
            </div>
          </div>
        </main>
      )}

      {view === 'showtimes' && (
        <main className="main">
          <button className="back-btn" onClick={() => setView('details')}>← Back to Movie</button>
          <h2>Select Showtime</h2>
          <div className="showtimes-list">
            {showtimes.length === 0 ? (
              <p>No showtimes available for this movie.</p>
            ) : (
              showtimes.map(showtime => (
                <div key={showtime.id} className="showtime-card" onClick={() => {
                  setSelectedShowtime(showtime)
                  fetchSeats(showtime.id)
                }}>
                  <div className="showtime-time">
                    {new Date(showtime.startTime).toLocaleString()}
                  </div>
                  <div className="showtime-info">
                    <p>Hall: {showtime.hall?.name}</p>
                    <p>Language: {showtime.language}</p>
                    <p>Price: {showtime.basePrice} KGS</p>
                  </div>
                </div>
              ))
            )}
          </div>
        </main>
      )}

      {view === 'seats' && (
        <main className="main">
          <button className="back-btn" onClick={() => setView('showtimes')}>← Back to Showtimes</button>
          <h2>Select Seats</h2>
          <div className="screen">SCREEN</div>
          <div className="seats-grid">
            {seats.map(seat => (
              <div
                key={seat.id}
                className={`seat ${!seat.isAvailable ? 'taken' : ''} ${
                  selectedSeats.find(s => s.id === seat.id) ? 'selected' : ''
                }`}
                onClick={() => handleSeatClick(seat)}
              >
                {seat.rowNumber}-{seat.seatNumber}
              </div>
            ))}
          </div>
          <div className="booking-summary">
            <p>Selected Seats: {selectedSeats.length}</p>
            <p>Total: {selectedSeats.length * (selectedShowtime?.basePrice || 0)} KGS</p>
            <button className="book-btn" onClick={handleBooking} disabled={selectedSeats.length === 0}>
              Confirm Booking
            </button>
          </div>
        </main>
      )}

      {view === 'register' && (
        <main className="main">
          <div className="auth-form">
            <h2>Register</h2>
            <form onSubmit={handleRegister}>
              <input
                type="text"
                name="firstName"
                placeholder="First Name"
                value={formData.firstName}
                onChange={handleInputChange}
                required
              />
              <input
                type="text"
                name="lastName"
                placeholder="Last Name"
                value={formData.lastName}
                onChange={handleInputChange}
                required
              />
              <input
                type="email"
                name="email"
                placeholder="Email"
                value={formData.email}
                onChange={handleInputChange}
                required
              />
              <input
                type="tel"
                name="phone"
                placeholder="Phone (+996555123456)"
                value={formData.phone}
                onChange={handleInputChange}
                required
              />
              <input
                type="password"
                name="password"
                placeholder="Password"
                value={formData.password}
                onChange={handleInputChange}
                required
              />
              <button type="submit">Register</button>
            </form>
            <p>Already have an account? <a onClick={() => setView('login')}>Login</a></p>
          </div>
        </main>
      )}

      {view === 'login' && (
        <main className="main">
          <div className="auth-form">
            <h2>Login</h2>
            <form onSubmit={handleLogin}>
              <input
                type="email"
                name="email"
                placeholder="Email"
                value={formData.email}
                onChange={handleInputChange}
                required
              />
              <input
                type="password"
                name="password"
                placeholder="Password"
                value={formData.password}
                onChange={handleInputChange}
                required
              />
              <button type="submit">Login</button>
            </form>
            <p>Don't have an account? <a onClick={() => setView('register')}>Register</a></p>
            <p className="test-credentials">
              <small>Test: admin@cinema.kg / password123 or user@cinema.kg / password123</small>
            </p>
          </div>
        </main>
      )}

      {view === 'admin' && user?.role === 'ADMIN' && (
        <AdminPanel
          user={user}
          token={token}
          onLogout={handleLogout}
        />
      )}

      <footer className="footer">
        <p>Cinema Management System - Bishkek 2026</p>
      </footer>
    </div>
  )
}

export default App
