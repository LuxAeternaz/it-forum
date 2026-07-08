<script setup>
import { computed } from 'vue'
import { relativeTime } from '../../utils/time'

const props = defineProps({
  post: { type: Object, required: true }
})

const isAI = computed(() => props.post.userId === '1')
const authorName = computed(() => {
  if (isAI.value) return 'IT小助手'
  return props.post.username || `用户${props.post.userId}`
})
const authorAvatar = computed(() => {
  if (isAI.value) return '/default-bot-avatar.svg'
  if (props.post.avatarUrl) return props.post.avatarUrl
  return '/default-avatar.svg'
})

const previewText = computed(() => {
  if (!props.post.content) return ''
  return props.post.content.replace(/[#*`>\-\[\]!~]/g, '').substring(0, 200)
})
</script>

<template>
  <div class="post-card" @click="$router.push(`/post/${post.id}`)">
    <div class="post-header">
      <router-link :to="`/user/${post.userId}`" class="post-avatar-link" @click.stop>
        <img :src="authorAvatar" class="post-avatar" alt="" />
      </router-link>
      <div class="post-meta">
        <router-link :to="`/user/${post.userId}`" class="post-author-link" @click.stop>{{ authorName }}</router-link>
        <span v-if="isAI" class="ai-badge">AI</span>
        <span class="post-dot">·</span>
        <span class="post-time">{{ relativeTime(post.createdAt) }}</span>
      </div>
    </div>

    <h3 class="post-title">{{ post.title }}</h3>
    <p class="post-preview">{{ previewText }}</p>

    <div class="post-footer">
      <div class="post-stats">
        <span class="stat-item">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          {{ post.commentCount || 0 }} 回复
        </span>
        <span class="stat-item like-stat">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
          {{ post.likeCount || 0 }} 点赞
        </span>
        <span class="stat-item">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/></svg>
          分享
        </span>
      </div>
      <div class="post-last-reply" v-if="post.lastReplyUsername">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
        <span>{{ post.lastReplyUsername }} 最后回复</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.post-card {
  background: var(--color-bg-white);
  border-radius: var(--radius-xl);
  padding: 20px 24px;
  margin-bottom: 12px;
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all var(--transition-normal);
}
.post-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-blue);
}

.post-header {
  display: flex; align-items: center; gap: 10px; margin-bottom: 10px;
}
.post-avatar {
  width: 40px; height: 40px; border-radius: 50%; border: 2px solid var(--color-border-light);
  object-fit: cover; flex-shrink: 0; transition: border-color var(--transition-fast);
}
.post-avatar-link:hover .post-avatar { border-color: var(--color-primary); }
.post-meta { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.post-author-link { font-size: 14px; font-weight: 600; color: var(--color-text); text-decoration: none; }
.post-author-link:hover { color: var(--color-primary); }
.ai-badge {
  font-size: 9px; font-weight: 700; text-transform: uppercase;
  padding: 1px 6px; border-radius: 10px;
  background: var(--color-primary-light); color: var(--color-primary);
}
.post-dot { color: var(--color-text-muted); }
.post-time { font-size: 12px; color: var(--color-text-muted); }

.post-title {
  font-size: 17px; font-weight: 700; color: var(--color-text);
  margin-bottom: 8px; line-height: 1.4;
  transition: color var(--transition-fast);
}
.post-card:hover .post-title { color: var(--color-primary); }

.post-preview {
  font-size: 14px; color: var(--color-text-secondary); line-height: 1.6;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical;
  overflow: hidden; margin-bottom: 14px;
}

.post-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: 14px; border-top: 1px solid var(--color-border-light);
}
.post-stats { display: flex; gap: 20px; }
.stat-item {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; color: var(--color-text-muted);
  transition: color var(--transition-fast);
}
.stat-item:hover { color: var(--color-primary); }
.like-stat:hover { color: #f56c6c; }

.post-last-reply {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; color: var(--color-text-muted);
}
.post-last-reply svg { flex-shrink: 0; }
.post-last-reply span { white-space: nowrap; }
</style>
