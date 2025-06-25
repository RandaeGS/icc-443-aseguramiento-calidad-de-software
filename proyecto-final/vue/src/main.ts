import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

// @ts-ignore
import App from './App.vue'
import router from './router'
import { PrimeVue } from '@primevue/core'
import Aura from '@primeuix/themes/aura'
import { ToastService } from 'primevue'

const app = createApp(App)

app.use(PrimeVue, {
  theme: {
    preset: Aura,
  },
})
app.use(ToastService)
app.use(createPinia())
app.use(router)

app.mount('#app')
