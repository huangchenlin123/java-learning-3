<template>
  <div>
    <el-card>
      <el-button type="success" @click="openDialog()" style="margin-bottom:15px">新增菜单</el-button>
      <el-table :data="tableData" border stripe row-key="id" default-expand-all v-loading="loading">
        <el-table-column prop="menuName" label="菜单名称" />
        <el-table-column prop="menuType" label="类型" width="80"><template #default="{row}"><el-tag size="small">{{ row.menuType }}</el-tag></template></el-table-column>
        <el-table-column prop="path" label="路由路径" />
        <el-table-column prop="perms" label="权限标识" />
        <el-table-column prop="sort" label="排序" width="60" />
        <el-table-column label="操作" width="200">
          <template #default="{row}">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog :title="form.id?'编辑菜单':'新增菜单'" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.menuType"><el-option label="目录" value="M" /><el-option label="菜单" value="C" /><el-option label="按钮" value="F" /></el-select></el-form-item>
        <el-form-item label="路径"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="form.perms" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listMenu, saveMenu, updateMenu, deleteMenu } from '../../api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, menuName: '', menuType: 'C', path: '', perms: '', sort: 0 })

onMounted(() => fetchData())
async function fetchData() {
  loading.value = true
  try { const res = await listMenu(); tableData.value = res.data.data || [] } finally { loading.value = false }
}
function openDialog(row) {
  Object.assign(form, { id: null, menuName: '', menuType: 'C', path: '', perms: '', sort: 0 })
  if (row) Object.assign(form, row)
  dialogVisible.value = true
}
async function handleSave() {
  if (form.id) await updateMenu({...form})
  else await saveMenu({...form})
  ElMessage.success('保存成功'); dialogVisible.value = false; fetchData()
}
async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await deleteMenu(id); ElMessage.success('已删除'); fetchData()
}
</script>
