<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()

const form = ref({ email: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.value.email.trim()) return ElMessage.warning('请输入邮箱')
  if (!form.value.password) return ElMessage.warning('请输入密码')
  loading.value = true
  try {
    await auth.login(form.value.email, form.value.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    /* handled by interceptor */
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-bg" />
    <div class="auth-card glass-card">
      <div class="auth-logo">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="url(#grad)" stroke-width="1.8">
          <rect x="2" y="3" width="20" height="14" rx="2" />
          <path d="M8 21h8" /><path d="M12 17v4" />
          <line x1="6" y1="8" x2="6.01" y2="8" /><line x1="10" y1="8" x2="10.01" y2="8" />
          <defs><linearGradient id="grad" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#409eff"/><stop offset="100%" stop-color="#6366f1"/></linearGradient></defs>
        </svg>
        <span class="auth-title gradient-text">ITForum</span>
      </div>
      <p class="auth-subtitle">智能化技术论坛</p>

      <form class="auth-form" @submit.prevent="handleLogin">
        <div class="input-wrap">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
          <input v-model="form.email" type="email" class="auth-input" placeholder="请输入邮箱" autocomplete="email" />
        </div>
        <div class="input-wrap">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
          <input v-model="form.password" type="password" class="auth-input" placeholder="请输入密码" autocomplete="current-password" @keyup.enter="handleLogin" />
        </div>
        <button type="submit" class="auth-submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="auth-footer">
        <router-link to="/register">还没有账号？立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex; justify-content: center; align-items: center;
  min-height: 100vh; position: relative; overflow: hidden;
}
.auth-bg {
  position: fixed; inset: 0; z-index: -1;
  background: linear-gradient(135deg, #eef2ff 0%, #e0f2fe 30%, #f0f5ff 60%, #e8f4fd 100%);
}
.auth-bg::before {
  content: '';
  position: absolute; top: -40%; right: -20%; width: 60%; height: 80%;
  background: radial-gradient(circle, rgba(99,102,241,0.08) 0%, transparent 70%);
  border-radius: 50%;
}
.auth-bg::after {
  content: '';
  position: absolute; bottom: -30%; left: -15%; width: 50%; height: 70%;
  background: radial-gradient(circle, rgba(64,158,255,0.07) 0%, transparent 70%);
  border-radius: 50%;
}

.auth-card { width: 400px; padding: 40px 36px 32px; text-align: center; }

.auth-logo {
  display: flex; align-items: center; justify-content: center; gap: 10px;
  margin-bottom: 4px;
}
.auth-title { font-size: 26px; font-weight: 800; }
.auth-subtitle { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 32px; }

.auth-form { display: flex; flex-direction: column; gap: 16px; }

.input-wrap {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 16px; border: 1px solid var(--color-border);
  border-radius: var(--radius-md); background: var(--color-bg);
  color: var(--color-text-secondary);
  transition: border-color var(--transition-fast);
}
.input-wrap:focus-within { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(64,158,255,0.1); }

.auth-input {
  flex: 1; border: none; outline: none; background: none;
  font-size: 14px; font-family: var(--font-family); color: var(--color-text);
}
.auth-input::placeholder { color: var(--color-text-muted); }
.auth-input:-webkit-autofill { -webkit-box-shadow: 0 0 0 30px #fff inset; }

.auth-submit {
  margin-top: 4px; padding: 12px; border: none;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary), var(--color-indigo));
  color: #fff; font-size: 15px; font-weight: 700; font-family: var(--font-family);
  cursor: pointer; transition: all var(--transition-fast);
  box-shadow: 0 4px 14px rgba(64,158,255,0.3);
}
.auth-submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(64,158,255,0.4); }
.auth-submit:disabled { opacity: 0.6; cursor: not-allowed; }

.auth-footer { margin-top: 24px; font-size: 13px; }
.auth-footer a { color: var(--color-primary); font-weight: 600; text-decoration: none; }
.auth-footer a:hover { text-decoration: underline; }
</style>
