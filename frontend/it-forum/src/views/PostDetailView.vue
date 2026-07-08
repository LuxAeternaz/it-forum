<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPostDetail, deletePost } from '../api/post'
import { getComments, createComment } from '../api/comment'
import { like, unlike, getLikeStatus } from '../api/like'
import { bookmark, unbookmark, checkBookmarked } from '../api/bookmark'
import { useAuthStore } from '../stores/auth'
import { relativeTime } from '../utils/time'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommentItem from '../components/comment/CommentItem.vue'
import { useMentions, getSuggestions, renderMentionHtml, addToNameMap } from '../composables/useMentions'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const post = ref(null)
const comments = ref([])
const liked = ref(false)
const bookmarked = ref(false)
const replyContent = ref('')
const replyTo = ref(null)
const submitting = ref(false)
const { agents, renderSegments } = useMentions()

// @mention autocomplete
const mentionQuery = ref('')
const mentionSuggestions = ref([])
const mentionIndex = ref(0)
const showMentions = ref(false)
const mentionStartPos = ref(-1)

async function fetchPost() {
  post.value = await getPostDetail(route.params.id)
  if (auth.isLoggedIn) {
    try {
      const ids = await getLikeStatus('POST', [post.value.id])
      liked.value = ids.includes(post.value.id)
      bookmarked.value = await checkBookmarked(post.value.id)
    } catch {}
  }
}

async function fetchComments() {
  comments.value = await getComments(route.params.id)
  for (const c of comments.value) {
    if (c.username) {
      addToNameMap(c.username, c.userId, 'user')
    }
  }
}

async function handleLike() {
  if (!auth.isLoggedIn) return ElMessage.warning('请先登录')
  if (liked.value) {
    await unlike('POST', post.value.id)
    post.value.likeCount--
  } else {
    await like('POST', post.value.id)
    post.value.likeCount++
  }
  liked.value = !liked.value
}

async function handleBookmark() {
  if (!auth.isLoggedIn) return ElMessage.warning('请先登录')
  if (bookmarked.value) {
    await unbookmark(post.value.id)
  } else {
    await bookmark(post.value.id)
  }
  bookmarked.value = !bookmarked.value
  ElMessage.success(bookmarked.value ? '已收藏' : '已取消收藏')
}

async function handleReply() {
  if (!auth.isLoggedIn) return ElMessage.warning('请先登录')
  if (!replyContent.value.trim()) return ElMessage.warning('请输入评论内容')
  const data = { content: replyContent.value }
  if (replyTo.value) {
    data.parentId = replyTo.value.id
    data.replyToUserId = replyTo.value.userId
  }
  submitting.value = true
  try {
    await createComment(post.value.id, data)
    ElMessage.success('评论成功')
    replyContent.value = ''
    replyTo.value = null
    fetchComments()
  } catch { /* handled by interceptor */ }
  finally { submitting.value = false }
}

function handleReplyTo(comment) {
  replyTo.value = comment
  const name = comment.username || ('用户' + comment.userId)
  if (comment.username) {
    addToNameMap(comment.username, comment.userId, 'user')
  }
  replyContent.value = `@${name} `
}

// @mention autocomplete
let mentionTimer = null
watch(replyContent, (val) => {
  clearTimeout(mentionTimer)
  mentionTimer = setTimeout(() => updateMentions(val), 150)
})

function updateMentions(val) {
  const textarea = document.querySelector('.reply-textarea')
  if (!textarea) return
  const pos = textarea.selectionStart
  const beforeCursor = val.slice(0, pos)
  const atMatch = beforeCursor.match(/@([^\s@]*)$/)
  if (atMatch) {
    mentionQuery.value = atMatch[1]
    mentionStartPos.value = pos - atMatch[1].length - 1
    mentionIndex.value = 0
    const q = atMatch[1]
    if (q.length >= 0) {
      getSuggestions(q).then(list => {
        mentionSuggestions.value = list || []
        showMentions.value = (list && list.length > 0)
      }).catch(() => {
        mentionSuggestions.value = []
        showMentions.value = false
      })
    } else {
      mentionSuggestions.value = []
      showMentions.value = false
    }
  } else {
    showMentions.value = false
    mentionSuggestions.value = []
  }
}

function selectMention(item) {
  const before = replyContent.value.slice(0, mentionStartPos.value)
  const after = replyContent.value.slice(mentionStartPos.value + 1 + mentionQuery.value.length)
  replyContent.value = before + '@' + item.name + ' ' + after
  showMentions.value = false
  mentionSuggestions.value = []
  nextTick(() => {
    const textarea = document.querySelector('.reply-textarea')
    if (textarea) {
      const newPos = mentionStartPos.value + item.name.length + 2
      textarea.setSelectionRange(newPos, newPos)
      textarea.focus()
    }
  })
}

