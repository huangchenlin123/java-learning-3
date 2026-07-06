<template>
  <div>
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="销售报表" name="sales">
        <el-radio-group v-model="salesPeriod" @change="loadSales" style="margin-bottom:15px">
          <el-radio-button value="day">日</el-radio-button>
          <el-radio-button value="week">周</el-radio-button>
          <el-radio-button value="month">月</el-radio-button>
        </el-radio-group>
        <el-table :data="salesData" border stripe>
          <el-table-column prop="date" label="期间" />
          <el-table-column prop="amount" label="销售额(元)" />
          <el-table-column prop="orderCount" label="订单数" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="品类排行" name="category">
        <el-table :data="categoryData" border stripe>
          <el-table-column prop="category" label="品类" />
          <el-table-column prop="amount" label="销售额(元)" />
          <el-table-column prop="ratio" label="占比"><template #default="{row}">{{ (row.ratio * 100).toFixed(1) }}%</template></el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="库存价值" name="inventory">
        <el-table :data="inventoryData" border stripe>
          <el-table-column prop="category" label="品类" />
          <el-table-column prop="quantity" label="库存量(kg)" />
          <el-table-column prop="value" label="估算价值(元)" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="毛利报表" name="profit">
        <el-radio-group v-model="profitPeriod" @change="loadProfit" style="margin-bottom:15px">
          <el-radio-button value="day">日</el-radio-button>
          <el-radio-button value="week">周</el-radio-button>
          <el-radio-button value="month">月</el-radio-button>
        </el-radio-group>
        <el-table :data="profitData" border stripe>
          <el-table-column prop="date" label="期间" />
          <el-table-column prop="sales" label="销售额(元)" />
          <el-table-column prop="cost" label="成本(元)" />
          <el-table-column prop="profit" label="毛利(元)" />
          <el-table-column prop="marginRate" label="毛利率"><template #default="{row}">{{ (row.marginRate * 100).toFixed(1) }}%</template></el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="供应商对账" name="supplier">
        <el-table :data="supplierData" border stripe>
          <el-table-column prop="supplierName" label="供应商" />
          <el-table-column prop="totalAmount" label="采购总额(元)" />
          <el-table-column prop="orderCount" label="订单数" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="客户对账" name="customer">
        <el-table :data="customerData" border stripe>
          <el-table-column prop="customerName" label="客户" />
          <el-table-column prop="totalAmount" label="消费总额(元)" />
          <el-table-column prop="orderCount" label="订单数" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { salesReport, categoryRanking, inventoryReport, profitReport, supplierReconciliation, customerReconciliation } from '../../api/finance'

const activeTab = ref('sales')
const salesPeriod = ref('month')
const profitPeriod = ref('month')
const salesData = ref([])
const categoryData = ref([])
const inventoryData = ref([])
const profitData = ref([])
const supplierData = ref([])
const customerData = ref([])

onMounted(() => loadSales())

async function loadSales() { try { const r = await salesReport(salesPeriod.value); salesData.value = r.data.data || [] } catch (e) {} }
async function loadProfit() { try { const r = await profitReport(profitPeriod.value); profitData.value = r.data.data || [] } catch (e) {} }

// Load all data on mount
;(async () => {
  try { categoryData.value = (await categoryRanking()).data.data || [] } catch (e) {}
  try { inventoryData.value = (await inventoryReport()).data.data || [] } catch (e) {}
  try { supplierData.value = (await supplierReconciliation()).data.data || [] } catch (e) {}
  try { customerData.value = (await customerReconciliation()).data.data || [] } catch (e) {}
})()
</script>
