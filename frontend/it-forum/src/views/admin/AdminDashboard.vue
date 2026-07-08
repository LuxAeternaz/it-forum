<script setup>
import { ref, onMounted } from 'vue'
import { getDashboard } from '../../api/admin'

const stats = ref(null)

onMounted(async () => {
  stats.value = await getDashboard()
})

const cards = [
  { key: 'totalUsers', label: '用户总数', color: '#409eff', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
  { key: 'totalPosts', label: '帖子总数', color: '#67c23a', icon: 'M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z' },
  { key: 'totalComments', label: '评论总数', color: '#e6a23c', icon: 'M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z' },
  { key: 'todayPosts', label: '今日帖子', color: '#409eff', icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z' },
  { key: 'todayComments', label: '今日评论', color: '#e6a23c', icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z' },
  { key: 'pendingModeration', label: '待审核', color: '#f56c6c', icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z' },
]
</script>

<template>
  <div class="dashboard">
    <h1 class="page-title">仪表盘</h1>
    <div class="stats-grid">
      <div v-for="card in cards" :key="card.key" class="stat-card">
        <div class="stat-icon" :style="{ background: card.color + '18', color: card.color }">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path :d="card.icon"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats?.[card.key] ?? '—' }}</span>
          <span class="stat-label">{{ card.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title { font-size: 22px; font-weight: 700; color: #1e293b; margin-bottom: 24px; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 18px; }
.stat-card {
  display: flex; align-items: center; gap: 16px;
  background: #fff; padding: 20px; border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 26px; font-weight: 700; color: #1e293b; }
.stat-label { font-size: 13px; color: #64748b; margin-top: 2px; }
</style>
