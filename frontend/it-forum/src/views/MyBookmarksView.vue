<script setup>
import { ref, onMounted } from 'vue'
import { getBookmarks } from '../api/bookmark'
import PostListItem from '../components/post/PostListItem.vue'

const posts = ref([])
const total = ref(0)
const page = ref(1)

async function fetch() {
  const res = await getBookmarks(page.value)
  posts.value = res.list
  total.value = res.total
}

onMounted(fetch)

function goPage(p) { page.value = p; fetch() }
</script>

<template>
  <div class="bookmarks-page">
    <h2 class="page-title">我的收藏</h2>
    <PostListItem v-for="p in posts" :key="p.id" :post="p" />
    <div v-if="!posts.length" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3"><path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z"/></svg>
      <span>暂无收藏的帖子</span>
    </div>
    <div v-if="total > 20" class="pagination-wrap">
      <button
        v-for="p in Math.min(Math.ceil(total / 20), 7)"
        :key="p"
        :class="['page-btn', { active: p === page }]"
        @click="goPage(p)"
      >{{ p }}</button>
    </div>
  </div>
</template>

<style scoped>
.bookmarks-page { max-width: 860px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: 16px; }
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
