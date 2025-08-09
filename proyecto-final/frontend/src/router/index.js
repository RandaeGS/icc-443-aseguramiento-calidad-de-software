import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory } from 'vue-router';
import { useKeycloak } from '@dsb-norge/vue-keycloak-js';

// Variable global para trackear el estado de Keycloak
let keycloakReady = false;
let keycloakReadyPromise = null;

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'landing',
            component: () => import('@/views/pages/Landing.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/products',
            component: AppLayout,
            children: [
                {
                    path: 'dashboard',
                    name: 'dashboard',
                    component: () => import('@/views/pages/Dashboard.vue')
                },
                {
                    path: '',
                    name: 'products',
                    component: () => import('@/views/pages/Products.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: ':id/history',
                    name: 'history',
                    component: () => import('@/views/pages/ProductHistory.vue'),
                    props: true,
                    meta: { requiresAuth: true },
                    beforeEnter: (to, from, next) => {
                        const id = parseInt(to.params.id);
                        if (isNaN(id) || id <= 0) {
                            next('/products');
                        } else {
                            next();
                        }
                    }
                }
            ]
        },
        {
            path: '/pages/notfound',
            name: 'notfound',
            component: () => import('@/views/pages/NotFound.vue')
        }
    ]
});

// Función para esperar a que Keycloak esté listo
function waitForKeycloak() {
    if (keycloakReady) {
        return Promise.resolve();
    }

    if (!keycloakReadyPromise) {
        keycloakReadyPromise = new Promise((resolve) => {
            const checkReady = () => {
                const keycloak = useKeycloak();
                if (keycloak && keycloak.ready) {
                    keycloakReady = true;
                    resolve();
                } else {
                    setTimeout(checkReady, 100);
                }
            };
            checkReady();
        });
    }

    return keycloakReadyPromise;
}

// Navigation Guard
router.beforeEach(async (to, from, next) => {
    try {
        await waitForKeycloak();

        const keycloak = useKeycloak();
        const requiresAuth = to.matched.some((record) => record.meta.requiresAuth === true);

        if (requiresAuth && !keycloak.authenticated) {
            await keycloak.login({
                redirectUri: window.location.origin + to.fullPath
            });
        } else {
            return next();
        }
    } catch (error) {
        console.error('Navigation guard error:', error);
        return next();
    }
});

export default router;
