<template>
  <div>
    <el-card>
      <el-button type="success" @click="openDialog()" style="margin-bottom:15px">新增字典</el-button>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="dictName" label="字典名称" />
        <el-table-column prop="dictCode" label="字典编码" />
        <el-table-column label="操作" width="200">
          <template #default="{row}">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog :title="form.id?'编辑字典':'新增字典'" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="字典名称"><el-input v-model="form.dictName" /></el-form-item>
        <el-form-item label="字典编码"><el-input v-model="form.dictCode" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listDict, saveDict, updateDict, deleteDict } from '../../api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, dictName: '', dictCode: '' })

onMounted(() => fetchData())
async function fetchData() {
  loading.value = true
  try { const res = await listDict(); tableData.value = res.data.data || [] } finally { loading.value = false }
}
function openDialog(row) {
  Object.assign(form, { id: null, dictName: '', dictCode: '' })
  if (row) Object.assign(form, row)
  dialogVisible.value = true
}
async function handleSave() {
  if (form.id) await updateDict({...form})
  else await saveDict({...form})
  ElMessage.success('保存成功'); dialogVisible.value = false; fetchData()
}
async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await deleteDict(id); ElMessage.success('已删除'); fetchData()
}
</script>
