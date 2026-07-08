<script setup>
import { ref, onMounted } from 'vue'
import { getAdminPosts, deleteAdminPost } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { relativeTime } from '../../utils/time'

const posts = ref([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')
const loading = ref(false)
const size = 20

async function fetchPosts() {
  loading.value = true
  try {
    const data = await getAdminPosts(page.value, size, keyword.value)
    posts.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}

async function handleDelete(post) {
  try {
    await ElMessageBox.confirm(`确定删除帖子「${post.title}」？`, '确认删除', { type: 'warning' })
    await deleteAdminPost(post.id)
    ElMessage.success('帖子已删除')
    fetchPosts()
  } catch {}
}

function handleSearch() { page.value = 1; fetchPosts() }
function handlePageChange(p) { page.value = p; fetchPosts() }

onMounted(fetchPosts)
</script>

<template>
  <div class="page">
    <h1 class="page-title">帖子管理</h1>
    <div class="card">
      <div class="search-bar">
        <input v-model="keyword" class="search-input" placeholder="搜索帖子..." @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-table :data="posts" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip />
        <el-table-column prop="username" label="作者" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="浏览/评论/点赞" width="160">
          <template #default="{ row }">
            <span class="stats-text">{{ row.viewCount }} / {{ row.commentCount }} / {{ row.likeCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ relativeTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.search-input {
  flex: 1; padding: 8px 14px; border: 1px solid #dcdfe6; border-radius: 8px;
  font-size: 14px; outline: none; transition: border-color 0.2s;
}
.search-input:focus { border-color: #409eff; }
.stats-text { font-size: 12px; color: #909399; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }
</style>
