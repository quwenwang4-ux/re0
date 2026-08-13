import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/', component: () => import('../layouts/AppLayout.vue'), children: [
      { path: '', name: 'home', component: () => import('../views/HomeView.vue') },
      { path: 'fishes', name: 'fishes', component: () => import('../views/FishListView.vue') },
      { path: 'fishes/:id', name: 'fish-detail', component: () => import('../views/FishDetailView.vue') },
      { path: 'detect', name: 'detect', component: () => import('../views/DetectionView.vue'), meta: { requiresAuth: true } },
      { path: 'rescue', name: 'rescue', component: () => import('../views/RescueView.vue') },
      { path: 'volunteer', name: 'volunteer', component: () => import('../views/VolunteerView.vue'), meta: { requiresAuth: true } },
      { path: 'fish-applications', name: 'fish-applications', component: () => import('../views/FishApplicationView.vue'), meta: { requiresAuth: true } },
      { path: 'admin', name: 'admin', component: () => import('../views/AdminView.vue'), meta: { requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'profile', name: 'profile', component: () => import('../views/ProfileView.vue'), meta: { requiresAuth: true } },
      { path: 'forbidden', name: 'forbidden', component: () => import('../views/ForbiddenView.vue') },
    ]},
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('../views/RegisterView.vue') },
    { path: '/admin/fishes', name: 'admin-fishes', component: () => import('../views/AdminFishView.vue'), meta: { requiresAuth: true, roles: ['ADMIN'] } },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFoundView.vue') },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  const requiredRoles = to.meta.roles as string[] | undefined
  if (to.meta.requiresAuth && !auth.isLoggedIn) return { name: 'login', query: { redirect: to.fullPath } }
  if (requiredRoles?.length && !auth.hasAnyRole(requiredRoles)) return { name: 'forbidden' }
  if ((to.name === 'login' || to.name === 'register') && auth.isLoggedIn) return { name: 'home' }
  return true
})

export default router
