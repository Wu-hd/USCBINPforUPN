import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
const publicRoutes = new Set(['/login', '/403']);
export const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/login',
            name: 'login',
            component: () => import('@/views/LoginView.vue'),
        },
        {
            path: '/',
            component: () => import('@/layouts/MainLayout.vue'),
            children: [
                {
                    path: '',
                    name: 'dashboard',
                    component: () => import('@/views/DashboardView.vue'),
                },
            ],
        },
        {
            path: '/403',
            name: 'forbidden',
            component: () => import('@/views/ForbiddenView.vue'),
        },
        {
            path: '/:pathMatch(.*)*',
            name: 'not-found',
            component: () => import('@/views/NotFoundView.vue'),
        },
    ],
});
router.beforeEach(async (to) => {
    const authStore = useAuthStore();
    if (!authStore.username && authStore.token) {
        try {
            await authStore.bootstrap();
        }
        catch {
            authStore.clearAuth();
        }
    }
    if (!authStore.isLoggedIn && !publicRoutes.has(to.path)) {
        return '/login';
    }
    if (authStore.isLoggedIn && to.path === '/login') {
        return '/';
    }
    return true;
});
//# sourceMappingURL=index.js.map