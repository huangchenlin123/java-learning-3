<template>
  <el-card>
    <el-form :inline="true">
      <el-form-item><el-select v-model="search.status" placeholder="状态" clearable><el-option label="待加工" value="PENDING" /><el-option label="已完成" value="COMPLETED" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      <el-form-item><el-button type="success" @click="openCreate()">新增加工单</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="processNo" label="加工单号" width="160" />
      <el-table-column prop="rawSkuId" label="原料SKU" width="80" />
      <el-table-column prop="rawQuantity" label="加工量(kg)" />
      <el-table-column prop="rawCost" label="原料成本" />
      <el-table-column prop="status" label="状态" width="100"><template #default="{row}"><el-tag :type="row.status==='COMPLETED'?'success':'warning'">{{ row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="openBomDialog(row)">查看产出</el-button>
          <el-button v-if="row.status==='PENDING'" size="small" type="success" @click="openComplete(row)">完成加工</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px" v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total,prev,pager,next" @current-change="fetchData" />

    <!-- 新增 dialog -->
    <el-dialog title="新增加工单" v-model="createVisible" width="550px">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="原料商品" prop="rawSkuId">
          <el-select v-model="form.rawSkuId" filterable placeholder="选择原料" style="width:100%">
            <el-option v-for="s in skuList" :key="s.id" :label="s.skuName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="BOM模板">
          <el-select v-model="form.bomId" clearable placeholder="可选" style="width:100%">
            <el-option v-for="b in bomList" :key="b.id" :label="b.bomName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="加工量(kg)" prop="rawQuantity">
          <el-input-number v-model="form.rawQuantity" :min="0.1" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="原料成本">
          <el-input-number v-model="form.rawCost" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="createVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>

    <!-- 完成加工 dialog — 填写产出明细 -->
    <el-dialog title="完成加工 — 产出明细" v-model="completeVisible" width="750px" @opened="loadBomItems">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:15px">
        <template #title>原料: {{ completeOrder.rawSkuId }} | 加工量: {{ completeOrder.rawQuantity }}kg | 成本: ¥{{ completeOrder.rawCost }}</template>
      </el-alert>
      <el-table :data="outputItems" border size="small">
        <el-table-column label="产出商品" width="180">
          <template #default="{row}"><el-select v-model="row.skuId" filterable placeholder="选择商品" size="small"><el-option v-for="s in skuList" :key="s.id" :label="s.skuName" :value="s.id" /></el-select></template>
        </el-table-column>
        <el-table-column label="预计产出(kg)" width="120">
          <template #default="{row}"><el-input-number v-model="row.expectedQuantity" :min="0" :precision="2" size="small" controls-position="right" /></template>
        </el-table-column>
        <el-table-column label="实际产出(kg)" width="130">
          <template #default="{row}"><el-input-number v-model="row.actualQuantity" :min="0" :precision="2" size="small" controls-position="right" /></template>
        </el-table-column>
        <el-table-column label="操作" width="60">
          <template #default="{ $index }"><el-button size="small" type="danger" @click="outputItems.splice($index,1)">×</el-button></template>
        </el-table-column>
      </el-table>
      <el-button size="small" type="primary" @click="addOutputRow" style="margin-top:10px">+ 添加产出项</el-button>
      <div style="margin-top:15px;color:#999;font-size:13px">
        总预计产出: {{ totalExpected.toFixed(2) }}kg | 总实际产出: {{ totalActual.toFixed(2) }}kg | 损耗: {{ (Math.max(0, totalExpected - totalActual)).toFixed(2) }}kg
      </div>
      <template #footer>
        <el-button @click="completeVisible=false">取消</el-button>
        <el-button type="success" @click="doComplete">确认完成（出库 + 分摊 + 入库）</el-button>
      </template>
    </el-dialog>

    <!-- 查看产出 dialog -->
    <el-dialog title="产出明细" v-model="bomVisible" width="600px">
      <el-table :data="bomData" border size="small">
        <el-table-column prop="skuId" label="商品ID" />
        <el-table-column prop="expectedQuantity" label="预计产出(kg)" />
        <el-table-column prop="actualQuantity" label="实际产出(kg)" />
        <el-table-column prop="costPrice" label="分摊成本" />
        <el-table-column prop="wasteQuantity" label="损耗(kg)" />
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { pageProcessOrder, saveProcessOrder, getProcessOrder, completeProcess, pageBom } from '../../api/warehouse'
import { pageSku } from '../../api/purchase'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const search = reactive({ status: '' })
const skuList = ref([]), bomList = ref([])

