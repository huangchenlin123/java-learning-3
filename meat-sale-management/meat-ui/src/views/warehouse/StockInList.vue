<template>
  <el-card>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="inNo" label="入库单号" width="170" />
      <el-table-column prop="inType" label="类型" width="90">
        <template #default="{row}"><el-tag size="small">{{ row.inType==='PURCHASE'?'采购入库':row.inType==='PROCESS'?'加工入库':row.inType }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="sourceId" label="来源ID" width="80" />
      <el-table-column prop="totalQuantity" label="数量(kg)" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{row}"><el-tag :type="row.status==='CONFIRMED'?'success':'warning'" size="small">{{ row.status==='CONFIRMED'?'已入库':row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="confirmTime" label="入库时间" width="160" />
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button v-if="row.status==='PENDING'" size="small" type="success" @click="confirm(row.id)">确认入库</el-button>
          <el-tag v-else size="small" type="success">已完成</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px" v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total,prev,pager,next" @current-change="fetchData" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pageStockIn, confirmStockIn } from '../../api/warehouse'
import { ElMessage } from 'element-plus'
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const r = await pageStockIn({ pageNum: pageNum.value, pageSize: pageSize.value }); tableData.value = r.data.data.records; total.value = r.data.data.total } finally { loading.value = false } }
async function confirm(id) { await confirmStockIn(id); ElMessage.success('入库确认成功，库存已更新'); fetchData() }
</script>
