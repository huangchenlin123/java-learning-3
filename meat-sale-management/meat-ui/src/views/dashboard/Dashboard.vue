<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover" style="margin-bottom:20px">
          <div style="display:flex;align-items:center;justify-content:space-between">
            <div>
              <p style="color:#999;font-size:14px;margin:0">{{ card.label }}</p>
              <p style="font-size:24px;font-weight:bold;margin:8px 0 0">{{ card.value }}</p>
            </div>
            <el-icon :size="40" :color="card.color"><component :is="card.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card><template #header>销售趋势</template><div ref="salesChartRef" style="height:300px"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><template #header>品类占比</template><div ref="pieChartRef" style="height:300px"></div></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { dashboard, salesReport, categoryRanking } from '../../api/finance'
import * as echarts from 'echarts'

const cards = reactive([
  { label: '今日销售额', value: '¥0', icon: 'Money', color: '#67C23A' },
  { label: '本月销售额', value: '¥0', icon: 'TrendCharts', color: '#409EFF' },
  { label: '库存价值', value: '¥0', icon: 'Box', color: '#E6A23C' },
  { label: '待处理订单', value: '0', icon: 'Document', color: '#F56C6C' },
  { label: '效期预警', value: '0', icon: 'Warning', color: '#F56C6C' },
  { label: '毛利率', value: '0%', icon: 'DataAnalysis', color: '#909399' }
])

const salesChartRef = ref(null)
const pieChartRef = ref(null)

onMounted(async () => {
  try {
    const dRes = await dashboard()
    const d = dRes.data.data
    cards[0].value = '¥' + (d.todaySales || 0).toLocaleString()
    cards[1].value = '¥' + (d.monthSales || 0).toLocaleString()
    cards[2].value = '¥' + (d.inventoryValue || 0).toLocaleString()
    cards[3].value = d.pendingOrders || 0
    cards[4].value = d.expiringSoon || 0
    cards[5].value = ((d.grossProfit || 0) * 100).toFixed(1) + '%'
  } catch (e) { /* use defaults */ }

  await nextTick()
  initCharts()
})

async function initCharts() {
  try {
    const sRes = await salesReport('day')
    const sData = sRes.data.data || []
    const chart1 = echarts.init(salesChartRef.value)
    chart1.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: sData.map(i => i.date) },
      yAxis: { type: 'value' },
      series: [{ data: sData.map(i => i.amount), type: 'line', smooth: true, areaStyle: {} }]
    })

    const cRes = await categoryRanking()
    const cData = cRes.data.data || []
    const chart2 = echarts.init(pieChartRef.value)
    chart2.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: cData.map(i => ({ name: i.category, value: i.amount }))
      }]
    })
  } catch (e) { /* chart init error */ }
}
</script>
