import Layout from '@/layout/index.vue'
import { createRouter, createWebHistory } from 'vue-router'

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
                    component: () => import('@/views/auth/signIn/index.vue')
                },
                {
                    path: '/sign-up',
                    name: 'SignUp',
                    component: () => import('@/views/auth/signUp/index.vue')
                },
                {
                    path: '/sign-up/success',
                    name: 'SignUpSuccess',
                    component: () => import('@/views/auth/signUpSuccess/index.vue')
                },
                {
                    path: '/@:username([A-Za-z][a-zA-Z0-9]{1,19})',
                    name: 'Profile',
                    component: () => import('@/views/account/profile/index.vue')
                },
                {
                    path: '/account',
                    name: 'AccountHome',
                    component: () => import('@/views/account/home/index.vue')
                },
                {
                    path: '/:code([A-Z0-9]{4,8})',
                    name: 'Game',
                    component: () => import('@/views/game/index.vue')
                },
                {
                    // 兜底路由：所有未匹配的地址都渲染 404 页面，需放在 children 最后
                    path: '/:pathMatch(.*)*',
                    name: '404',
                    component: () => import('@/components/404/index.vue')
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


export default router
