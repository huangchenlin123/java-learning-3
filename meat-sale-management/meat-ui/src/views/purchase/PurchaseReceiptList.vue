<template>
  <el-card>
    <el-form :inline="true">
      <el-form-item><el-button type="primary" @click="fetchData">刷新</el-button></el-form-item>
      <el-form-item><el-button type="success" @click="openDialog()">新增验收单</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="receiptNo" label="验收单号" width="170" />
      <el-table-column prop="orderId" label="采购单ID" width="90" />
      <el-table-column prop="actualWeight" label="实际重量(kg)" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}"><el-tag :type="row.status==='CONFIRMED'?'success':'warning'">{{ row.status==='CONFIRMED'?'已验收':row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="openDialog(row)">查看</el-button>
          <el-button v-if="row.status==='PENDING'" size="small" type="success" @click="confirmIt(row.id)">确认验收</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px" v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total,prev,pager,next" @current-change="fetchData" />

    <el-dialog title="新增采购验收单" v-model="dialogVisible" width="650px" @opened="loadOrders">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="选择采购单" prop="orderId">
          <el-select v-model="form.orderId" filterable placeholder="搜索采购单号" style="width:100%" @change="onOrderChange">
            <el-option v-for="o in orderList" :key="o.id" :label="o.orderNo + ' - ￥' + o.totalAmount + ' (' + (o.supplierName||'供应商'+o.supplierId) + ')'" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="实际重量(kg)">
          <el-input-number v-model="form.actualWeight" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="产地">
          <el-input v-model="form.origin" placeholder="如：山东" />
        </el-form-item>
        <el-form-item label="屠宰日期">
          <el-date-picker v-model="form.slaughterDate" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="检疫证号">
          <el-input v-model="form.quarantineNo" placeholder="检疫证号" />
        </el-form-item>
        <el-alert type="success" :closable="false" show-icon style="margin-top:10px">
          <template #title>验收明细将自动从采购单加载；确认验收后自动创建批次、入库并更新库存</template>
        </el-alert>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageReceipt, saveReceipt, confirmReceipt } from '../../api/purchase'
import { pagePurchaseOrder } from '../../api/purchase'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const dialogVisible = ref(false), formRef = ref(null), orderList = ref([])
const form = reactive({ orderId: null, actualWeight: 0, origin: '', slaughterDate: null, quarantineNo: '' })
const rules = { orderId: [{ required: true, message: '请选择采购单', trigger: 'change' }] }

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try { const r = await pageReceipt({ pageNum: pageNum.value, pageSize: pageSize.value }); tableData.value = r.data.data.records; total.value = r.data.data.total } finally { loading.value = false }
}

async function loadOrders() {
  try {
    const r = await pagePurchaseOrder({ pageSize: 200, status: 'APPROVED' })
    orderList.value = (r.data.data.records || []).filter(o => o.status === 'APPROVED')
  } catch (e) { orderList.value = [] }
}

function onOrderChange(val) {
  const order = orderList.value.find(o => o.id === val)
  if (order && !form.actualWeight) {
    form.actualWeight = order.totalAmount ? Number(order.totalAmount) / 10 : 0
  }
}

function openDialog(row) {
  Object.assign(form, { orderId: null, actualWeight: 0, origin: '', slaughterDate: null, quarantineNo: '' })
  if (row) Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  await saveReceipt({...form})
  ElMessage.success('验收单创建成功，明细已自动加载')
  dialogVisible.value = false
  fetchData()
}

async function confirmIt(id) {
  await ElMessageBox.confirm('确认验收？将自动创建批次、入库并更新库存。', '确认验收', { type: 'warning' })
  await confirmReceipt(id)
  ElMessage.success('验收确认成功，库存已更新！')
  fetchData()
}
</script>
