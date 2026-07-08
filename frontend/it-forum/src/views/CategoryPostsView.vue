<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getPosts } from '../api/post'
import { getCategories } from '../api/category'
import PostListItem from '../components/post/PostListItem.vue'

const route = useRoute()
const posts = ref([])
const total = ref(0)
const page = ref(1)
const categoryName = ref('')

async function fetch() {
  const [res, cats] = await Promise.all([
    getPosts({ categoryId: route.params.id, page: page.value }),
    getCategories()
  ])
  posts.value = res.list
  total.value = res.total
  const cat = cats.find(c => c.id === Number(route.params.id))
  categoryName.value = cat?.name || '版块'
}

onMounted(fetch)
watch(() => route.params.id, () => { page.value = 1; fetch() })

function goPage(p) { page.value = p; fetch() }
</script>

<template>
  <div class="category-page">
    <h2 class="page-title">{{ categoryName }}</h2>
    <PostListItem v-for="p in posts" :key="p.id" :post="p" />
    <div v-if="!posts.length" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
      <span>该版块暂无帖子</span>
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
.category-page { max-width: 860px; }
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
