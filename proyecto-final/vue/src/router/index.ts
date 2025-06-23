import ProductoView from '@/views/ProductoView.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'productos',
      component: ProductoView,
    },
  ],
})

export default router
