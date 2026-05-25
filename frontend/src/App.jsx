import { useState, useEffect } from 'react'
import axios from 'axios'
import './App.css'

const API_URL = 'http://localhost:8080/api'

function App() {
  const [movies, setMovies] = useState([])
  const [selectedMovie, setSelectedMovie] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [view, setView] = useState('movies') // 'movies', 'details', 'register', 'login'
  const [user, setUser] = useState(null)
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: ''
  })

  useEffect(() => {
    fetchMovies()
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

  const handleRegister = async (e) => {
    e.preventDefault()
    try {
      const response = await axios.post(`${API_URL}/auth/register`, {
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
      setUser(response.data)
      localStorage.setItem('token', response.data.token)
      alert('Login successful!')
      setView('movies')
      setFormData({ email: '', password: '', firstName: '', lastName: '', phone: '' })
    } catch (err) {
      alert('Login failed: ' + (err.response?.data?.message || err.message))
    }
  }

  const handleLogout = () => {
    setUser(null)
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
              <span>Welcome, {user.firstName}!</span>
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
                <button className="book-btn">Book Tickets</button>
              )}
              {selectedMovie.status === 'coming_soon' && (
                <p className="coming-soon-label">Coming {selectedMovie.releaseDate}</p>
              )}
            </div>
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
          </div>
        </main>
      )}

      <footer className="footer">
        <p>Cinema Management System - Bishkek 2026</p>
      </footer>
    </div>
  )
}

export default App
