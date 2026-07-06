<template>
  <el-card>
    <el-form :inline="true">
      <el-form-item><el-select v-model="search.warnLevel" placeholder="预警级别" clearable><el-option label="严重" value="CRITICAL" /><el-option label="警告" value="WARNING" /><el-option label="注意" value="NOTICE" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      <el-form-item><el-button type="warning" @click="manualScan">手动扫描</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="batchId" label="批次ID" width="80" />
      <el-table-column prop="skuId" label="商品ID" width="80" />
      <el-table-column prop="quantity" label="库存(kg)" />
      <el-table-column prop="expireDate" label="到期日" />
      <el-table-column prop="daysRemaining" label="剩余天数" width="100"><template #default="{row}"><span :style="{color:row.daysRemaining<=3?'#F56C6C':row.daysRemaining<=7?'#E6A23C':'#409EFF'}">{{ row.daysRemaining }} 天</span></template></el-table-column>
      <el-table-column prop="warnLevel" label="级别" width="100"><template #default="{row}"><el-tag :type="row.warnLevel==='CRITICAL'?'danger':row.warnLevel==='WARNING'?'warning':'info'">{{ row.warnLevel }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{row}"><el-button v-if="!row.isHandled" size="small" type="success" @click="handleWarn(row.id)">已处理</el-button></template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listExpiryWarnings, handleExpiryWarning, manualScanExpiry } from '../../api/warehouse'
import { ElMessage } from 'element-plus'
const loading = ref(false), tableData = ref([])
const search = reactive({ warnLevel: '' })
onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const r = await listExpiryWarnings({ warnLevel: search.warnLevel }); tableData.value = r.data.data || [] } finally { loading.value = false } }
async function handleWarn(id) { await handleExpiryWarning(id); ElMessage.success('已标记处理'); fetchData() }
async function manualScan() { const r = await manualScanExpiry(); ElMessage.success(`扫描完成，发现 ${r.data.data} 条预警`); fetchData() }
</script>
