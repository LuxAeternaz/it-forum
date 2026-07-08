<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { createPost } from '../api/post'
import { getCategories } from '../api/category'
import { polishText } from '../api/ai'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { useDraft } from '../composables/useDraft'
import TiptapEditor from '../components/editor/TiptapEditor.vue'

const router = useRouter()
const auth = useAuthStore()
const categories = ref([])
const form = ref({ title: '', content: '', categoryId: null })
const loading = ref(false)
const polishLoading = ref(false)
const polishResult = ref('')
const showPolish = ref(false)

const wordCount = computed(() => form.value.content.replace(/\s/g, '').length)
const titleLength = computed(() => form.value.title.length)

const { draftSaved, draftSaving, hasRestoredDraft, checkAndRestore, clearDraft } =
  useDraft(auth.userId, null, form)

onMounted(async () => {
  try { categories.value = await getCategories() } catch {}
  const restored = await checkAndRestore()
  if (restored) ElMessage.success('已恢复之前保存的草稿')
})

async function handleSubmit() {
  if (!form.value.title.trim()) return ElMessage.warning('请输入标题')
  if (!form.value.content.trim()) return ElMessage.warning('请输入内容')
  if (!form.value.categoryId) return ElMessage.warning('请选择版块')
  loading.value = true
  try {
    const res = await createPost(form.value)
    await clearDraft()
    ElMessage.success('发布成功')
    router.push(`/post/${res.id}`)
  } catch {
    ElMessage.error('发布失败')
  } finally {
    loading.value = false
  }
}

async function handlePolish(content) {
  polishLoading.value = true
  try {
    polishResult.value = await polishText(content, '')
    showPolish.value = true
  } catch {
    ElMessage.error('AI 润色失败，请稍后再试')
  } finally {
    polishLoading.value = false
  }
}

function acceptPolish() {
  form.value.content = polishResult.value
  showPolish.value = false
  ElMessage.success('已采用润色结果')
}
</script>

<template>
  <div class="create-page">
    <h2 class="page-title">发布新帖</h2>

    <div class="card form-card">
      <div class="form-row">
        <el-select
          v-model="form.categoryId"
          placeholder="请选择版块"
          size="large"
          class="category-select"
        >
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </div>

      <div class="form-row">
        <div class="title-wrap">
          <input
            v-model="form.title"
            type="text"
            class="title-input"
            placeholder="请输入标题..."
            maxlength="200"
          />
          <span class="title-count" :class="{ warn: titleLength > 180 }">{{ titleLength }}/200</span>
        </div>
      </div>

      <div class="form-row editor-row">
        <TiptapEditor
          v-model="form.content"
          placeholder="请输入内容...支持 Markdown 语法"
          @polish="handlePolish"
        />
      </div>

      <div class="form-actions">
        <div class="actions-left">
          <button
            class="polish-btn"
            :disabled="!form.content.trim() || polishLoading"
            @click="handlePolish(form.content)"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3l1.5 5.5L19 10l-5.5 1.5L12 17l-1.5-5.5L5 10l5.5-1.5z"/></svg>
            {{ polishLoading ? '润色中...' : 'AI 智能润色' }}
          </button>
        </div>
        <div class="actions-right">
          <span v-if="draftSaving" class="draft-status">保存中...</span>
          <span v-else-if="draftSaved" class="draft-status saved">草稿已保存</span>
          <span class="word-count">共 {{ wordCount }} 字</span>
          <button class="submit-btn" :disabled="loading" @click="handleSubmit">
            {{ loading ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>
    </div>

    <!-- AI Polish Dialog -->
    <el-dialog v-model="showPolish" title="AI 润色结果" width="760px" :close-on-click-modal="false">
      <div class="polish-compare">
        <div class="polish-panel">
          <div class="polish-panel-title">原文</div>
          <div class="polish-panel-content original">{{ form.content }}</div>
        </div>
        <div class="polish-arrow">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
        </div>
        <div class="polish-panel">
          <div class="polish-panel-title">润色后</div>
          <div class="polish-panel-content polished">{{ polishResult }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPolish = false">取消</el-button>
        <el-button type="primary" @click="acceptPolish">采用润色结果</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.create-page { max-width: 860px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: 20px; }

.form-card { padding: 24px; }
.form-row { margin-bottom: 18px; }
.form-row:last-child { margin-bottom: 0; }

.category-select { width: 200px; }

.title-wrap { position: relative; }
.title-input {
  width: 100%; padding: 12px 60px 12px 16px;
  border: 1px solid var(--color-border); border-radius: var(--radius-md);
  font-size: 18px; font-weight: 600; font-family: var(--font-family);
  color: var(--color-text); outline: none;
  transition: border-color var(--transition-fast);
}
.title-input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(64,158,255,0.1); }
.title-input::placeholder { color: var(--color-text-muted); font-weight: 400; }
.title-count {
  position: absolute; right: 14px; top: 50%; transform: translateY(-50%);
  font-size: 12px; color: var(--color-text-muted);
}
.title-count.warn { color: #f56c6c; }

.editor-row { margin-bottom: 0; }

.form-actions {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 18px; padding-top: 18px; border-top: 1px solid var(--color-border-light);
}
.actions-left, .actions-right { display: flex; align-items: center; gap: 12px; }

.polish-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; border: 1px solid var(--color-primary);
  border-radius: var(--radius-full); background: transparent;
  color: var(--color-primary); font-size: 13px; font-weight: 600;
  font-family: var(--font-family); cursor: pointer;
  transition: all var(--transition-fast);
}
.polish-btn:hover:not(:disabled) { background: var(--color-primary-light); }
.polish-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.draft-status { font-size: 12px; color: var(--color-text-muted); }
.draft-status.saved { color: var(--color-indigo); }
.word-count { font-size: 13px; color: var(--color-text-muted); }

.submit-btn {
  padding: 10px 28px; border: none; border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary), var(--color-indigo));
  color: #fff; font-size: 14px; font-weight: 700; font-family: var(--font-family);
  cursor: pointer; transition: all var(--transition-fast);
  box-shadow: 0 4px 14px rgba(64,158,255,0.3);
}
.submit-btn:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(64,158,255,0.4); }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

/* Polish dialog */
.polish-compare { display: flex; align-items: stretch; gap: 16px; }
.polish-arrow { display: flex; align-items: center; color: var(--color-primary); flex-shrink: 0; }
.polish-panel { flex: 1; min-width: 0; }
.polish-panel-title { font-size: 13px; font-weight: 700; color: var(--color-text-secondary); margin-bottom: 8px; }
.polish-panel-content {
  padding: 14px; border-radius: var(--radius-md);
  font-size: 14px; line-height: 1.7; white-space: pre-wrap;
  max-height: 380px; overflow-y: auto;
}
.polish-panel-content.original { background: var(--color-bg-slate); color: var(--color-text-secondary); }
.polish-panel-content.polished { background: #eef2ff; color: var(--color-text); border: 1px solid var(--color-primary-light); }
</style>
