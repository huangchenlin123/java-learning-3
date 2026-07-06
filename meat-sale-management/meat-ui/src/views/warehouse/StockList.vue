<template>
  <el-card>
    <el-form :inline="true">
      <el-form-item><el-input v-model="search.skuName" placeholder="商品名称" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="skuName" label="商品名称" />
      <el-table-column prop="batchNo" label="批次号" />
      <el-table-column prop="warehouseName" label="仓库" />
      <el-table-column prop="quantity" label="库存(kg)" />
      <el-table-column prop="lockedQuantity" label="锁定(kg)" />
      <el-table-column prop="available" label="可用(kg)"><template #default="{row}">{{ (row.quantity||0) - (row.lockedQuantity||0) }}</template></el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px" v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total,prev,pager,next" @current-change="fetchData" />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageStock } from '../../api/warehouse'
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const search = reactive({ skuName: '' })
onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const r = await pageStock({ pageNum: pageNum.value, pageSize: pageSize.value, ...search }); tableData.value = r.data.data.records; total.value = r.data.data.total } finally { loading.value = false } }
</script>
