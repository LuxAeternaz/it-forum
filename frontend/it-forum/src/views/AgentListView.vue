<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getAgents, getMyAgents } from '../api/agent'

const router = useRouter()
const auth = useAuthStore()

const agents = ref([])
const myAgents = ref([])
const activeTab = ref('all')
const loading = ref(false)

const displayedAgents = computed(() => activeTab.value === 'all' ? agents.value : myAgents.value)

async function fetchAgents() {
  loading.value = true
  try {
    const [all, mine] = await Promise.all([
      getAgents(),
      auth.isLoggedIn ? getMyAgents() : Promise.resolve([])
    ])
    agents.value = all
    myAgents.value = mine
  } catch {}
  loading.value = false
}

function goDetail(id) {
  router.push(`/agents/${id}`)
}

function goCreate() {
  router.push('/agents/create')
}

onMounted(fetchAgents)
</script>

<template>
  <div class="agents-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">AI 智能体广场</h1>
        <p class="page-subtitle">浏览所有用户创建的 AI 智能体，在评论中 @智能体名称 即可召唤</p>
      </div>
      <button v-if="auth.isLoggedIn" class="btn-create" @click="goCreate">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        创建智能体
      </button>
    </div>

    <div class="tabs">
      <button :class="['tab', { active: activeTab === 'all' }]" @click="activeTab = 'all'">
        全部智能体
      </button>
      <button v-if="auth.isLoggedIn" :class="['tab', { active: activeTab === 'my' }]" @click="activeTab = 'my'">
        我的智能体
      </button>
    </div>

    <el-skeleton :loading="loading" animated :count="6">
      <div v-if="displayedAgents.length" class="agents-grid">
        <div
          v-for="agent in displayedAgents"
          :key="agent.id"
          class="agent-card glass-card"
          @click="goDetail(agent.id)"
        >
          <div class="agent-card-header">
            <img
              :src="agent.avatarUrl || '/default-bot-avatar.svg'"
              :alt="agent.name"
              class="agent-avatar"
            />
            <div class="agent-name-row">
              <span class="agent-name">@{{ agent.name }}</span>
              <span v-if="agent.status === 'ACTIVE'" class="status-dot active" />
            </div>
          </div>
          <p class="agent-desc">{{ agent.description || '暂无描述' }}</p>
          <div class="agent-card-footer">
            <router-link :to="`/user/${agent.creatorUserId}`" class="agent-creator" @click.stop>
              创建者: {{ agent.creatorUserId === '1' ? '官方' : (agent.creatorUsername || `用户${agent.creatorUserId}`) }}
            </router-link>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3"><path d="M12 2a3 3 0 00-3 3v2a3 3 0 006 0V5a3 3 0 00-3-3z"/><path d="M2 12h2a8 8 0 0114 0h2"/><circle cx="12" cy="18" r="4"/></svg>
        <span>{{ activeTab === 'my' ? '你还没有创建智能体' : '暂无智能体' }}</span>
      </div>
    </el-skeleton>
  </div>
</template>

<style scoped>
.agents-page { max-width: 900px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 24px; gap: 16px; }
.page-title { font-size: 24px; font-weight: 700; color: var(--color-text); margin: 0 0 6px; }
.page-subtitle { font-size: 14px; color: var(--color-text-muted); margin: 0; }

.btn-create {
  display: flex; align-items: center; gap: 6px; flex-shrink: 0;
  padding: 9px 20px; border: none; border-radius: var(--radius-full);
  background: var(--color-primary); color: #fff;
  font-size: 13px; font-weight: 600; font-family: var(--font-family);
  cursor: pointer; transition: all var(--transition-fast);
}
.btn-create:hover { background: var(--color-primary-dark); transform: translateY(-1px); box-shadow: var(--shadow-blue); }

.tabs { display: flex; gap: 4px; margin-bottom: 20px; border-bottom: 1px solid var(--color-border-light); padding-bottom: 0; }
.tab {
  padding: 10px 20px; border: none; background: none;
  font-size: 14px; font-weight: 500; color: var(--color-text-secondary);
  font-family: var(--font-family); cursor: pointer;
  border-bottom: 2px solid transparent; margin-bottom: -1px;
  transition: all var(--transition-fast);
}
.tab:hover { color: var(--color-primary); }
.tab.active { color: var(--color-primary); font-weight: 600; border-bottom-color: var(--color-primary); }

.agents-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
@media (max-width: 640px) { .agents-grid { grid-template-columns: 1fr; } }

.agent-card {
  padding: 20px; border-radius: var(--radius-lg); cursor: pointer;
  border: 1px solid var(--color-border-light);
  transition: all var(--transition-fast);
}
.agent-card:hover { border-color: var(--color-primary); transform: translateY(-2px); box-shadow: var(--shadow-blue); }

.agent-card-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.agent-avatar { width: 40px; height: 40px; border-radius: 50%; background: var(--color-bg-slate); border: 1px solid var(--color-border-light); }
.agent-name-row { display: flex; align-items: center; gap: 8px; }
.agent-name { font-size: 16px; font-weight: 700; color: var(--color-text); }
.status-dot { width: 8px; height: 8px; border-radius: 50%; }
.status-dot.active { background: #4ade80; }

.agent-desc { font-size: 13px; color: var(--color-text-secondary); line-height: 1.5; margin: 0 0 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.agent-creator { font-size: 12px; color: var(--color-text-muted); text-decoration: none; }
.agent-creator:hover { color: var(--color-primary); }

.empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 60px 0; color: var(--color-text-muted); font-size: 14px; }
</style>
