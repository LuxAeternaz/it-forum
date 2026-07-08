<script setup>
import { ref, onMounted } from 'vue'
import { getModerationLogs, reviewModeration, reviewAllApproved, getPendingCount, restoreModeration } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const logs = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const approvedPending = ref(0)
const size = ref(12)

async function fetch() {
  loading.value = true
  try {
    const data = await getModerationLogs(page.value, size.value)
    logs.value = data.list
    total.value = data.total
    getPendingCount().then(d => approvedPending.value = d.approvedPending).catch(() => {})
  } finally { loading.value = false }
}

async function handleReview(log) {
  await reviewModeration(log.id)
  log.reviewedBy = true
  approvedPending.value = Math.max(0, approvedPending.value - 1)
  ElMessage.success('已标记为已审核')
}

async function handleRestore(log) {
  try {
    await ElMessageBox.confirm(
      `确定恢复此${log.targetType === 'POST' ? '帖子' : '评论'}内容为正常状态？`,
      '确认恢复',
      { confirmButtonText: '确定恢复', cancelButtonText: '取消', type: 'warning' }
    )
    await restoreModeration(log.id)
    log.reviewedBy = true
    ElMessage.success('内容已恢复为正常状态')
  } catch {}
}

async function handleReviewAllApproved() {
  if (approvedPending.value === 0) return ElMessage.info('没有待审核的通过项')
  try {
    await ElMessageBox.confirm(
      `确定将 ${approvedPending.value} 条「通过」的审核结果一键标记为已审核？`,
      '批量审核确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
    const res = await reviewAllApproved()
    ElMessage.success(res || '批量审核完成')
    fetch()
  } catch {}
}

function handlePageChange(p) { page.value = p; fetch() }
function handleSizeChange(s) { size.value = s; page.value = 1; fetch() }

const resultLabels = { APPROVED: '通过', FLAGGED: '标记', REJECTED: '拒绝' }
const resultTypes = { APPROVED: 'success', FLAGGED: 'warning', REJECTED: 'danger' }

onMounted(fetch)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">审核日志</h1>
      <el-button
        type="success"
        :disabled="approvedPending === 0"
        @click="handleReviewAllApproved"
      >
        一键审核通过项（{{ approvedPending }}）
      </el-button>
    </div>
    <div class="card">
      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="targetType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.targetType === 'POST' ? '' : 'info'">
              {{ row.targetType === 'POST' ? '帖子' : '评论' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contentSnippet" label="内容摘要" min-width="250" show-overflow-tooltip />
        <el-table-column label="审核结果" width="100">
          <template #default="{ row }">
            <el-tag :type="resultTypes[row.result]" size="small">
              {{ resultLabels[row.result] || row.result }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confidence" label="置信度" width="90">
          <template #default="{ row }">
            {{ row.confidence != null ? (row.confidence * 100).toFixed(0) + '%' : '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.reviewedBy ? 'success' : 'warning'" size="small">
              {{ row.reviewedBy ? '已审核' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <template v-if="!row.reviewedBy">
              <el-button size="small" type="primary" @click="handleReview(row)">
                标记已审
              </el-button>
              <el-button
                v-if="row.result !== 'APPROVED'"
                size="small" type="warning"
                @click="handleRestore(row)"
              >
                恢复
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:16px; display:flex; justify-content:center; gap:20px; align-items:center;">
        <span style="color:#909399;font-size:13px;">共 {{ total }} 条，第 {{ page }}/{{ Math.ceil(total/size) || 1 }} 页</span>
        <el-button size="small" :disabled="page <= 1" @click="handlePageChange(page - 1)">上一页</el-button>
        <el-button size="small" :disabled="page >= Math.ceil(total/size)" @click="handlePageChange(page + 1)">下一页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title { font-size: 22px; font-weight: 700; color: #1e293b; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.card { background: #fff; padding: 20px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
</style>