function handleMentionKeydown(e) {
  if (!showMentions.value || mentionSuggestions.value.length === 0) return
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    mentionIndex.value = (mentionIndex.value + 1) % mentionSuggestions.value.length
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    mentionIndex.value = (mentionIndex.value - 1 + mentionSuggestions.value.length) % mentionSuggestions.value.length
  } else if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    selectMention(mentionSuggestions.value[mentionIndex.value])
  } else if (e.key === 'Escape') {
    showMentions.value = false
    mentionSuggestions.value = []
  }
}

function hideMentions() {
  setTimeout(() => {
    showMentions.value = false
    mentionSuggestions.value = []
  }, 200)
}

async function handleDeletePost() {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？删除后不可恢复。', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePost(post.value.id)
    ElMessage.success('帖子已删除')
    router.push('/')
  } catch {}
}

const postAvatarSrc = computed(() => {
  if (post.value?.userId === '1') return '/default-bot-avatar.svg'
  if (post.value?.avatarUrl) return post.value.avatarUrl
  return '/default-avatar.svg'
})
const postAuthorName = computed(() => {
  if (post.value?.userId === '1') return 'IT小助手'
  return post.value?.username || `用户${post.value?.userId || ''}`
})

const renderedPostHtml = computed(() => {
  const html = post.value?.contentHtml || post.value?.content || ''
  return renderMentionHtml(html)
})

onMounted(async () => {
  await fetchPost()
  fetchComments()
})
</script>

<template>
  <div v-if="post" class="detail-page">
    <!-- Post Card -->
    <article class="card post-card">
      <header class="post-header">
        <div class="post-author">
          <router-link :to="`/user/${post.userId}`">
            <img
              :src="postAvatarSrc"
              :alt="postAuthorName"
              class="author-avatar"
            />
          </router-link>
          <div class="author-info">
            <router-link :to="`/user/${post.userId}`" class="author-name-link">{{ postAuthorName }}</router-link>
            <span class="post-time">{{ relativeTime(post.createdAt) }}</span>
          </div>
        </div>
        <div class="post-stats">
          <span class="stat-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            {{ post.viewCount }}
          </span>
          <span class="stat-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
            {{ post.commentCount }}
          </span>
        </div>
      </header>

      <h1 class="post-title">{{ post.title }}</h1>

      <div v-html="renderedPostHtml" class="post-content" />

      <footer class="post-actions">
        <div class="actions-left">
          <button :class="['action-btn', { active: liked }]" @click="handleLike">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
            {{ liked ? '已赞' : '点赞' }} {{ post.likeCount || 0 }}
          </button>
          <button :class="['action-btn', { active: bookmarked }]" @click="handleBookmark">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z"/></svg>
            {{ bookmarked ? '已收藏' : '收藏' }}
          </button>
        </div>
        <div class="actions-right">
          <router-link v-if="auth.userId === post.userId" :to="`/post/${post.id}/edit`" class="edit-link">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            编辑
          </router-link>
          <button v-if="auth.userId === post.userId" class="delete-link" @click="handleDeletePost">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
            删除
          </button>
        </div>
      </footer>
    </article>

    <!-- Comments Section -->
    <section class="card comments-card">
      <h3 class="comments-title">评论 ({{ comments.length }})</h3>

      <div v-if="comments.length" class="comments-list">
        <CommentItem
          v-for="c in comments.filter(c => !c.parentId)"
          :key="c.id"
          :comment="c"
          :all-comments="comments"
          :on-reply="handleReplyTo"
        />
      </div>
      <div v-else class="empty-comments">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
        <span>暂无评论，来抢沙发吧</span>
      </div>

      <!-- Reply Form -->
      <div v-if="auth.isLoggedIn" class="reply-form">
        <div v-if="replyTo" class="reply-to">
          回复 @{{ replyTo.username || '用户'+replyTo.userId }}
          <button class="cancel-reply" @click="replyTo = null; replyContent = ''">取消</button>
        </div>
        <div class="mentions-wrap">
          <textarea
            ref="replyTextarea"
            v-model="replyContent"
            class="reply-textarea"
            rows="3"
            placeholder="写下你的评论... 输入 @ 可以提及智能体或互关用户"
            maxlength="5000"
            @keydown="handleMentionKeydown"
            @blur="hideMentions"
          />
          <ul v-if="showMentions && mentionSuggestions.length" class="mentions-dropdown">
            <li
              v-for="(item, i) in mentionSuggestions"
              :key="item.id"
              :class="['mentions-item', { active: i === mentionIndex }]"
              @mousedown.prevent="selectMention(item)"
            >
              <img :src="item.avatarUrl || (item.type === 'agent' ? '/default-bot-avatar.svg' : '/default-avatar.svg')" class="mention-avatar" />
              <span class="mention-name">{{ item.name }}</span>
              <span class="mention-type">{{ item.type === 'agent' ? '智能体' : '用户' }}</span>
            </li>
          </ul>
        </div>
        <button class="reply-submit" :disabled="submitting" @click="handleReply">
          {{ submitting ? '发表中...' : '发表评论' }}
        </button>
      </div>
      <div v-else class="login-tip">
        <router-link to="/login">登录</router-link> 后参与评论
      </div>
    </section>
  </div>
  <div v-else class="skeleton-wrap">
    <div class="card skeleton-header"><el-skeleton :rows="4" animated /></div>
  </div>
