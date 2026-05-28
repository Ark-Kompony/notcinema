import { useState, useEffect } from 'react'
import axios from 'axios'

const API_URL = 'http://localhost:8080/api'

function AdminPanel({ user, token, onLogout }) {
  const [view, setView] = useState('dashboard')
  const [stats, setStats] = useState(null)
  const [users, setUsers] = useState([])
  const [movies, setMovies] = useState([])
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  // Movie form state
  const [movieForm, setMovieForm] = useState({
    title: '',
    description: '',
    ageRating: '12+',
    durationMin: '',
    posterUrl: '',
    trailerUrl: '',
    status: 'coming_soon',
    releaseDate: '',
    country: '',
    director: '',
    imdbRating: ''
  })

  useEffect(() => {
    if (view === 'dashboard') fetchDashboard()
    if (view === 'users') fetchUsers()
    if (view === 'movies') fetchMovies()
    if (view === 'orders') fetchOrders()
  }, [view])

  const fetchDashboard = async () => {
    try {
      setLoading(true)
      const response = await axios.get(`${API_URL}/admin/dashboard`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      setStats(response.data)
      setError(null)
    } catch (err) {
      setError('Failed to load dashboard: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const fetchUsers = async () => {
    try {
      setLoading(true)
      const response = await axios.get(`${API_URL}/admin/users`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      setUsers(response.data)
      setError(null)
    } catch (err) {
      setError('Failed to load users: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const fetchMovies = async () => {
    try {
      setLoading(true)
      const response = await axios.get(`${API_URL}/movies`)
      setMovies(response.data)
      setError(null)
    } catch (err) {
      setError('Failed to load movies: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const fetchOrders = async () => {
    try {
      setLoading(true)
      const response = await axios.get(`${API_URL}/admin/orders`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      setOrders(response.data)
      setError(null)
    } catch (err) {
      setError('Failed to load orders: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleCreateMovie = async (e) => {
    e.preventDefault()
    try {
      await axios.post(`${API_URL}/movies`, movieForm, {
        headers: { Authorization: `Bearer ${token}` }
      })
      alert('Movie created successfully!')
      fetchMovies()
      setMovieForm({
        title: '',
        description: '',
        ageRating: '12+',
        durationMin: '',
        posterUrl: '',
        trailerUrl: '',
        status: 'coming_soon',
        releaseDate: '',
        country: '',
        director: '',
        imdbRating: ''
      })
    } catch (err) {
      alert('Failed to create movie: ' + err.message)
    }
  }

  const handleDeleteMovie = async (id) => {
    if (!confirm('Are you sure you want to delete this movie?')) return
    try {
      await axios.delete(`${API_URL}/movies/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      alert('Movie deleted successfully!')
      fetchMovies()
    } catch (err) {
      alert('Failed to delete movie: ' + err.message)
    }
  }

  const handleChangeUserRole = async (userId, newRole) => {
    try {
      await axios.patch(`${API_URL}/admin/users/${userId}/role`,
        { role: newRole },
        { headers: { Authorization: `Bearer ${token}` }}
      )
      alert('User role updated!')
      fetchUsers()
    } catch (err) {
      alert('Failed to update role: ' + err.message)
    }
  }

  const handleDeactivateUser = async (userId) => {
    try {
      await axios.patch(`${API_URL}/admin/users/${userId}/deactivate`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      })
      alert('User deactivated!')
      fetchUsers()
    } catch (err) {
      alert('Failed to deactivate user: ' + err.message)
    }
  }

  return (
    <div className="admin-container">
      <header className="admin-header">
        <h1>🎬 Admin Panel - Bishkek Cinema</h1>
        <div className="admin-user-info">
          <span>Welcome, {user.firstName}!</span>
          <button onClick={onLogout} className="logout-btn">Logout</button>
        </div>
      </header>

      <nav className="admin-nav">
        <button
          className={view === 'dashboard' ? 'active' : ''}
          onClick={() => setView('dashboard')}
        >
          📊 Dashboard
        </button>
        <button
          className={view === 'movies' ? 'active' : ''}
          onClick={() => setView('movies')}
        >
          🎬 Movies
        </button>
        <button
          className={view === 'users' ? 'active' : ''}
          onClick={() => setView('users')}
        >
          👥 Users
        </button>
        <button
          className={view === 'orders' ? 'active' : ''}
          onClick={() => setView('orders')}
        >
          💰 Orders
        </button>
      </nav>

      <main className="admin-content">
        {loading && <div className="loading">Loading...</div>}
        {error && <div className="error">{error}</div>}

        {view === 'dashboard' && stats && (
          <div className="dashboard">
            <h2>Dashboard Statistics</h2>
            <div className="stats-grid">
              <div className="stat-card">
                <h3>Today's Orders</h3>
                <p className="stat-value">{stats.todayOrders}</p>
              </div>
              <div className="stat-card">
                <h3>Today's Revenue</h3>
                <p className="stat-value">{stats.todayRevenue} KGS</p>
              </div>
              <div className="stat-card">
                <h3>Total Users</h3>
                <p className="stat-value">{stats.totalUsers}</p>
              </div>
              <div className="stat-card">
                <h3>Total Orders</h3>
                <p className="stat-value">{stats.totalOrders}</p>
              </div>
            </div>
          </div>
        )}

        {view === 'movies' && (
          <div className="movies-management">
            <h2>Movie Management</h2>

            <div className="create-movie-form">
              <h3>Add New Movie</h3>
              <form onSubmit={handleCreateMovie}>
                <input
                  type="text"
                  placeholder="Title"
                  value={movieForm.title}
                  onChange={(e) => setMovieForm({...movieForm, title: e.target.value})}
                  required
                />
                <textarea
                  placeholder="Description"
                  value={movieForm.description}
                  onChange={(e) => setMovieForm({...movieForm, description: e.target.value})}
                  required
                />
                <select
                  value={movieForm.ageRating}
                  onChange={(e) => setMovieForm({...movieForm, ageRating: e.target.value})}
                >
                  <option value="0+">0+</option>
                  <option value="6+">6+</option>
                  <option value="12+">12+</option>
                  <option value="16+">16+</option>
                  <option value="18+">18+</option>
                </select>
                <input
                  type="number"
                  placeholder="Duration (minutes)"
                  value={movieForm.durationMin}
                  onChange={(e) => setMovieForm({...movieForm, durationMin: e.target.value})}
                  required
                />
                <input
                  type="text"
                  placeholder="Poster URL"
                  value={movieForm.posterUrl}
                  onChange={(e) => setMovieForm({...movieForm, posterUrl: e.target.value})}
                />
                <input
                  type="text"
                  placeholder="Trailer URL"
                  value={movieForm.trailerUrl}
                  onChange={(e) => setMovieForm({...movieForm, trailerUrl: e.target.value})}
                />
                <select
                  value={movieForm.status}
                  onChange={(e) => setMovieForm({...movieForm, status: e.target.value})}
                >
                  <option value="coming_soon">Coming Soon</option>
                  <option value="in_theaters">In Theaters</option>
                  <option value="archived">Archived</option>
                </select>
                <input
                  type="date"
                  value={movieForm.releaseDate}
                  onChange={(e) => setMovieForm({...movieForm, releaseDate: e.target.value})}
                  required
                />
                <input
                  type="text"
                  placeholder="Country"
                  value={movieForm.country}
                  onChange={(e) => setMovieForm({...movieForm, country: e.target.value})}
                />
                <input
                  type="text"
                  placeholder="Director"
                  value={movieForm.director}
                  onChange={(e) => setMovieForm({...movieForm, director: e.target.value})}
                />
                <input
                  type="number"
                  step="0.1"
                  placeholder="IMDB Rating"
                  value={movieForm.imdbRating}
                  onChange={(e) => setMovieForm({...movieForm, imdbRating: e.target.value})}
                />
                <button type="submit" className="btn-primary">Create Movie</button>
              </form>
            </div>

            <div className="movies-list">
              <h3>Existing Movies ({movies.length})</h3>
              <table>
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Status</th>
                    <th>Duration</th>
                    <th>Rating</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {movies.map(movie => (
                    <tr key={movie.id}>
                      <td>{movie.title}</td>
                      <td>{movie.status}</td>
                      <td>{movie.durationMin} min</td>
                      <td>{movie.imdbRating}</td>
                      <td>
                        <button
                          onClick={() => handleDeleteMovie(movie.id)}
                          className="btn-danger"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {view === 'users' && (
          <div className="users-management">
            <h2>User Management ({users.length} users)</h2>
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map(u => (
                  <tr key={u.id}>
                    <td>{u.id}</td>
                    <td>{u.firstName} {u.lastName}</td>
                    <td>{u.email}</td>
                    <td>
                      <select
                        value={u.role}
                        onChange={(e) => handleChangeUserRole(u.id, e.target.value)}
                        disabled={u.id === user.id}
                      >
                        <option value="USER">USER</option>
                        <option value="MANAGER">MANAGER</option>
                        <option value="ADMIN">ADMIN</option>
                      </select>
                    </td>
                    <td>{u.isActive ? '✅ Active' : '❌ Inactive'}</td>
                    <td>
                      {u.id !== user.id && u.isActive && (
                        <button
                          onClick={() => handleDeactivateUser(u.id)}
                          className="btn-danger"
                        >
                          Deactivate
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {view === 'orders' && (
          <div className="orders-management">
            <h2>All Orders ({orders.length})</h2>
            <table>
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>User</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {orders.map(order => (
                  <tr key={order.id}>
                    <td>#{order.id}</td>
                    <td>{order.user?.email}</td>
                    <td>{order.finalAmount} KGS</td>
                    <td>{order.paymentStatus}</td>
                    <td>{new Date(order.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  )
}

export default AdminPanel
