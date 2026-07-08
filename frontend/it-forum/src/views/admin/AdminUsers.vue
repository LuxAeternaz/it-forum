<script setup>
import { ref, onMounted } from 'vue'
import { getAdminUsers, updateUserRole, updateUserStatus } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const size = 20

async function fetchUsers() {
  loading.value = true
  try {
    const data = await getAdminUsers(page.value, size)
    users.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}

async function handleRoleChange(user) {
  const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
  try {
    await ElMessageBox.confirm(`确定将 ${user.username} 的角色改为 ${newRole}？`, '确认', { type: 'warning' })
    await updateUserRole(user.id, newRole)
    user.role = newRole
    ElMessage.success('角色已更新')
  } catch {}
}

async function handleStatusToggle(user) {
  const newStatus = user.status === 1 ? 0 : 1
  const label = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${label}用户 ${user.username}？`, '确认', { type: 'warning' })
    await updateUserStatus(user.id, newStatus)
    user.status = newStatus
    ElMessage.success(`用户已${label}`)
  } catch {}
}

function handlePageChange(p) { page.value = p; fetchUsers() }

onMounted(fetchUsers)
</script>

<template>
  <div class="page">
    <h1 class="page-title">用户管理</h1>
    <div class="card">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
              {{ row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleRoleChange(row)">
              {{ row.role === 'ADMIN' ? '降为用户' : '升为管理员' }}
            </el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="handleStatusToggle(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          background layout="prev, pager, next"
          :total="total" :page-size="size"
          v-model:current-page="page" @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title { font-size: 22px; font-weight: 700; color: #1e293b; margin-bottom: 20px; }
.card { background: #fff; padding: 20px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }
</style>
