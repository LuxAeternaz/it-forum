<script setup>
import { ref, onMounted } from 'vue'
import { getPosts } from '../api/post'
import { getCategories } from '../api/category'
import PostListItem from '../components/post/PostListItem.vue'

const posts = ref([])
const total = ref(0)
const page = ref(1)
const sort = ref('latest')
const loading = ref(false)
const categories = ref([])
const hotPosts = ref([])

async function fetchPosts() {
  loading.value = true
  try {
    const res = await getPosts({ sort: sort.value, page: page.value })
    posts.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function fetchSidebar() {
  try {
    const [cats, hot] = await Promise.all([
      getCategories(),
      getPosts({ sort: 'hot', size: 5 })
    ])
    categories.value = cats
    hotPosts.value = hot.list
  } catch {}
}

function switchSort(s) {
  sort.value = s
  page.value = 1
  fetchPosts()
}

onMounted(() => {
  fetchPosts()
  fetchSidebar()
})

function goPage(p) {
  page.value = p
  fetchPosts()
}
</script>

<template>
  <div class="home-layout">
    <!-- Main Content -->
    <div class="home-main">
      <div class="sort-bar">
        <div class="sort-pills">
          <button :class="['sort-pill', { active: sort === 'latest' }]" @click="switchSort('latest')">最新</button>
          <button :class="['sort-pill', { active: sort === 'hot' }]" @click="switchSort('hot')">热门</button>
        </div>
      </div>

      <el-skeleton :loading="loading" animated :count="5">
        <template v-if="posts.length">
          <PostListItem v-for="post in posts" :key="post.id" :post="post" />
        </template>
        <div v-else class="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          <span>暂无帖子</span>
        </div>
      </el-skeleton>

      <div v-if="total > 20" class="pagination-wrap">
        <button
          v-for="p in Math.min(Math.ceil(total / 20), 7)"
          :key="p"
          :class="['page-btn', { active: p === page }]"
          @click="goPage(p)"
        >{{ p }}</button>
      </div>
    </div>

    <!-- Right Sidebar -->
    <aside class="home-sidebar">
      <!-- AI Agent Card -->
      <router-link to="/agents" class="sidebar-card ai-card">
        <div class="ai-card-header">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2a3 3 0 00-3 3v2a3 3 0 006 0V5a3 3 0 00-3-3z"/><path d="M2 12h2a8 8 0 0114 0h2"/><circle cx="12" cy="18" r="4"/></svg>
          <span>AI 智能体广场</span>
        </div>
        <p class="ai-desc">浏览用户创建的AI智能体，在评论中 @名称 即可召唤专属AI助手。</p>
        <div class="ai-tags">
          <span>@IT小助手</span>
          <span>创建我的智能体</span>
        </div>
      </router-link>

      <!-- Trending Posts -->
      <div class="sidebar-card trending-card">
        <h4 class="sidebar-title">今日热门</h4>
        <div v-if="hotPosts.length" class="trending-list">
          <a
            v-for="(p, i) in hotPosts"
            :key="p.id"
            :href="`/post/${p.id}`"
            class="trending-item"
          >
            <span class="trending-rank" :class="`rank-${i + 1}`">{{ i + 1 }}</span>
            <span class="trending-title">{{ p.title }}</span>
          </a>
        </div>
        <div v-else class="sidebar-empty">暂无数据</div>
      </div>

      <!-- Categories -->
      <div class="sidebar-card cats-card">
        <h4 class="sidebar-title">版块导航</h4>
        <div class="cats-list">
          <a
            v-for="c in categories"
            :key="c.id"
            :href="`/category/${c.id}`"
            class="cat-item"
          >
            <span class="cat-name">{{ c.name }}</span>
            <span class="cat-count">{{ c.postCount || 0 }}</span>
          </a>
        </div>
      </div>
    </aside>
  </div>
</template>

<style scoped>
.home-layout { display: flex; gap: 24px; }
.home-main { flex: 1; min-width: 0; }
.home-sidebar { width: 280px; flex-shrink: 0; }

/* Sort Pills */
.sort-bar { margin-bottom: 16px; }
.sort-pills { display: flex; gap: 8px; }
.sort-pill {
  padding: 7px 20px; border: 1px solid var(--color-border);
  border-radius: var(--radius-full); background: var(--color-bg);
  color: var(--color-text-secondary); font-size: 13px; font-weight: 600;
  font-family: var(--font-family); cursor: pointer;
  transition: all var(--transition-fast);
}
.sort-pill:hover { border-color: var(--color-primary); color: var(--color-primary); }
.sort-pill.active {
  background: var(--color-primary); color: #fff; border-color: var(--color-primary);
  box-shadow: 0 2px 8px rgba(64,158,255,0.3);
}

/* Pagination */
.pagination-wrap {
  display: flex; justify-content: center; gap: 6px; margin-top: 20px;
}
.page-btn {
  width: 36px; height: 36px; border: 1px solid var(--color-border);
  border-radius: var(--radius-md); background: var(--color-bg);
  color: var(--color-text-secondary); font-size: 13px; font-weight: 500;
  font-family: var(--font-family); cursor: pointer;
  transition: all var(--transition-fast);
}
.page-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.page-btn.active { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }

.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 60px 0; color: var(--color-text-muted); font-size: 14px;
}

/* Sidebar */
.sidebar-card {
  background: var(--color-bg); border-radius: var(--radius-lg);
  padding: 20px; margin-bottom: 16px;
  border: 1px solid var(--color-border-light);
}
.sidebar-title {
  font-size: 14px; font-weight: 700; color: var(--color-text);
  margin-bottom: 14px;
}

/* AI Card */
.ai-card {
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  border: 1px solid var(--color-primary-light);
  text-decoration: none; display: block;
}
.ai-card-header {
  display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 700; color: var(--color-indigo);
  margin-bottom: 10px;
}
.ai-desc { font-size: 13px; color: var(--color-text-secondary); line-height: 1.6; margin-bottom: 12px; }
.ai-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.ai-tags span {
  display: inline-block; padding: 3px 10px;
  border-radius: var(--radius-full); background: rgba(99,102,241,0.1);
  color: var(--color-indigo); font-size: 12px; font-weight: 600;
}

/* Trending */
.trending-list { display: flex; flex-direction: column; gap: 2px; }
.trending-item {
  display: flex; align-items: center; gap: 10px; padding: 8px 0;
  text-decoration: none; border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}
.trending-item:hover { background: var(--color-bg-slate); }
.trending-rank {
  width: 22px; height: 22px; display: flex; align-items: center; justify-content: center;
  border-radius: 6px; font-size: 12px; font-weight: 700; flex-shrink: 0;
  background: var(--color-bg-slate); color: var(--color-text-secondary);
}
.rank-1 { background: #f56c6c; color: #fff; }
.rank-2 { background: #f89860; color: #fff; }
.rank-3 { background: #e6a23c; color: #fff; }
.trending-title {
  font-size: 13px; color: var(--color-text); font-weight: 500;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

/* Categories */
.cats-list { display: flex; flex-direction: column; gap: 2px; }
.cat-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 9px 10px; text-decoration: none; border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}
.cat-item:hover { background: var(--color-bg-slate); }
.cat-name { font-size: 13px; color: var(--color-text); font-weight: 500; }
.cat-count { font-size: 12px; color: var(--color-text-muted); }

.sidebar-empty { font-size: 13px; color: var(--color-text-muted); text-align: center; padding: 16px 0; }

@media (max-width: 960px) {
  .home-sidebar { display: none; }
}
</style>
