<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getCategories } from '../api/category'
import { getUnreadCount } from '../api/notification'
import { Search, Bell, HomeFilled, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const categories = ref([])
const unreadCount = ref(0)
const searchKeyword = ref('')

onMounted(async () => {
  try { categories.value = await getCategories() } catch {}
  await refreshUnread()
})

watch(() => route.path, () => {
  if (auth.isLoggedIn) refreshUnread()
})

async function refreshUnread() {
  if (!auth.isLoggedIn) return
  try { unreadCount.value = await getUnreadCount() } catch {}
}

const activeMenu = computed(() => route.path)

const avatarUrl = computed(() => {
  if (auth.user?.avatarUrl) return auth.user.avatarUrl
  return '/default-avatar.svg'
})

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Search', query: { q: searchKeyword.value.trim() } })
  }
}

function handleLogout() {
  auth.logout()
}
</script>

<template>
  <div class="app-shell">
    <!-- Header -->
    <header class="app-header glass-card">
      <div class="header-left">
        <router-link to="/" class="header-logo">
          <span class="logo-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="16" rx="2"/><rect x="9" y="9" width="6" height="6"/><line x1="9" y1="1" x2="9" y2="4"/><line x1="15" y1="1" x2="15" y2="4"/><line x1="9" y1="20" x2="9" y2="23"/><line x1="15" y1="20" x2="15" y2="23"/><line x1="20" y1="9" x2="23" y2="9"/><line x1="20" y1="14" x2="23" y2="14"/><line x1="1" y1="9" x2="4" y2="9"/><line x1="1" y1="14" x2="4" y2="14"/></svg>
          </span>
          <span class="gradient-text logo-text">智能化技术论坛</span>
        </router-link>
        <div class="search-box">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索帖子..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
      </div>

      <div class="header-right">
        <template v-if="auth.isLoggedIn">
          <router-link to="/post/create">
            <button class="btn-post">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              发布新帖
            </button>
          </router-link>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <button class="btn-icon" @click="router.push('/notifications')">
              <el-icon :size="18"><Bell /></el-icon>
            </button>
          </el-badge>
          <el-dropdown trigger="click" @command="(cmd) => { if (cmd === 'logout') handleLogout(); else router.push(cmd) }">
            <div class="user-trigger">
              <img :src="avatarUrl" class="user-avatar" alt="avatar" />
              <span class="user-name">{{ auth.user?.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="`/user/${auth.userId}`">个人空间</el-dropdown-item>
                <el-dropdown-item command="/profile">账号设置</el-dropdown-item>
                <el-dropdown-item command="/bookmarks">我的收藏</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login"><button class="btn-ghost">登录</button></router-link>
          <router-link to="/register"><button class="btn-primary">注册</button></router-link>
        </template>
      </div>
    </header>

    <div class="app-body">
      <!-- Sidebar -->
      <aside class="app-sidebar">
        <nav class="sidebar-nav">
          <router-link to="/" class="sidebar-item" :class="{ active: route.path === '/' }">
            <el-icon :size="18"><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>

          <div class="sidebar-label">版块分类</div>

          <router-link
            v-for="cat in categories"
            :key="cat.id"
            :to="`/category/${cat.id}`"
            class="sidebar-item"
            :class="{ active: route.path === `/category/${cat.id}` }"
          >
            <span class="cat-dot" />
            <span>{{ cat.name }}</span>
            <span class="cat-count">{{ cat.postCount || 0 }}</span>
          </router-link>
        </nav>

        <router-link to="/agents" class="sidebar-ai-card">
          <div class="ai-card-content">
            <h4>AI 智能体广场</h4>
            <p>浏览所有用户创建的AI智能体，在评论中 @名称 即可召唤</p>
            <div class="ai-card-badge">
              <span class="ai-dot" />
              探索更多
            </div>
          </div>
          <svg class="ai-card-decor" width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2a4 4 0 0 1 4 4v1h-8V6a4 4 0 0 1 4-4z"/><path d="M2 10a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v8a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4v-8z"/><circle cx="9" cy="14" r="1"/><circle cx="15" cy="14" r="1"/><path d="M9 17c.83.67 1.83 1 3 1s2.17-.33 3-1"/></svg>
        </router-link>

        <router-link v-if="auth.isAdmin" to="/admin" class="sidebar-item admin-item">
          <el-icon :size="18"><Setting /></el-icon>
          <span>管理后台</span>
        </router-link>
      </aside>

      <!-- Main Content -->
      <main class="app-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell { min-height: 100vh; background: var(--color-bg); }

/* Header */
.app-header {
  position: fixed; top: 0; left: 0; right: 0; z-index: 1000;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; height: 60px;
  border-bottom: 1px solid var(--color-border-light);
}
.header-left { display: flex; align-items: center; gap: 24px; }
.header-logo { display: flex; align-items: center; gap: 8px; text-decoration: none; }
.logo-icon {
  display: flex; align-items: center; justify-content: center;
  width: 34px; height: 34px; border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--color-primary), var(--color-indigo));
  color: #fff;
}
.logo-text { font-size: 20px; font-weight: 700; }

