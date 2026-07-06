<template>
  <el-card>
    <el-form :inline="true">
      <el-form-item><el-input v-model="search.skuName" placeholder="商品名称" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      <el-form-item><el-button type="success" @click="openDialog()">新增商品</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="skuName" label="商品名称" />
      <el-table-column prop="categoryName" label="分类" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="defaultPrice" label="默认售价" />
      <el-table-column label="操作" width="200">
        <template #default="{row}"><el-button size="small" @click="openDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px" v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total,prev,pager,next" @current-change="fetchData" />

    <el-dialog :title="form.id?'编辑':'新增'" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="SKU编码"><el-input v-model="form.skuCode" /></el-form-item>
        <el-form-item label="商品名称"><el-input v-model="form.skuName" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.categoryId"><el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.unit" /></el-form-item>
        <el-form-item label="售价"><el-input-number v-model="form.defaultPrice" :min="0" :precision="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageSku, saveSku, updateSku, listCategory } from '../../api/purchase'
import { ElMessage, ElMessageBox } from 'element-plus'
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0), categories = ref([])
const search = reactive({ skuName: '' }), dialogVisible = ref(false)
const form = reactive({ id: null, skuCode: '', skuName: '', categoryId: null, unit: 'kg', defaultPrice: 0 })
onMounted(async () => { fetchData(); const r = await listCategory(); categories.value = r.data.data || [] })
async function fetchData() { loading.value = true; try { const r = await pageSku({ pageNum: pageNum.value, pageSize: pageSize.value, ...search }); tableData.value = r.data.data.records; total.value = r.data.data.total } finally { loading.value = false } }
function openDialog(row) { Object.assign(form, { id: null, skuCode: '', skuName: '', categoryId: null, unit: 'kg', defaultPrice: 0 }); if (row) Object.assign(form, row); dialogVisible.value = true }
async function handleSave() { if (form.id) await updateSku({...form}); else await saveSku({...form}); ElMessage.success('保存成功'); dialogVisible.value = false; fetchData() }
async function handleDelete(id) { await ElMessageBox.confirm('确定删除？','提示',{type:'warning'}); fetchData() }
</script>
