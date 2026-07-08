<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getUserProfile, getUserPosts } from '../api/user'
import { follow, unfollow, checkFollow } from '../api/follow'
import { useAuthStore } from '../stores/auth'
import { relativeTime } from '../utils/time'
import { ElMessage } from 'element-plus'

const route = useRoute()
const auth = useAuthStore()
const profile = ref(null)
const posts = ref([])
const following = ref(false)
const tab = ref('posts')

const isSelf = computed(() => auth.userId === profile.value?.id)

const avatarSrc = computed(() => {
  if (profile.value?.avatarUrl) return profile.value.avatarUrl
  return profile.value?.id === '1' ? '/default-bot-avatar.svg' : '/default-avatar.svg'
})

onMounted(async () => {
  profile.value = await getUserProfile(route.params.id)
  posts.value = (await getUserPosts(route.params.id)).list
  if (auth.isLoggedIn && route.params.id !== auth.userId) {
    try { following.value = await checkFollow(route.params.id, 'USER') } catch {}
  }
})

async function handleFollow() {
  if (!auth.isLoggedIn) return ElMessage.warning('请先登录')
  if (following.value) {
    await unfollow(route.params.id, 'USER')
  } else {
    await follow(route.params.id, 'USER')
  }
  following.value = !following.value
}
</script>

<template>
  <div v-if="profile" class="profile-page">
    <div class="card profile-card">
      <img :src="avatarSrc" :alt="profile.username" class="profile-avatar" />
      <h2 class="profile-name">{{ profile.username }}</h2>
      <p class="profile-bio">{{ profile.bio || '这个人很懒，什么都没写...' }}</p>
      <div class="profile-stats">
        <div class="stat"><span class="stat-num">{{ profile.postCount || 0 }}</span><span class="stat-label">帖子</span></div>
        <div class="stat"><span class="stat-num">{{ profile.commentCount || 0 }}</span><span class="stat-label">回复</span></div>
        <div class="stat"><span class="stat-num">{{ profile.likeReceivedCount || 0 }}</span><span class="stat-label">获赞</span></div>
        <div class="stat"><span class="stat-num">{{ profile.followerCount || 0 }}</span><span class="stat-label">粉丝</span></div>
        <div class="stat"><span class="stat-num">{{ profile.followingCount || 0 }}</span><span class="stat-label">关注</span></div>
      </div>
      <div v-if="!isSelf" class="profile-actions">
        <button :class="['follow-btn', { following }]" @click="handleFollow">
          {{ following ? '已关注' : '+ 关注' }}
        </button>
      </div>
      <router-link v-if="isSelf" to="/profile" class="edit-profile-link">编辑资料</router-link>
    </div>

    <div class="card posts-card">
      <div class="tabs">
        <button :class="['tab', { active: tab === 'posts' }]" @click="tab = 'posts'">他的帖子</button>
      </div>
      <div v-if="posts.length" class="posts-list">
        <div v-for="p in posts" :key="p.id" class="post-row">
          <router-link :to="`/post/${p.id}`" class="post-link">{{ p.title }}</router-link>
          <span class="post-meta">{{ relativeTime(p.createdAt) }} · {{ p.viewCount }} 浏览 · {{ p.commentCount }} 评论</span>
        </div>
      </div>
      <div v-else class="empty-tab">
        <span>暂无帖子</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page { max-width: 860px; }

.profile-card {
  padding: 40px 24px 28px; text-align: center; margin-bottom: 16px;
}
.profile-avatar {
  width: 88px; height: 88px; border-radius: 50%;
  border: 4px solid var(--color-border-light); margin-bottom: 16px;
  background: var(--color-bg-slate);
}
.profile-name { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: 6px; }
.profile-bio { font-size: 14px; color: var(--color-text-secondary); max-width: 400px; margin: 0 auto 20px; line-height: 1.6; }

.profile-stats {
  display: flex; justify-content: center; gap: 32px;
  margin-bottom: 20px;
}
.stat { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.stat-num { font-size: 18px; font-weight: 700; color: var(--color-text); }
.stat-label { font-size: 12px; color: var(--color-text-muted); }

.follow-btn {
  padding: 8px 32px; border-radius: var(--radius-full);
  font-size: 14px; font-weight: 600; font-family: var(--font-family); cursor: pointer;
  border: 1px solid var(--color-primary); color: #fff;
  background: var(--color-primary); transition: all var(--transition-fast);
}
.follow-btn:hover { box-shadow: 0 4px 12px rgba(64,158,255,0.3); }
.follow-btn.following { background: transparent; color: var(--color-text-secondary); border-color: var(--color-border); }
.edit-profile-link {
  display: inline-block; margin-top: 12px;
  font-size: 13px; color: var(--color-primary); font-weight: 500;
}

.posts-card { padding: 24px; }
.tabs { display: flex; gap: 0; margin-bottom: 16px; border-bottom: 2px solid var(--color-border-light); }
.tab {
  padding: 10px 20px; border: none; background: none;
  font-size: 14px; font-weight: 600; font-family: var(--font-family);
  color: var(--color-text-secondary); cursor: pointer;
  border-bottom: 2px solid transparent; margin-bottom: -2px;
  transition: all var(--transition-fast);
}
.tab:hover { color: var(--color-primary); }
.tab.active { color: var(--color-primary); border-bottom-color: var(--color-primary); }

.post-row {
  padding: 12px 0; border-bottom: 1px solid var(--color-border-light);
}
.post-row:last-child { border-bottom: none; }
.post-link {
  font-size: 15px; font-weight: 500; color: var(--color-text);
  text-decoration: none; display: block; margin-bottom: 4px;
  transition: color var(--transition-fast);
}
.post-link:hover { color: var(--color-primary); }
.post-meta { font-size: 12px; color: var(--color-text-muted); }

.empty-tab { text-align: center; padding: 40px 0; font-size: 14px; color: var(--color-text-muted); }
</style>
