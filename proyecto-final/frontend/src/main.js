import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

import Aura from '@primeuix/themes/aura';
import PrimeVue from 'primevue/config';
import ConfirmationService from 'primevue/confirmationservice';
import ToastService from 'primevue/toastservice';

import '@/assets/styles.scss';
import VueKeyCloak from '@dsb-norge/vue-keycloak-js';

const app = createApp(App);
app.use(VueKeyCloak, {
    config: {
        url: 'http://localhost:7080',
        realm: 'project',
        clientId: 'vue'
    },
    onReady: () => {
        console.log('App is ready!');
    }
});

app.use(router);
app.use(PrimeVue, {
    theme: {
        preset: Aura,
        options: {
            darkModeSelector: '.app-dark'
        }
    }
});
app.use(ToastService);
app.use(ConfirmationService);

app.mount('#app');
