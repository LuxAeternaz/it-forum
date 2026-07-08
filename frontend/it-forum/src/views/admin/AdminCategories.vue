<script setup>
import { ref, onMounted } from 'vue'
import { getAdminCategories, createCategory, updateCategory, deleteCategory } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const categories = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ name: '', description: '', icon: '', sortOrder: 0 })

async function fetch() {
  categories.value = await getAdminCategories()
}

function openCreate() {
  isEdit.value = false
  form.value = { name: '', description: '', icon: 'Folder', sortOrder: 0 }
  dialogVisible.value = true
}

function openEdit(cat) {
  isEdit.value = true
  form.value = { ...cat }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) return ElMessage.warning('请输入分类名称')
  if (isEdit.value) {
    await updateCategory(form.value.id, form.value)
    ElMessage.success('分类已更新')
  } else {
    await createCategory(form.value)
    ElMessage.success('分类已创建')
  }
  dialogVisible.value = false
  fetch()
}

async function handleDelete(cat) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${cat.name}」？`, '确认', { type: 'warning' })
    await deleteCategory(cat.id)
    ElMessage.success('分类已删除')
    fetch()
  } catch {}
}

onMounted(fetch)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">分类管理</h1>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>
    <div class="card">
      <el-table :data="categories" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="460px">
      <el-form label-position="top">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="分类描述" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名，如 Folder" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-title { font-size: 22px; font-weight: 700; color: #1e293b; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.card { background: #fff; padding: 20px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
</style>
