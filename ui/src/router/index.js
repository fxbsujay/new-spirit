import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'
import http from '@/utils/http'
import { useUserStore } from '@/stores/user.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Layout',
      component: Layout,
      children: [
        {
          path: '/',
          name: 'Home',
          component: () => import('@/views/home/index.vue')
        },
        {
          path: '/sign-in',
          name: 'SignIn',
          component: () => import('@/views/auth/signIn/index.vue'),
        },
        {
          path: '/sign-up',
          name: 'SignUp',
          component: () => import('@/views/auth/signUp/index.vue'),
        },
        {
          path: '/sign-up/success',
          name: 'SignUpSuccess',
          component: () => import('@/views/auth/signUpSuccess/index.vue'),
        },
        {
          path: '/:code([A-Z0-9]{4,8})',
          name: 'Game',
          component: () => import('@/views/game/index.vue'),
        }
      ]
    },
    {
      path: '/test',
      name: 'Test',
      component: () => import('@/views/test/index.vue')
    }
  ]
})

router.beforeEach((to, from, next) => {
  next()
})

export default router
