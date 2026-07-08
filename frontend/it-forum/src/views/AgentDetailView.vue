<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getAgent, deleteAgent } from '../api/agent'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const agent = ref(null)
const loading = ref(true)

const isCreator = computed(() =>
  agent.value && auth.user && String(agent.value.creatorUserId) === String(auth.user.id)
)

onMounted(async () => {
  try {
    agent.value = await getAgent(route.params.id)
  } catch {
    ElMessage.error('智能体不存在')
    router.replace('/agents')
  }
  loading.value = false
})

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这个智能体吗？删除后不可恢复。', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAgent(agent.value.id)
    ElMessage.success('智能体已删除')
    router.push('/agents')
  } catch {}
}

function goEdit() {
  router.push(`/agents/${agent.value.id}/edit`)
}
</script>

<template>
  <div class="detail-page">
    <el-skeleton :loading="loading" animated :count="1">
      <template v-if="agent">
        <div class="page-header">
          <button class="btn-back" @click="router.push('/agents')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          </button>
          <span class="breadcrumb">智能体广场 / {{ agent.name }}</span>
        </div>

        <div class="agent-hero glass-card">
          <div class="hero-top">
            <img
              :src="agent.avatarUrl || '/default-bot-avatar.svg'"
              :alt="agent.name"
              class="hero-avatar"
            />
            <div>
              <h1 class="hero-name">@{{ agent.name }}</h1>
              <p class="hero-meta">
                <router-link :to="`/user/${agent.creatorUserId}`" class="creator-link">
                  创建者: {{ agent.creatorUserId === '1' ? '官方' : (agent.creatorUsername || `用户${agent.creatorUserId}`) }}
                </router-link>
                <span class="dot-sep">·</span>
                创建于 {{ new Date(agent.createdAt).toLocaleDateString('zh-CN') }}
              </p>
            </div>
            <div v-if="isCreator" class="hero-actions">
              <button class="btn-edit" @click="goEdit">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                编辑
              </button>
              <button class="btn-delete" @click="handleDelete">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
                删除
              </button>
            </div>
          </div>
          <p v-if="agent.description" class="hero-desc">{{ agent.description }}</p>
        </div>

        <div class="persona-card glass-card">
          <h2 class="card-title">人设 (Persona)</h2>
          <pre class="persona-text">{{ agent.persona }}</pre>
        </div>

        <div class="usage-card glass-card">
          <h2 class="card-title">使用方式</h2>
          <p>在任意帖子的评论区输入 <code class="code-tag">@{{ agent.name }}</code> + 你的问题，即可召唤该智能体进行回复。</p>
          <div class="usage-example">
            <span class="example-prompt">@{{ agent.name }} 请问Vue 3中computed和watch的区别是什么？</span>
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.detail-page { max-width: 800px; }
.page-header { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.btn-back {
  display: flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border: 1px solid var(--color-border);
  border-radius: 50%; background: #fff; color: var(--color-text-secondary);
  cursor: pointer; transition: all var(--transition-fast);
}
.btn-back:hover { border-color: var(--color-primary); color: var(--color-primary); }
.breadcrumb { font-size: 13px; color: var(--color-text-muted); }

.agent-hero { padding: 28px; border-radius: var(--radius-lg); border: 1px solid var(--color-border-light); margin-bottom: 16px; }
.hero-top { display: flex; align-items: flex-start; gap: 16px; }
.hero-avatar { width: 64px; height: 64px; border-radius: 50%; background: var(--color-bg-slate); border: 2px solid var(--color-border-light); flex-shrink: 0; }
.hero-name { font-size: 24px; font-weight: 700; color: var(--color-text); margin: 0 0 6px; }
.hero-meta { font-size: 13px; color: var(--color-text-muted); margin: 0; }
.creator-link { color: var(--color-text-muted); text-decoration: none; }
.creator-link:hover { color: var(--color-primary); }
.dot-sep { margin: 0 6px; }
.hero-actions { display: flex; gap: 8px; margin-left: auto; }
.btn-edit, .btn-delete {
  display: flex; align-items: center; gap: 4px; padding: 7px 14px;
  border-radius: var(--radius-full); font-size: 12px; font-weight: 600;
  font-family: var(--font-family); cursor: pointer; transition: all var(--transition-fast);
}
.btn-edit { border: 1px solid var(--color-border); background: #fff; color: var(--color-text-secondary); }
.btn-edit:hover { border-color: var(--color-primary); color: var(--color-primary); }
.btn-delete { border: 1px solid #fde2e2; background: #fff; color: #f56c6c; }
.btn-delete:hover { background: #fef0f0; border-color: #f56c6c; }
.hero-desc { font-size: 14px; color: var(--color-text-secondary); line-height: 1.6; margin: 14px 0 0; }

.persona-card, .usage-card {
  padding: 24px 28px; border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light); margin-bottom: 16px;
}
.card-title { font-size: 16px; font-weight: 700; color: var(--color-text); margin: 0 0 14px; }
.persona-text {
  font-size: 14px; color: var(--color-text); line-height: 1.7;
  white-space: pre-wrap; font-family: var(--font-family);
  background: var(--color-bg-slate); padding: 16px; border-radius: var(--radius-md);
  margin: 0;
}

.usage-card p { font-size: 14px; color: var(--color-text-secondary); line-height: 1.6; margin: 0 0 14px; }
.code-tag {
  display: inline-block; padding: 2px 8px;
  background: rgba(64,158,255,0.1); color: var(--color-primary);
  border-radius: var(--radius-sm); font-size: 13px; font-weight: 600;
  font-family: var(--font-family);
}
.usage-example {
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  padding: 14px 18px; border-radius: var(--radius-md);
}
.example-prompt { font-size: 14px; color: var(--color-indigo); font-weight: 500; }
</style>
