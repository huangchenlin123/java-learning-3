<template>
  <el-card>
    <el-form :inline="true">
      <el-form-item><el-input v-model="search.status" placeholder="状态" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      <el-form-item><el-button type="success" @click="openDialog()">新增采购单</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="orderNo" label="采购单号" width="160" />
      <el-table-column prop="supplierName" label="供应商" />
      <el-table-column prop="totalAmount" label="金额(元)" />
      <el-table-column prop="status" label="状态" width="100"><template #default="{row}"><el-tag :type="row.status==='APPROVED'?'success':row.status==='PENDING'?'warning':'info'">{{ row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="openDialog(row)">查看</el-button>
          <el-button v-if="row.status==='PENDING'" size="small" type="success" @click="approve(row.id,true)">审批</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px" v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total,prev,pager,next" @current-change="fetchData" />

    <el-dialog title="采购单" v-model="dialogVisible" width="700px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="供应商"><el-select v-model="form.supplierId" filterable><el-option v-for="s in suppliers" :key="s.id" :label="s.supplierName" :value="s.id" /></el-select></el-form-item>
        <el-form-item label="总金额"><el-input-number v-model="form.totalAmount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pagePurchaseOrder, savePurchaseOrder, approvePurchaseOrder } from '../../api/purchase'
import { pageSupplier } from '../../api/purchase'
import { ElMessage } from 'element-plus'
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const search = reactive({ status: '' }), dialogVisible = ref(false), suppliers = ref([])
const form = reactive({ id: null, supplierId: null, totalAmount: 0, remark: '' })
onMounted(async () => { fetchData(); const r = await pageSupplier({ pageSize: 999 }); suppliers.value = r.data.data.records })
async function fetchData() {
  loading.value = true
  try { const res = await pagePurchaseOrder({ pageNum: pageNum.value, pageSize: pageSize.value, status: search.status }); tableData.value = res.data.data.records; total.value = res.data.data.total } finally { loading.value = false }
}
function openDialog(row) {
  Object.assign(form, { id: null, supplierId: null, totalAmount: 0, remark: '' })
  if (row) Object.assign(form, row)
  dialogVisible.value = true
}
async function handleSave() { await savePurchaseOrder({...form}); ElMessage.success('保存成功'); dialogVisible.value = false; fetchData() }
async function approve(id, ok) { await approvePurchaseOrder(id, ok); ElMessage.success('审批完成'); fetchData() }
</script>
