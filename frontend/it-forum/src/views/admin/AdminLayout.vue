<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const menuItems = [
  { path: '/admin', label: '仪表盘', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
  { path: '/admin/users', label: '用户管理', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
  { path: '/admin/posts', label: '帖子管理', icon: 'M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z' },
  { path: '/admin/categories', label: '分类管理', icon: 'M4 6h16M4 10h16M4 14h16M4 18h16' },
  { path: '/admin/moderation', label: '审核日志', icon: 'M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z' }
]

function isActive(path) {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}
</script>

<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <h2>管理后台</h2>
      </div>
      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          :class="['nav-item', { active: isActive(item.path) }]"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path :d="item.icon"/>
          </svg>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <router-link to="/" class="back-link">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
          </svg>
          <span>返回论坛</span>
        </router-link>
      </div>
    </aside>
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-layout { display: flex; min-height: calc(100vh - 56px); }

.admin-sidebar {
  width: 220px; background: #1e293b; color: #e2e8f0;
  display: flex; flex-direction: column; flex-shrink: 0;
}
.sidebar-header {
  padding: 20px 20px 16px; border-bottom: 1px solid rgba(255,255,255,0.08);
}
.sidebar-header h2 { font-size: 16px; font-weight: 700; letter-spacing: 0.5px; }
.sidebar-nav { flex: 1; padding: 12px 0; }
.nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 20px; font-size: 14px; color: #94a3b8;
  text-decoration: none; transition: all 0.15s;
  border-left: 3px solid transparent;
}
.nav-item:hover { color: #e2e8f0; background: rgba(255,255,255,0.05); }
.nav-item.active {
  color: #fff; background: rgba(64,158,255,0.15);
  border-left-color: #409eff; font-weight: 600;
}
.sidebar-footer { padding: 12px 20px; border-top: 1px solid rgba(255,255,255,0.08); }
.back-link {
  display: flex; align-items: center; gap: 8px;
  color: #64748b; text-decoration: none; font-size: 13px; transition: color 0.15s;
}
.back-link:hover { color: #e2e8f0; }
.admin-main { flex: 1; padding: 28px; background: #f1f5f9; overflow-y: auto; min-width: 0; }
</style>
