<script setup>
import { relativeTime } from '../../utils/time'

defineProps({
  comment: { type: Object, required: true }
})
</script>

<template>
  <div class="comment-search-card" @click="$router.push(`/post/${comment.postId}`)">
    <div class="cs-header">
      <img
        :src="comment.avatarUrl || '/default-avatar.svg'"
        class="cs-avatar"
        alt=""
      />
      <span class="cs-username">{{ comment.username || '用户' + comment.userId }}</span>
      <span class="cs-time">{{ relativeTime(comment.createdAt) }}</span>
    </div>
    <p class="cs-content">{{ comment.content?.substring(0, 300) }}</p>
    <div class="cs-context">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M4 11a9 9 0 0 1 9 9"/><path d="M4 4a16 16 0 0 1 16 16"/><circle cx="5" cy="19" r="1"/>
      </svg>
      在帖子 <strong>{{ comment.postTitle }}</strong> 中评论
    </div>
  </div>
</template>

<style scoped>
.comment-search-card {
  padding: 16px; margin-bottom: 8px;
  border: 1px solid var(--color-border-light); border-radius: var(--radius-md);
  background: var(--color-bg-card); cursor: pointer;
  transition: all var(--transition-fast);
}
.comment-search-card:hover { border-color: var(--color-primary); box-shadow: 0 2px 8px rgba(64,158,255,0.08); }
.cs-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.cs-avatar { width: 24px; height: 24px; border-radius: 50%; background: var(--color-bg-slate); }
.cs-username { font-size: 13px; font-weight: 600; color: var(--color-text); }
.cs-time { font-size: 12px; color: var(--color-text-muted); margin-left: auto; }
.cs-content {
  font-size: 14px; color: var(--color-text); line-height: 1.6;
  white-space: pre-wrap; word-break: break-word; margin-bottom: 8px;
}
.cs-context {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; color: var(--color-text-muted);
}
.cs-context strong { color: var(--color-primary); font-weight: 600; }
</style>
