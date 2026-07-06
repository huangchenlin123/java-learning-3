<template>
  <el-card>
    <el-form :inline="true">
      <el-form-item><el-select v-model="search.promoType" placeholder="促销类型" clearable><el-option label="满减" value="FULL_REDUCTION" /><el-option label="折扣" value="DISCOUNT" /><el-option label="特价" value="FLASH_SALE" /><el-option label="买赠" value="BUY_GIFT" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      <el-form-item><el-button type="success" @click="openDialog()">新增促销</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="promoName" label="活动名称" />
      <el-table-column prop="promoType" label="类型" width="100"><template #default="{row}"><el-tag>{{ row.promoType }}</el-tag></template></el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="160" />
      <el-table-column prop="endTime" label="结束时间" width="160" />
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column prop="status" label="状态" width="80"><template #default="{row}"><el-switch :model-value="row.status===1" @change="toggleStatus(row)" /></template></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}"><el-button size="small" @click="openDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:15px" v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total,prev,pager,next" @current-change="fetchData" />

    <el-dialog :title="form.id?'编辑促销':'新增促销'" v-model="dialogVisible" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="活动名称"><el-input v-model="form.promoName" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.promoType"><el-option label="满减" value="FULL_REDUCTION" /><el-option label="折扣" value="DISCOUNT" /><el-option label="限时特价" value="FLASH_SALE" /><el-option label="买赠" value="BUY_GIFT" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" /></el-form-item>
        <el-form-item label="优先级"><el-input-number v-model="form.priority" :min="0" /></el-form-item>
        <el-form-item label="规则">
          <div v-for="(rule, idx) in form.rules" :key="idx" style="margin-bottom:8px">
            <el-select v-model="rule.ruleType" size="small" style="width:90px"><el-option label="条件" value="CONDITION" /><el-option label="动作" value="ACTION" /></el-select>
            <el-input-number v-model="rule.threshold" size="small" :min="0" placeholder="阈值" style="width:100px" />
            <el-input-number v-model="rule.discount" size="small" :min="0" :precision="2" placeholder="优惠值" style="width:100px" />
            <el-button size="small" type="danger" @click="form.rules.splice(idx,1)">×</el-button>
          </div>
          <el-button size="small" @click="form.rules.push({ruleType:'CONDITION',threshold:null,discount:null})">+ 添加规则</el-button>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pagePromotion, savePromotion, updatePromotion, deletePromotion } from '../../api/sale'
import { ElMessage, ElMessageBox } from 'element-plus'
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const search = reactive({ promoType: '' }), dialogVisible = ref(false)
const form = reactive({ id: null, promoName: '', promoType: 'FULL_REDUCTION', startTime: '', endTime: '', priority: 0, rules: [] })
onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const r = await pagePromotion({ pageNum: pageNum.value, pageSize: pageSize.value, promoType: search.promoType }); tableData.value = r.data.data.records; total.value = r.data.data.total } finally { loading.value = false } }
function openDialog(row) {
  Object.assign(form, { id: null, promoName: '', promoType: 'FULL_REDUCTION', startTime: '', endTime: '', priority: 0, rules: [] })
  if (row) {
    Object.assign(form, row)
    if (!form.rules) form.rules = []
  }
  dialogVisible.value = true
}
async function handleSave() {
  if (form.id) await updatePromotion({...form}); else await savePromotion({...form})
  ElMessage.success('保存成功'); dialogVisible.value = false; fetchData()
}
async function handleDelete(id) { await ElMessageBox.confirm('确定删除？','提示',{type:'warning'}); await deletePromotion(id); ElMessage.success('已删除'); fetchData() }
async function toggleStatus(row) {
  await updatePromotion({ id: row.id, status: row.status === 1 ? 0 : 1 })
  fetchData()
}
</script>
