<template>
  <el-card>
    <el-button type="success" @click="openDialog()" style="margin-bottom:15px">新增分类</el-button>
    <el-table :data="tableData" border stripe row-key="id" default-expand-all v-loading="loading">
      <el-table-column prop="categoryName" label="分类名称" />
      <el-table-column prop="categoryCode" label="编码" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="200">
        <template #default="{row}"><el-button size="small" @click="openDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-dialog :title="form.id?'编辑':'新增'" v-model="dialogVisible" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.categoryName" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.categoryCode" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listCategory, saveCategory, updateCategory } from '../../api/purchase'
import { ElMessage, ElMessageBox } from 'element-plus'
const loading = ref(false), tableData = ref([]), dialogVisible = ref(false)
const form = reactive({ id: null, categoryName: '', categoryCode: '', sort: 0 })
onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const r = await listCategory(); tableData.value = r.data.data || [] } finally { loading.value = false } }
function openDialog(row) { Object.assign(form, { id: null, categoryName: '', categoryCode: '', sort: 0 }); if (row) Object.assign(form, row); dialogVisible.value = true }
async function handleSave() { if (form.id) await updateCategory({...form}); else await saveCategory({...form}); ElMessage.success('保存成功'); dialogVisible.value = false; fetchData() }
async function handleDelete(id) { await ElMessageBox.confirm('确定删除？','提示',{type:'warning'}); fetchData() }
</script>
