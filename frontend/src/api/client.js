import axios from 'axios'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
})

// Attach the access token (full JWT, issued only after OTP verification) to
// every request. Pre-auth "tempToken"s used during the OTP stage are sent
// explicitly in each request body instead - never via this header.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('taskflow_access_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Access token missing/expired/invalid - force a clean re-login.
      localStorage.removeItem('taskflow_access_token')
      localStorage.removeItem('taskflow_user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default client