.search-box {
  position: relative; width: 320px;
}
.search-icon {
  position: absolute; left: 14px; top: 50%; transform: translateY(-50%);
  color: var(--color-text-muted); font-size: 15px;
}
.search-input {
  width: 100%; height: 38px;
  padding: 0 16px 0 40px;
  border: 1px solid var(--color-border); border-radius: var(--radius-full);
  background: var(--color-bg-slate);
  font-size: 14px; font-family: var(--font-family);
  color: var(--color-text); outline: none;
  transition: all var(--transition-fast);
}
.search-input:focus { border-color: var(--color-primary); background: #fff; box-shadow: 0 0 0 3px rgba(64,158,255,0.1); }
.search-input::placeholder { color: var(--color-text-muted); }

.header-right { display: flex; align-items: center; gap: 12px; }

/* Buttons */
.btn-post {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 18px; border: none; border-radius: var(--radius-full);
  background: var(--color-primary); color: #fff;
  font-size: 13px; font-weight: 600; font-family: var(--font-family);
  cursor: pointer; transition: all var(--transition-fast);
}
.btn-post:hover { background: var(--color-primary-dark); transform: translateY(-1px); box-shadow: var(--shadow-blue); }

.btn-icon {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border: 1px solid var(--color-border);
  border-radius: 50%; background: #fff; color: var(--color-text-secondary);
  cursor: pointer; transition: all var(--transition-fast);
}
.btn-icon:hover { border-color: var(--color-primary); color: var(--color-primary); }

.btn-primary {
  padding: 8px 20px; border: none; border-radius: var(--radius-full);
  background: var(--color-primary); color: #fff;
  font-size: 13px; font-weight: 600; font-family: var(--font-family);
  cursor: pointer; transition: all var(--transition-fast);
}
.btn-primary:hover { background: var(--color-primary-dark); }

.btn-ghost {
  padding: 8px 20px; border: 1px solid var(--color-border); border-radius: var(--radius-full);
  background: transparent; color: var(--color-text-secondary);
  font-size: 13px; font-weight: 500; font-family: var(--font-family);
  cursor: pointer; transition: all var(--transition-fast);
}
.btn-ghost:hover { border-color: var(--color-primary); color: var(--color-primary); }

/* User */
.user-trigger {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  padding: 4px 8px; border-radius: var(--radius-full);
  transition: background var(--transition-fast);
}
.user-trigger:hover { background: var(--color-bg-slate); }
.user-avatar { width: 32px; height: 32px; border-radius: 50%; border: 2px solid var(--color-border-light); object-fit: cover; }
.user-name { font-size: 14px; font-weight: 500; color: var(--color-text); max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* Body */
/* Body */
.app-body { padding-top: 60px; }

/* Sidebar */
.app-sidebar {
  position: fixed; top: 60px; left: 0; bottom: 0; width: 220px;
  background: var(--color-bg-white); border-right: 1px solid var(--color-border-light);
  padding: 16px 12px; overflow-y: auto;
  display: flex; flex-direction: column; z-index: 100;
}
.sidebar-nav { flex: 1; }
.sidebar-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; border-radius: var(--radius-md);
  font-size: 14px; color: var(--color-text-secondary); text-decoration: none;
  transition: all var(--transition-fast);
}
.sidebar-item:hover { background: var(--color-bg-slate); color: var(--color-text); }
.sidebar-item.active { background: var(--color-primary-light); color: var(--color-primary); font-weight: 600; }
.cat-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--color-text-muted); flex-shrink: 0; }
.sidebar-item.active .cat-dot { background: var(--color-primary); }
.cat-count { margin-left: auto; font-size: 12px; color: var(--color-text-muted); background: var(--color-bg-slate); padding: 1px 7px; border-radius: 10px; }
.sidebar-label { padding: 16px 12px 8px; font-size: 11px; font-weight: 600; color: var(--color-text-muted); text-transform: uppercase; letter-spacing: 0.5px; }

.admin-item { margin-top: 12px; color: var(--color-primary); border-top: 1px solid var(--color-border-light); padding-top: 16px; }
.admin-item:hover { background: var(--color-primary-light); }
.admin-item.active { background: var(--color-primary-light); color: var(--color-primary); font-weight: 600; }

/* AI card in sidebar */
.sidebar-ai-card {
  position: relative; margin-top: 12px; padding: 16px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--color-indigo), var(--color-primary));
  color: #fff; overflow: hidden; text-decoration: none; display: block;
}
.ai-card-content { position: relative; z-index: 1; }
.ai-card-content h4 { font-size: 14px; font-weight: 700; margin-bottom: 6px; }
.ai-card-content p { font-size: 11px; opacity: 0.85; line-height: 1.5; margin-bottom: 10px; }
.ai-card-badge { display: inline-flex; align-items: center; gap: 5px; font-size: 11px; background: rgba(255,255,255,0.2); padding: 3px 10px; border-radius: var(--radius-full); }
.ai-dot { width: 6px; height: 6px; border-radius: 50%; background: #4ade80; }
.ai-card-decor { position: absolute; bottom: -16px; right: -16px; opacity: 0.12; z-index: 0; }

/* Main content — centered in the remaining space after sidebar */
.app-main {
  max-width: 900px; margin: 0 auto;
  padding: 24px 24px 60px;
  width: 100%;
}
</style>
