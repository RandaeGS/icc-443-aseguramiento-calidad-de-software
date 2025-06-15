import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import axiosInstance from './utils/axios'

const app = createApp(App)

axiosInstance.defaults.baseURL = 'http://localhost:8080'
app.config.globalProperties.$axios = axiosInstance

app.use(createPinia())
app.use(router)
app.mount('#app')
