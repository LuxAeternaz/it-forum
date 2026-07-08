<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { deleteComment } from '../../api/comment'
import { relativeTime } from '../../utils/time'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMentions } from '../../composables/useMentions'

const props = defineProps({
  comment: { type: Object, required: true },
  allComments: { type: Array, required: true },
  onReply: { type: Function, required: true }
})

const auth = useAuthStore()
const { renderSegments, agents } = useMentions()

const agentId = computed(() => {
  if (!isAI.value) return null
  const match = props.comment.content?.match(/^\*\*\[@([^\]]+)\]\*\*/)
  const name = match ? match[1] : 'IT小助手'
  const found = agents.value.find(a => a.name === name)
  return found ? found.id : null
})

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteComment(props.comment.postId, props.comment.id)
    ElMessage.success('评论已删除')
    window.location.reload()
  } catch {}
}

const children = computed(() =>
  props.allComments.filter(c => c.parentId === props.comment.id)
)

const isAI = computed(() => props.comment.userId === '1')

const agentName = computed(() => {
  if (!isAI.value) return null
  const match = props.comment.content?.match(/^\*\*\[@([^\]]+)\]\*\*/)
  return match ? match[1] : 'IT小助手'
})

const displayContent = computed(() => {
  if (!isAI.value) return props.comment.content
  return props.comment.content?.replace(/^\*\*\[@[^\]]+\]\*\*\s*/, '') || props.comment.content
})

const segments = computed(() => renderSegments(displayContent.value))

const displayName = computed(() => {
  if (isAI.value) return agentName.value
  return props.comment.username || `用户${props.comment.userId}`
})

const avatarSrc = computed(() => {
  if (isAI.value) return '/default-bot-avatar.svg'
  if (props.comment.avatarUrl) return props.comment.avatarUrl
  return '/default-avatar.svg'
})
</script>

<template>
  <div :class="['comment-item', { 'is-reply': comment.parentId }]">
    <div class="comment-body">
      <router-link :to="isAI && agentId ? `/agents/${agentId}` : `/user/${comment.userId}`">
        <img
          :src="avatarSrc"
          :alt="displayName"
          class="comment-avatar"
        />
      </router-link>
      <div class="comment-main">
        <div class="comment-meta">
          <router-link :to="isAI && agentId ? `/agents/${agentId}` : `/user/${comment.userId}`" class="comment-author-link">{{ displayName }}</router-link>
          <span v-if="isAI" class="ai-badge">AI</span>
          <span class="comment-time">{{ relativeTime(comment.createdAt) }}</span>
        </div>
        <div class="comment-content">
          <template v-for="(seg, i) in segments" :key="i">
            <router-link v-if="seg.type === 'mention'" :to="seg.link" class="mention-link">{{ seg.text }}</router-link>
            <span v-else>{{ seg.text }}</span>
          </template>
        </div>
        <div class="comment-actions">
          <button class="reply-btn" @click="onReply(comment)">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 17 4 12 9 7"/><path d="M20 18v-2a4 4 0 00-4-4H4"/></svg>
            回复
          </button>
          <button v-if="String(auth.userId) === String(comment.userId)" class="delete-btn" @click="handleDelete">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
            删除
          </button>
          <span class="comment-likes">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
            {{ comment.likeCount || 0 }}
          </span>
        </div>
      </div>
    </div>

    <!-- Child comments with left border indentation -->
    <div v-if="children.length" class="children-wrap">
      <CommentItem
        v-for="child in children"
        :key="child.id"
        :comment="child"
        :all-comments="allComments"
        :on-reply="onReply"
      />
    </div>
  </div>
</template>

<style scoped>
.comment-item {
  padding: 0;
  border-bottom: 1px solid var(--color-border-light);
}

.comment-item:last-child { border-bottom: none; }

.comment-item.is-reply {
  margin-left: 0;
  border-left: 3px solid var(--color-primary-light);
  border-bottom: none;
  padding-left: 16px;
  margin-top: 4px;
}

.comment-body {
  display: flex; gap: 10px; padding: 14px 0;
}

.comment-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--color-bg-slate); flex-shrink: 0;
  border: 1px solid var(--color-border-light);
  transition: border-color var(--transition-fast);
}
a:hover .comment-avatar { border-color: var(--color-primary); }

.comment-main { flex: 1; min-width: 0; }

.comment-meta {
  display: flex; align-items: center; gap: 8px; margin-bottom: 4px;
}

.comment-author-link { font-size: 13px; font-weight: 600; color: var(--color-text); text-decoration: none; }
.comment-author-link:hover { color: var(--color-primary); }

.ai-badge {
  display: inline-block; padding: 1px 6px;
  border-radius: var(--radius-sm); background: var(--color-primary);
  color: #fff; font-size: 10px; font-weight: 700; letter-spacing: 0.5px;
}

.comment-time { font-size: 12px; color: var(--color-text-muted); }

.comment-content {
  font-size: 14px; line-height: 1.65; color: var(--color-text);
  white-space: pre-wrap; word-break: break-word;
}

.comment-actions {
  display: flex; align-items: center; gap: 16px; margin-top: 6px;
}

.reply-btn {
  display: inline-flex; align-items: center; gap: 4px;
  border: none; background: none; color: var(--color-text-secondary);
  font-size: 12px; font-family: var(--font-family); cursor: pointer;
  padding: 2px 0; transition: color var(--transition-fast);
}
.reply-btn:hover { color: var(--color-primary); }

.delete-btn {
  display: inline-flex; align-items: center; gap: 4px;
  border: none; background: none; color: var(--color-text-muted);
  font-size: 12px; font-family: var(--font-family); cursor: pointer;
  padding: 2px 0; transition: color var(--transition-fast);
}
.delete-btn:hover { color: #f56c6c; }

.comment-likes {
  display: inline-flex; align-items: center; gap: 3px;
  font-size: 12px; color: var(--color-text-muted);
}

.children-wrap {
  padding-bottom: 8px;
}

.mention-link {
  color: var(--color-primary); font-weight: 600; text-decoration: none;
}
.mention-link:hover { text-decoration: underline; }
</style>
