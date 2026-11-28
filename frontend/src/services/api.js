import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json'
    }
});

// Agregar automáticamente el token JWT
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

// Si el token ya no sirve (usuario borrado, sesión expirada),
// se limpia la sesión y se manda al login
api.interceptors.response.use(
    (respuesta) => respuesta,
    (error) => {
        const status = error?.response?.status;
        const enLogin = window.location.pathname.startsWith('/login');

        if (status === 401 && !enLogin) {
            localStorage.removeItem('token');
            localStorage.removeItem('usuario');
            window.location.href = '/login';
        }

        return Promise.reject(error);
    }
);

export default api;