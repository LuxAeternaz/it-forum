<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { searchPosts, searchComments } from '../api/search'
import PostListItem from '../components/post/PostListItem.vue'
import CommentSearchItem from '../components/comment/CommentSearchItem.vue'

const route = useRoute()
const activeTab = ref('posts')

const posts = ref([])
const postsTotal = ref(0)
const comments = ref([])
const commentsTotal = ref(0)
const page = ref(1)
const loading = ref(false)

async function fetch() {
  const q = route.query.q
  if (!q) return
  loading.value = true
  try {
    if (activeTab.value === 'posts') {
      const res = await searchPosts({ q, page: page.value })
      posts.value = res.list
      postsTotal.value = res.total
    } else {
      const res = await searchComments({ q, page: page.value })
      comments.value = res.list
      commentsTotal.value = res.total
    }
  } finally { loading.value = false }
}

onMounted(fetch)
watch(() => route.query.q, () => { page.value = 1; fetch() })
watch(activeTab, () => { page.value = 1; fetch() })

function goPage(p) { page.value = p; fetch() }
</script>

<template>
  <div class="search-page">
    <h3 class="search-title">搜索 "{{ route.query.q }}"</h3>

    <div class="search-tabs">
      <button :class="['tab-btn', { active: activeTab === 'posts' }]" @click="activeTab = 'posts'">
        帖子 ({{ postsTotal }})
      </button>
      <button :class="['tab-btn', { active: activeTab === 'comments' }]" @click="activeTab = 'comments'">
        评论 ({{ commentsTotal }})
      </button>
    </div>

    <el-skeleton :loading="loading" animated :count="4">
      <template v-if="activeTab === 'posts'">
        <PostListItem v-for="p in posts" :key="p.id" :post="p" />
        <div v-if="!posts.length" class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <span>未找到相关帖子</span>
        </div>
      </template>
      <template v-else>
        <CommentSearchItem v-for="c in comments" :key="c.commentId" :comment="c" />
        <div v-if="!comments.length" class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <span>未找到相关评论</span>
        </div>
      </template>
    </el-skeleton>

    <div v-if="(activeTab === 'posts' ? postsTotal : commentsTotal) > 20" class="pagination-wrap">
      <button
        v-for="p in Math.min(Math.ceil((activeTab === 'posts' ? postsTotal : commentsTotal) / 20), 7)"
        :key="p"
        :class="['page-btn', { active: p === page }]"
        @click="goPage(p)"
      >{{ p }}</button>
    </div>
  </div>
</template>

<style scoped>
.search-page { max-width: 860px; }
.search-title { font-size: 18px; font-weight: 700; color: var(--color-text); margin-bottom: 12px; }

.search-tabs {
  display: flex; gap: 0; margin-bottom: 20px;
  border-bottom: 2px solid var(--color-border-light);
}
.tab-btn {
  padding: 8px 20px; border: none; background: none;
  font-family: var(--font-family); font-size: 14px; font-weight: 500;
  color: var(--color-text-secondary); cursor: pointer;
  border-bottom: 2px solid transparent; margin-bottom: -2px;
  transition: all var(--transition-fast);
}
.tab-btn:hover { color: var(--color-primary); }
.tab-btn.active {
  color: var(--color-primary); border-bottom-color: var(--color-primary);
}

.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 60px 0; color: var(--color-text-muted); font-size: 14px;
}

.pagination-wrap { display: flex; justify-content: center; gap: 6px; margin-top: 20px; }
.page-btn {
  width: 36px; height: 36px; border: 1px solid var(--color-border);
  border-radius: var(--radius-md); background: var(--color-bg);
  color: var(--color-text-secondary); font-size: 13px; font-weight: 500;
  font-family: var(--font-family); cursor: pointer;
  transition: all var(--transition-fast);
}
.page-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.page-btn.active { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }
</style>
