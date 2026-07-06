<template>
  <el-card>
    <el-button type="success" @click="openDialog()" style="margin-bottom:15px">新增客户</el-button>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="customerName" label="客户名称" />
      <el-table-column prop="customerType" label="类型" width="80" />
      <el-table-column prop="contactPerson" label="联系人" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column label="操作" width="200">
        <template #default="{row}"><el-button size="small" @click="openDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px" v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total,prev,pager,next" @current-change="fetchData" />

    <el-dialog :title="form.id?'编辑客户':'新增客户'" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.customerName" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.customerType"><el-option label="批发" value="WHOLESALE" /><el-option label="零售" value="RETAIL" /></el-select></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactPerson" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageCustomer, saveCustomer, updateCustomer } from '../../api/sale'
import { ElMessage, ElMessageBox } from 'element-plus'
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const dialogVisible = ref(false)
const form = reactive({ id: null, customerName: '', customerType: 'RETAIL', contactPerson: '', phone: '' })
onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const r = await pageCustomer({ pageNum: pageNum.value, pageSize: pageSize.value }); tableData.value = r.data.data.records; total.value = r.data.data.total } finally { loading.value = false } }
function openDialog(row) { Object.assign(form, { id: null, customerName: '', customerType: 'RETAIL', contactPerson: '', phone: '' }); if (row) Object.assign(form, row); dialogVisible.value = true }
async function handleSave() { if (form.id) await updateCustomer({...form}); else await saveCustomer({...form}); ElMessage.success('保存成功'); dialogVisible.value = false; fetchData() }
async function handleDelete(id) { await ElMessageBox.confirm('确定删除？','提示',{type:'warning'}); fetchData() }
</script>
