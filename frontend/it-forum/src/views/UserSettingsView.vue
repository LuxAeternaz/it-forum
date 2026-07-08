<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { updateProfile, changePassword, updateAvatar } from '../api/user'
import { uploadImage } from '../api/upload'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()

const profileForm = ref({ username: '', bio: '' })
const passwordForm = ref({ oldPassword: '', newPassword: '' })

const avatarSrc = computed(() => {
  if (auth.user?.avatarUrl) return auth.user.avatarUrl
  return '/default-avatar.svg'
})

onMounted(() => {
  profileForm.value.username = auth.user?.username || ''
  profileForm.value.bio = auth.user?.bio || ''
})

async function handleSaveProfile() {
  await updateProfile(profileForm.value)
  ElMessage.success('资料已更新')
  auth.fetchProfile()
}

async function handleChangePassword() {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    return ElMessage.warning('请填写完整密码信息')
  }
  if (passwordForm.value.newPassword.length < 6) {
    return ElMessage.warning('新密码至少6位')
  }
  await changePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword)
  ElMessage.success('密码已修改')
  passwordForm.value = { oldPassword: '', newPassword: '' }
}

async function handleAvatarUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const res = await uploadImage(file)
  await updateAvatar(res.filePath)
  ElMessage.success('头像已更新')
  auth.fetchProfile()
}
</script>

<template>
  <div class="settings-page">
    <h2 class="page-title">个人设置</h2>

    <div class="card settings-card">
      <h4 class="section-title">基本信息</h4>
      <div class="avatar-area">
        <img :src="avatarSrc" :alt="auth.user?.username" class="settings-avatar" />
        <label class="upload-hint">
          更换头像
          <input type="file" accept="image/*" class="file-input" @change="handleAvatarUpload" />
        </label>
      </div>

      <div class="form-group">
        <label class="form-label">用户名</label>
        <input v-model="profileForm.username" type="text" class="form-input" placeholder="用户名" maxlength="50" />
      </div>
      <div class="form-group">
        <label class="form-label">个人简介</label>
        <textarea v-model="profileForm.bio" class="form-textarea" rows="3" placeholder="写点什么介绍自己..." maxlength="500" />
      </div>
      <button class="save-btn" @click="handleSaveProfile">保存资料</button>
    </div>

    <div class="card settings-card">
      <h4 class="section-title">修改密码</h4>
      <div class="form-group">
        <label class="form-label">原密码</label>
        <input v-model="passwordForm.oldPassword" type="password" class="form-input" placeholder="请输入原密码" />
      </div>
      <div class="form-group">
        <label class="form-label">新密码</label>
        <input v-model="passwordForm.newPassword" type="password" class="form-input" placeholder="请输入新密码（至少6位）" />
      </div>
      <button class="save-btn secondary" @click="handleChangePassword">修改密码</button>
    </div>
  </div>
</template>

<style scoped>
.settings-page { max-width: 640px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: 20px; }

.settings-card { padding: 28px; margin-bottom: 16px; }
.section-title { font-size: 15px; font-weight: 700; color: var(--color-text); margin-bottom: 20px; }

.avatar-area { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.settings-avatar {
  width: 64px; height: 64px; border-radius: 50%;
  border: 2px solid var(--color-border-light); background: var(--color-bg-slate);
}
.upload-hint {
  font-size: 13px; color: var(--color-primary); cursor: pointer; font-weight: 500;
}
.file-input { display: none; }

.form-group { margin-bottom: 16px; }
.form-label {
  display: block; font-size: 13px; font-weight: 600;
  color: var(--color-text-secondary); margin-bottom: 6px;
}
.form-input {
  width: 100%; padding: 10px 14px; border: 1px solid var(--color-border);
  border-radius: var(--radius-md); font-size: 14px; font-family: var(--font-family);
  color: var(--color-text); outline: none;
  transition: border-color var(--transition-fast);
}
.form-input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(64,158,255,0.1); }
.form-textarea {
  width: 100%; padding: 10px 14px; border: 1px solid var(--color-border);
  border-radius: var(--radius-md); font-size: 14px; font-family: var(--font-family);
  color: var(--color-text); outline: none; resize: vertical;
  transition: border-color var(--transition-fast);
}
.form-textarea:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(64,158,255,0.1); }

.save-btn {
  padding: 10px 28px; border: none; border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary), var(--color-indigo));
  color: #fff; font-size: 14px; font-weight: 600; font-family: var(--font-family);
  cursor: pointer; transition: all var(--transition-fast);
  box-shadow: 0 4px 14px rgba(64,158,255,0.3);
}
.save-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(64,158,255,0.4); }
.save-btn.secondary {
  background: transparent; color: var(--color-primary);
  border: 1px solid var(--color-primary); box-shadow: none;
}
.save-btn.secondary:hover { background: var(--color-primary-light); }
</style>
