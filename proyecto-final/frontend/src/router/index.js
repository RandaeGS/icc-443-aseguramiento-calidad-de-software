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
                },
                {
                    path: '/uikit/formlayout',
                    name: 'formlayout',
                    component: () => import('@/views/uikit/FormLayout.vue')
                },
                {
                    path: '/uikit/input',
                    name: 'input',
                    component: () => import('@/views/uikit/InputDoc.vue')
                },
                {
                    path: '/uikit/button',
                    name: 'button',
                    component: () => import('@/views/uikit/ButtonDoc.vue')
                },
                {
                    path: '/uikit/table',
                    name: 'table',
                    component: () => import('@/views/uikit/TableDoc.vue')
                },
                {
                    path: '/uikit/list',
                    name: 'list',
                    component: () => import('@/views/uikit/ListDoc.vue')
                },
                {
                    path: '/uikit/tree',
                    name: 'tree',
                    component: () => import('@/views/uikit/TreeDoc.vue')
                },
                {
                    path: '/uikit/panel',
                    name: 'panel',
                    component: () => import('@/views/uikit/PanelsDoc.vue')
                },

                {
                    path: '/uikit/overlay',
                    name: 'overlay',
                    component: () => import('@/views/uikit/OverlayDoc.vue')
                },
                {
                    path: '/uikit/media',
                    name: 'media',
                    component: () => import('@/views/uikit/MediaDoc.vue')
                },
                {
                    path: '/uikit/message',
                    name: 'message',
                    component: () => import('@/views/uikit/MessagesDoc.vue')
                },
                {
                    path: '/uikit/file',
                    name: 'file',
                    component: () => import('@/views/uikit/FileDoc.vue')
                },
                {
                    path: '/uikit/menu',
                    name: 'menu',
                    component: () => import('@/views/uikit/MenuDoc.vue')
                },
                {
                    path: '/uikit/charts',
                    name: 'charts',
                    component: () => import('@/views/uikit/ChartDoc.vue')
                },
                {
                    path: '/uikit/misc',
                    name: 'misc',
                    component: () => import('@/views/uikit/MiscDoc.vue')
                },
                {
                    path: '/uikit/timeline',
                    name: 'timeline',
                    component: () => import('@/views/uikit/TimelineDoc.vue')
                },
                {
                    path: '/pages/empty',
                    name: 'empty',
                    component: () => import('@/views/pages/Empty.vue')
                },
                {
                    path: '/pages/crud',
                    name: 'crud',
                    component: () => import('@/views/pages/Crud.vue')
                },
                {
                    path: '/documentation',
                    name: 'documentation',
                    component: () => import('@/views/pages/Documentation.vue')
                }
            ]
        },
        {
            path: '/pages/notfound',
            name: 'notfound',
            component: () => import('@/views/pages/NotFound.vue')
        },

        {
            path: '/auth/login',
            name: 'login',
            component: () => import('@/views/pages/auth/Login.vue')
        },
        {
            path: '/auth/access',
            name: 'accessDenied',
            component: () => import('@/views/pages/auth/Access.vue')
        },
        {
            path: '/auth/error',
            name: 'error',
            component: () => import('@/views/pages/auth/Error.vue')
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
        // Esperar a que Keycloak esté listo
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