</template>

<style scoped>
.detail-page { max-width: 860px; }

/* Post Card */
.post-card { padding: 28px; margin-bottom: 16px; }

.post-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px;
}
.post-author { display: flex; align-items: center; gap: 12px; }
.author-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  background: var(--color-bg-slate); border: 2px solid var(--color-border-light);
  transition: border-color var(--transition-fast);
}
a:hover .author-avatar { border-color: var(--color-primary); }
.author-name-link { font-size: 14px; font-weight: 600; color: var(--color-text); text-decoration: none; }
.author-name-link:hover { color: var(--color-primary); }
.post-time { font-size: 12px; color: var(--color-text-muted); display: block; margin-top: 2px; }
.author-info { display: flex; flex-direction: column; }
.post-stats { display: flex; gap: 16px; }
.stat-item {
  display: flex; align-items: center; gap: 4px;
  font-size: 13px; color: var(--color-text-secondary);
}

.post-title { font-size: 24px; font-weight: 700; color: var(--color-text); margin-bottom: 20px; line-height: 1.4; }

.post-content {
  line-height: 1.9; font-size: 15px; color: var(--color-text);
  padding-bottom: 24px; border-bottom: 1px solid var(--color-border-light);
}

/* Markdown rendered HTML styles */
:deep(.post-content p) { margin: 0 0 1em; }
:deep(.post-content p:last-child) { margin-bottom: 0; }
:deep(.post-content h1),
:deep(.post-content h2),
:deep(.post-content h3),
:deep(.post-content h4),
:deep(.post-content h5),
:deep(.post-content h6) {
  margin: 1.4em 0 0.6em; font-weight: 700; line-height: 1.4; color: var(--color-text);
}
:deep(.post-content h1) { font-size: 1.5em; }
:deep(.post-content h2) { font-size: 1.35em; border-bottom: 1px solid var(--color-border-light); padding-bottom: 0.3em; }
:deep(.post-content h3) { font-size: 1.2em; }
:deep(.post-content h4) { font-size: 1.1em; }
:deep(.post-content ul),
:deep(.post-content ol) { margin: 0 0 1em; padding-left: 1.8em; }
:deep(.post-content li) { margin-bottom: 0.3em; }
:deep(.post-content code) {
  font-family: 'Cascadia Code', Consolas, monospace; font-size: 0.88em;
  background: var(--color-bg-slate); padding: 2px 6px; border-radius: 4px; color: #e83e8c;
}
:deep(.post-content pre) {
  margin: 0 0 1.2em; padding: 16px; border-radius: var(--radius-md);
  background: #1e293b; color: #e2e8f0; overflow-x: auto; font-size: 13px; line-height: 1.65;
}
:deep(.post-content pre code) {
  background: none; padding: 0; color: inherit; font-size: inherit; border-radius: 0;
}
:deep(.post-content blockquote) {
  margin: 0 0 1em; padding: 8px 16px; border-left: 4px solid var(--color-primary);
  background: var(--color-bg-slate); border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  color: var(--color-text-secondary);
}
:deep(.post-content blockquote p:last-child) { margin-bottom: 0; }
:deep(.post-content table) {
  width: 100%; border-collapse: collapse; margin: 0 0 1.2em; font-size: 14px;
}
:deep(.post-content th),
:deep(.post-content td) {
  border: 1px solid var(--color-border); padding: 8px 12px; text-align: left;
}
:deep(.post-content th) {
  background: var(--color-bg-slate); font-weight: 600;
}
:deep(.post-content img) { max-width: 100%; border-radius: var(--radius-md); }
:deep(.post-content a) { color: var(--color-primary); text-decoration: none; }
:deep(.post-content a:hover) { text-decoration: underline; }
:deep(.post-content hr) {
  margin: 1.5em 0; border: none; height: 1px; background: var(--color-border-light);
}
:deep(.post-content strong) { font-weight: 700; }
:deep(.post-content em) { font-style: italic; }

