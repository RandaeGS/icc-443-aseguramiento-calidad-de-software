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

app.use(router);

app.use(VueKeyCloak, {
    init: {
        onLoad: 'check-sso',
        checkLoginIfFrame: false,
        flow: 'standard'
    },
    config: {
        url: 'http://localhost:7080',
        realm: 'project',
        clientId: 'vue',
        scope: 'openid profile email uma_authorization'
    },
    onReady: () => {
        console.log('App is ready!');
    }
});
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
