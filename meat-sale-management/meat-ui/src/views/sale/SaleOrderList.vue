<template>
  <el-card>
    <el-form :inline="true">
      <el-form-item><el-select v-model="search.status" placeholder="状态" clearable><el-option label="已完成" value="COMPLETED" /><el-option label="待审批" value="PENDING" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="orderType" label="类型" width="80" />
      <el-table-column prop="totalAmount" label="原价" />
      <el-table-column prop="discountAmount" label="优惠" />
      <el-table-column prop="finalAmount" label="实付"><template #default="{row}"><span style="color:#F56C6C;font-weight:bold">¥{{ row.finalAmount }}</span></template></el-table-column>
      <el-table-column prop="status" label="状态" width="100"><template #default="{row}"><el-tag>{{ row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{row}">
          <el-button v-if="row.status==='PENDING'" size="small" type="success" @click="approve(row.id,true)">审批通过</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px" v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total,prev,pager,next" @current-change="fetchData" />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageSaleOrder, approveSaleOrder } from '../../api/sale'
import { ElMessage } from 'element-plus'
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const search = reactive({ status: '' })
onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const r = await pageSaleOrder({ pageNum: pageNum.value, pageSize: pageSize.value, status: search.status }); tableData.value = r.data.data.records; total.value = r.data.data.total } finally { loading.value = false } }
async function approve(id, ok) { await approveSaleOrder(id, ok); ElMessage.success('审批完成'); fetchData() }
</script>