.post-actions {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: 16px;
}
.actions-left, .actions-right { display: flex; gap: 8px; }

.action-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; border: 1px solid var(--color-border);
  border-radius: var(--radius-full); background: transparent;
  color: var(--color-text-secondary); font-size: 13px; font-weight: 500;
  font-family: var(--font-family); cursor: pointer;
  transition: all var(--transition-fast);
}
.action-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.action-btn.active { border-color: var(--color-primary); color: var(--color-primary); background: var(--color-primary-light); }

.edit-link {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 8px 16px; border-radius: var(--radius-full);
  color: var(--color-text-secondary); font-size: 13px;
  transition: all var(--transition-fast);
}
.edit-link:hover { color: var(--color-primary); background: var(--color-bg-slate); }
.delete-link {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 8px 16px; border: 1px solid #fde2e2; border-radius: var(--radius-full);
  background: transparent; color: #f56c6c;
  font-size: 13px; font-weight: 500; font-family: var(--font-family);
  cursor: pointer; transition: all var(--transition-fast);
}
.delete-link:hover { background: #fef0f0; border-color: #f56c6c; }

/* Comments */
.comments-card { padding: 24px; }
.comments-title { font-size: 16px; font-weight: 700; color: var(--color-text); margin-bottom: 20px; }
.comments-list { margin-bottom: 8px; }

.empty-comments {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 40px 0; color: var(--color-text-muted); font-size: 14px;
}

/* Reply Form */
.reply-form { margin-top: 20px; padding-top: 20px; border-top: 1px solid var(--color-border-light); }
.reply-to {
  font-size: 13px; color: var(--color-text-secondary); margin-bottom: 8px;
  display: flex; align-items: center; gap: 8px;
}
.cancel-reply {
  border: none; background: none; color: #f56c6c; cursor: pointer;
  font-size: 12px; font-family: var(--font-family);
}
.reply-textarea {
  width: 100%; padding: 12px; border: 1px solid var(--color-border);
  border-radius: var(--radius-md); resize: vertical;
  font-size: 14px; font-family: var(--font-family);
  color: var(--color-text); outline: none;
  transition: border-color var(--transition-fast);
}
.reply-textarea:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(64,158,255,0.1); }
.reply-submit {
  margin-top: 10px; padding: 8px 24px; border: none;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary), var(--color-indigo));
  color: #fff; font-size: 13px; font-weight: 600; font-family: var(--font-family);
  cursor: pointer; transition: all var(--transition-fast);
}
.reply-submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 14px rgba(64,158,255,0.3); }
.reply-submit:disabled { opacity: 0.6; cursor: not-allowed; }

.login-tip { margin-top: 24px; text-align: center; font-size: 14px; color: var(--color-text-secondary); }
.login-tip a { color: var(--color-primary); font-weight: 600; }

/* Mentions */
:deep(.mention-link) { color: var(--color-primary); font-weight: 600; text-decoration: none; }
:deep(.mention-link:hover) { text-decoration: underline; }

.mentions-wrap { position: relative; }
.mentions-dropdown {
  position: absolute; left: 0; right: 0; bottom: 100%;
  margin-bottom: 4px; background: var(--color-bg-card);
  border: 1px solid var(--color-border); border-radius: var(--radius-md);
  box-shadow: 0 -4px 12px rgba(0,0,0,0.1); max-height: 200px; overflow-y: auto;
  list-style: none; padding: 4px 0; z-index: 100;
}
.mentions-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 14px; cursor: pointer; font-size: 13px;
  transition: background var(--transition-fast);
}
.mentions-item:hover, .mentions-item.active { background: var(--color-bg-slate); }
.mention-avatar { width: 24px; height: 24px; border-radius: 50%; flex-shrink: 0; }
.mention-name { font-weight: 600; color: var(--color-text); }
.mention-type {
  margin-left: auto; font-size: 11px; color: var(--color-text-muted);
  background: var(--color-bg-slate); padding: 1px 6px; border-radius: var(--radius-sm);
}

.skeleton-wrap { max-width: 860px; }
.skeleton-header { padding: 28px; }
</style>
