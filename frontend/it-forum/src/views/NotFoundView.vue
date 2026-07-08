<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="not-found">
    <div class="nf-content">
      <span class="nf-code">404</span>
      <h2 class="nf-title">页面不存在</h2>
      <p class="nf-desc">你访问的页面可能已被删除或移动</p>
      <button class="nf-btn" @click="router.push('/')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
        返回首页
      </button>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  display: flex; align-items: center; justify-content: center;
  min-height: 60vh; text-align: center;
}
.nf-code {
  font-size: 100px; font-weight: 800;
  background: linear-gradient(135deg, var(--color-primary), var(--color-indigo));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text; line-height: 1.1;
}
.nf-title { font-size: 20px; font-weight: 700; color: var(--color-text); margin: 8px 0 6px; }
.nf-desc { font-size: 14px; color: var(--color-text-secondary); margin-bottom: 24px; }
.nf-btn {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 10px 28px; border: none; border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary), var(--color-indigo));
  color: #fff; font-size: 14px; font-weight: 600; font-family: var(--font-family);
  cursor: pointer; transition: all var(--transition-fast);
  box-shadow: 0 4px 14px rgba(64,158,255,0.3);
}
.nf-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(64,158,255,0.4); }
</style>
