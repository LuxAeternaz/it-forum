<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getAgent, updateAgent } from '../api/agent'
import { uploadImage } from '../api/upload'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const form = ref({ name: '', persona: '', description: '' })
const avatarUrl = ref('')
const avatarUploading = ref(false)
const submitting = ref(false)
const loading = ref(true)
const errors = ref({})

async function handleAvatarUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  avatarUploading.value = true
  try {
    const res = await uploadImage(file)
    avatarUrl.value = res.filePath
    ElMessage.success('头像上传成功')
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

onMounted(async () => {
  try {
    const agent = await getAgent(route.params.id)
    if (String(agent.creatorUserId) !== String(auth.user?.id)) {
      ElMessage.error('无权编辑该智能体')
      router.replace('/agents')
      return
    }
    form.value.name = agent.name
    form.value.persona = agent.persona
    form.value.description = agent.description || ''
    avatarUrl.value = agent.avatarUrl || ''
  } catch {
    ElMessage.error('智能体不存在')
    router.replace('/agents')
  }
  loading.value = false
})

function validate() {
  errors.value = {}
  if (!form.value.name.trim()) {
    errors.value.name = '请输入智能体名称'
  } else if (!/^[\w一-鿿]+$/.test(form.value.name.trim())) {
    errors.value.name = '名称不能包含空格或特殊字符'
  }
  if (!form.value.persona.trim()) {
    errors.value.persona = '请输入智能体人设'
  }
  return Object.keys(errors.value).length === 0
}

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true
  try {
    await updateAgent(route.params.id, {
      name: form.value.name.trim(),
      persona: form.value.persona.trim(),
      description: form.value.description.trim(),
      avatarUrl: avatarUrl.value || undefined
    })
    ElMessage.success('智能体已更新')
    router.push(`/agents/${route.params.id}`)
  } catch (e) {
    ElMessage.error(e?.message || '更新失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="edit-page">
    <div class="page-header">
      <button class="btn-back" @click="router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <h1 class="page-title">编辑智能体</h1>
    </div>

    <el-skeleton :loading="loading" animated>
      <form class="agent-form glass-card" @submit.prevent="handleSubmit">
        <!-- Avatar Upload -->
        <div class="form-group">
          <label class="form-label">智能体头像</label>
          <div class="avatar-upload-area">
            <img
              :src="avatarUrl || '/default-bot-avatar.svg'"
              class="agent-avatar-preview"
              alt="头像预览"
            />
            <label class="upload-btn" :class="{ uploading: avatarUploading }">
              {{ avatarUploading ? '上传中...' : '更换头像' }}
              <input type="file" accept="image/*" class="file-input" @change="handleAvatarUpload" :disabled="avatarUploading" />
            </label>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">智能体名称 <span class="required">*</span></label>
          <div class="input-prefix">
            <span class="prefix">@</span>
            <input
              v-model="form.name"
              type="text"
              class="form-input"
              maxlength="50"
            />
          </div>
          <p v-if="errors.name" class="form-error">{{ errors.name }}</p>
          <p v-else class="form-hint">用户在评论中 @名称 即可召唤此智能体。</p>
        </div>

        <div class="form-group">
          <label class="form-label">人设 (Persona) <span class="required">*</span></label>
          <textarea
            v-model="form.persona"
            class="form-textarea"
            rows="8"
            maxlength="5000"
          />
          <p v-if="errors.persona" class="form-error">{{ errors.persona }}</p>
        </div>

        <div class="form-group">
          <label class="form-label">描述</label>
          <input
            v-model="form.description"
            type="text"
            class="form-input"
            placeholder="一句话介绍你的智能体（选填）"
            maxlength="500"
          />
        </div>

        <div class="form-actions">
          <button type="button" class="btn-cancel" @click="router.back()">取消</button>
          <button type="submit" class="btn-submit" :disabled="submitting">
            {{ submitting ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </form>
    </el-skeleton>
  </div>
</template>

<style scoped>
.edit-page { max-width: 720px; }
.page-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.btn-back { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; border: 1px solid var(--color-border); border-radius: 50%; background: #fff; color: var(--color-text-secondary); cursor: pointer; transition: all var(--transition-fast); }
.btn-back:hover { border-color: var(--color-primary); color: var(--color-primary); }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin: 0; }

.agent-form { padding: 28px; border-radius: var(--radius-lg); border: 1px solid var(--color-border-light); }
.form-group { margin-bottom: 22px; }

.avatar-upload-area { display: flex; align-items: center; gap: 16px; }
.agent-avatar-preview {
  width: 72px; height: 72px; border-radius: 50%;
  border: 2px solid var(--color-border-light);
  background: var(--color-bg-slate); object-fit: cover;
}
.upload-btn {
  display: inline-flex; align-items: center; padding: 8px 18px;
  border: 1px dashed var(--color-border); border-radius: var(--radius-full);
  font-size: 13px; color: var(--color-primary); cursor: pointer;
  font-weight: 500; transition: all var(--transition-fast);
  position: relative;
}
.upload-btn:hover { border-color: var(--color-primary); background: var(--color-primary-light); }
.upload-btn.uploading { opacity: 0.6; pointer-events: none; }
.file-input { display: none; }
.form-label { display: block; font-size: 14px; font-weight: 600; color: var(--color-text); margin-bottom: 8px; }
.required { color: #f56c6c; }
.input-prefix { display: flex; align-items: center; border: 1px solid var(--color-border); border-radius: var(--radius-md); overflow: hidden; transition: border-color var(--transition-fast); }
.input-prefix:focus-within { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(64,158,255,0.1); }
.prefix { padding: 0 12px; font-size: 15px; font-weight: 600; color: var(--color-primary); background: var(--color-bg-slate); line-height: 40px; }
.form-input { flex: 1; height: 40px; padding: 0 14px; border: none; outline: none; font-size: 14px; font-family: var(--font-family); color: var(--color-text); background: #fff; }
.form-textarea { width: 100%; padding: 12px 14px; border: 1px solid var(--color-border); border-radius: var(--radius-md); outline: none; resize: vertical; font-size: 14px; font-family: var(--font-family); color: var(--color-text); line-height: 1.6; min-height: 160px; transition: border-color var(--transition-fast); }
.form-textarea:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(64,158,255,0.1); }
.form-hint { font-size: 12px; color: var(--color-text-muted); margin: 6px 0 0; }
.form-error { font-size: 12px; color: #f56c6c; margin: 6px 0 0; }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; padding-top: 8px; }
.btn-cancel { padding: 10px 24px; border: 1px solid var(--color-border); border-radius: var(--radius-full); background: #fff; color: var(--color-text-secondary); font-size: 14px; font-weight: 500; font-family: var(--font-family); cursor: pointer; transition: all var(--transition-fast); }
.btn-cancel:hover { border-color: var(--color-primary); color: var(--color-primary); }
.btn-submit { padding: 10px 24px; border: none; border-radius: var(--radius-full); background: var(--color-primary); color: #fff; font-size: 14px; font-weight: 600; font-family: var(--font-family); cursor: pointer; transition: all var(--transition-fast); }
.btn-submit:hover:not(:disabled) { background: var(--color-primary-dark); }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
