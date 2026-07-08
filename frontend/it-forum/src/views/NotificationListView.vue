<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markRead, markAllRead, getUnreadCount } from '../api/notification'
import { relativeTime } from '../utils/time'
import { ElMessage } from 'element-plus'

const router = useRouter()
const notifications = ref([])
const total = ref(0)
const page = ref(1)
const type = ref('')
const unreadCount = ref(0)

async function fetch() {
  const res = await getNotifications({ type: type.value || null, page: page.value })
  notifications.value = res.list
  total.value = res.total
}

async function fetchUnread() {
  try { unreadCount.value = await getUnreadCount() } catch {}
}

async function handleClick(notif) {
  if (!notif.isRead) {
    await markRead(notif.id)
    notif.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  if (notif.targetType === 'POST' && notif.targetId) {
    router.push(`/post/${notif.targetId}`)
  } else if (notif.targetType === 'USER' && notif.targetId) {
    router.push(`/profile/${notif.targetId}`)
  }
}

async function handleMarkAll() {
  await markAllRead()
  ElMessage.success('已全部标记为已读')
  unreadCount.value = 0
  fetch()
}

function changeType(t) {
  type.value = t
  page.value = 1
  fetch()
}

const typeIcon = (t) => {
  if (t === 'REPLY') return '💬'
  if (t === 'LIKE') return '❤️'
  if (t === 'FOLLOW') return '👤'
  if (t === 'MENTION') return '🤖'
  return '🔔'
}

onMounted(() => {
  fetch()
  fetchUnread()
})
</script>

<template>
  <div class="notif-page">
    <div class="notif-header">
      <h2 class="page-title">通知</h2>
      <div class="notif-header-actions">
        <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }} 条未读</span>
        <button class="mark-all-btn" @click="handleMarkAll">全部标为已读</button>
      </div>
    </div>

    <div class="type-filters">
      <button :class="['filter-btn', { active: type === '' }]" @click="changeType('')">全部</button>
      <button :class="['filter-btn', { active: type === 'REPLY' }]" @click="changeType('REPLY')">回复</button>
      <button :class="['filter-btn', { active: type === 'LIKE' }]" @click="changeType('LIKE')">点赞</button>
      <button :class="['filter-btn', { active: type === 'FOLLOW' }]" @click="changeType('FOLLOW')">关注</button>
      <button :class="['filter-btn', { active: type === 'MENTION' }]" @click="changeType('MENTION')">@提及</button>
    </div>

    <div class="card notif-list">
      <div
        v-for="n in notifications"
        :key="n.id"
        :class="['notif-item', { unread: !n.isRead }]"
        @click="handleClick(n)"
      >
        <span class="notif-icon">{{ typeIcon(n.type) }}</span>
        <div class="notif-body">
          <div class="notif-title">{{ n.title }}</div>
          <div class="notif-content">{{ n.content }}</div>
          <div class="notif-time">{{ relativeTime(n.createdAt) }}</div>
        </div>
        <span v-if="!n.isRead" class="notif-dot" />
      </div>
      <div v-if="!notifications.length" class="notif-empty">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>
        <span>暂无通知</span>
      </div>
    </div>

    <div v-if="total > 20" class="pagination-wrap">
      <button
        v-for="p in Math.min(Math.ceil(total / 20), 7)"
        :key="p"
        :class="['page-btn', { active: p === page }]"
        @click="page = p; fetch()"
      >{{ p }}</button>
    </div>
  </div>
</template>

<style scoped>
.notif-page { max-width: 720px; }
.notif-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.notif-header-actions { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); }
.unread-badge { font-size: 12px; color: var(--color-primary); font-weight: 600; }
.mark-all-btn {
  padding: 6px 16px; border: 1px solid var(--color-border);
  border-radius: var(--radius-full); background: transparent;
  color: var(--color-text-secondary); font-size: 12px; font-family: var(--font-family); cursor: pointer;
  transition: all var(--transition-fast);
}
.mark-all-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }

.type-filters { display: flex; gap: 8px; margin-bottom: 16px; }
.filter-btn {
  padding: 6px 16px; border: 1px solid var(--color-border);
  border-radius: var(--radius-full); background: transparent;
  color: var(--color-text-secondary); font-size: 12px; font-weight: 600;
  font-family: var(--font-family); cursor: pointer;
  transition: all var(--transition-fast);
}
.filter-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.filter-btn.active {
  background: var(--color-primary); color: #fff; border-color: var(--color-primary);
}

.notif-list { padding: 0; overflow: hidden; }
.notif-item {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 16px 20px; cursor: pointer;
  transition: background var(--transition-fast);
  border-bottom: 1px solid var(--color-border-light);
}
.notif-item:last-child { border-bottom: none; }
.notif-item:hover { background: var(--color-bg-slate); }
.notif-item.unread { background: #f0f9ff; }
.notif-item.unread:hover { background: #e6f3fc; }

.notif-icon { font-size: 18px; flex-shrink: 0; margin-top: 2px; }
.notif-body { flex: 1; min-width: 0; }
.notif-title { font-size: 14px; font-weight: 600; color: var(--color-text); margin-bottom: 2px; }
.notif-content { font-size: 13px; color: var(--color-text-secondary); line-height: 1.5; }
.notif-time { font-size: 12px; color: var(--color-text-muted); margin-top: 4px; }
.notif-dot {
  width: 8px; height: 8px; border-radius: 50%; background: var(--color-primary);
  flex-shrink: 0; margin-top: 6px;
}

.notif-empty {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 48px 0; color: var(--color-text-muted); font-size: 14px;
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
