import axios from 'axios';
import { useKeycloak } from '@dsb-norge/vue-keycloak-js';

// Crear una instancia de Axios con configuración base
const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
});

axiosInstance.interceptors.request.use(
    (config) => {
        const keycloak = useKeycloak();
        console.log(keycloak.hasRealmRole('employee'));
        if (keycloak.authenticated) {
            config.headers.Authorization = `Bearer ${keycloak.token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default axiosInstance;