// 新增
const createVisible = ref(false), formRef = ref(null)
const form = reactive({ rawSkuId: null, bomId: null, rawQuantity: 0, rawCost: 0 })
const rules = { rawSkuId: [{ required: true, message: '请选择原料', trigger: 'change' }], rawQuantity: [{ required: true, message: '请输入加工量', trigger: 'blur' }] }

// 完成加工
const completeVisible = ref(false), completeOrder = reactive({})
const outputItems = ref([])

// 查看产出
const bomVisible = ref(false), bomData = ref([])

onMounted(async () => {
  fetchData()
  try { const r1 = await pageSku({ pageSize: 200 }); skuList.value = r1.data.data.records || [] } catch (e) {}
  try { const r2 = await pageBom(); bomList.value = r2.data.data || [] } catch (e) {}
})

async function fetchData() {
  loading.value = true
  try { const r = await pageProcessOrder({ pageNum: pageNum.value, pageSize: pageSize.value, ...search }); tableData.value = r.data.data.records; total.value = r.data.data.total } finally { loading.value = false }
}

function openCreate() { Object.assign(form, { rawSkuId: null, bomId: null, rawQuantity: 0, rawCost: 0 }); createVisible.value = true }

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  await saveProcessOrder({...form})
  ElMessage.success('加工单创建成功')
  createVisible.value = false
  fetchData()
}

// --- 完成加工 ---
async function openComplete(row) {
  const r = await getProcessOrder(row.id)
  const order = r.data.data
  Object.assign(completeOrder, order)
  // 预填产出明细（从已有数据或 BOM）
  if (order.outputs && order.outputs.length > 0) {
    outputItems.value = order.outputs.map(o => ({...o}))
  } else {
    outputItems.value = []
  }
  completeVisible.value = true
}

async function loadBomItems() {
  // 如果已有产出项或已绑定 BOM，则自动加载 BOM 明细
  if (outputItems.value.length > 0) return
  if (!completeOrder.bomId) return
  try {
    const r = await pageBom()
    const boms = r.data.data || []
    const bom = boms.find(b => b.id === completeOrder.bomId)
    if (bom && bom.items && bom.items.length > 0) {
      outputItems.value = bom.items.map(i => ({
        skuId: i.outputSkuId,
        expectedQuantity: (completeOrder.rawQuantity || 0) * (i.outputRatio || 0) / 100,
        actualQuantity: (completeOrder.rawQuantity || 0) * (i.outputRatio || 0) / 100
      }))
    }
  } catch (e) {}
}

function addOutputRow() {
  outputItems.value.push({ skuId: null, expectedQuantity: 0, actualQuantity: 0 })
}

const totalExpected = computed(() => outputItems.value.reduce((s, i) => s + (Number(i.expectedQuantity) || 0), 0))
const totalActual = computed(() => outputItems.value.reduce((s, i) => s + (Number(i.actualQuantity) || 0), 0))

async function doComplete() {
  if (outputItems.value.length === 0) { ElMessage.warning('请至少添加一项产出'); return }
  if (totalActual.value <= 0) { ElMessage.warning('实际产出量必须大于 0'); return }
  await ElMessageBox.confirm(
    `确认完成加工？原料出库 ${completeOrder.rawQuantity}kg，实际产出 ${totalActual.value.toFixed(2)}kg，系统将自动分摊成本。`,
    '确认', { type: 'warning' }
  )
  const body = {
    rawCost: completeOrder.rawCost,
    outputs: outputItems.value.map(i => ({
      skuId: i.skuId,
      expectedQuantity: i.expectedQuantity,
      actualQuantity: i.actualQuantity
    }))
  }
  await completeProcess(completeOrder.id, body)
  ElMessage.success('加工完成！原料已出库，产出入库，成本已分摊')
  completeVisible.value = false
  fetchData()
}

// --- 查看产出 ---
async function openBomDialog(row) {
  try {
    const r = await getProcessOrder(row.id)
    const order = r.data.data
    bomData.value = order.outputs || []
    bomVisible.value = true
  } catch (e) { bomData.value = [] }
}
</script>
