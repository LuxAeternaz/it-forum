import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUser } from '../utils/storage'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/HomeView.vue') },
      { path: 'post/create', name: 'PostCreate', component: () => import('../views/PostCreateView.vue'), meta: { requiresAuth: true } },
      { path: 'post/:id/edit', name: 'PostEdit', component: () => import('../views/PostEditView.vue'), meta: { requiresAuth: true } },
      { path: 'post/:id', name: 'PostDetail', component: () => import('../views/PostDetailView.vue') },
      { path: 'user/:id', name: 'UserProfile', component: () => import('../views/UserProfileView.vue') },
      { path: 'profile', name: 'MyProfile', component: () => import('../views/UserSettingsView.vue'), meta: { requiresAuth: true } },
      { path: 'bookmarks', name: 'Bookmarks', component: () => import('../views/MyBookmarksView.vue'), meta: { requiresAuth: true } },
      { path: 'notifications', name: 'Notifications', component: () => import('../views/NotificationListView.vue'), meta: { requiresAuth: true } },
      { path: 'search', name: 'Search', component: () => import('../views/SearchResultView.vue') },
      { path: 'category/:id', name: 'CategoryPosts', component: () => import('../views/CategoryPostsView.vue') },
      { path: 'agents', name: 'Agents', component: () => import('../views/AgentListView.vue') },
      { path: 'agents/create', name: 'AgentCreate', component: () => import('../views/AgentCreateView.vue'), meta: { requiresAuth: true } },
      { path: 'agents/:id', name: 'AgentDetail', component: () => import('../views/AgentDetailView.vue') },
      { path: 'agents/:id/edit', name: 'AgentEdit', component: () => import('../views/AgentEditView.vue'), meta: { requiresAuth: true } }
    ]
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('../views/admin/AdminDashboard.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/AdminUsers.vue') },
      { path: 'posts', name: 'AdminPosts', component: () => import('../views/admin/AdminPosts.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('../views/admin/AdminCategories.vue') },
      { path: 'moderation', name: 'AdminModeration', component: () => import('../views/admin/AdminModeration.vue') }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { guest: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  const user = getUser()

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.guest && token) {
    next('/')
  } else if (to.meta.requiresAdmin && user?.role !== 'ADMIN') {
    next('/')
  } else {
    next()
  }
})

export default router
