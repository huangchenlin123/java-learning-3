<template>
  <div style="display:flex;gap:20px;height:calc(100vh - 120px)">
    <!-- 左侧商品区 -->
    <div style="flex:1;display:flex;flex-direction:column;gap:15px">
      <el-card>
        <el-input v-model="skuSearch" placeholder="搜索商品（名称/条码）" clearable @keyup.enter="searchSku" />
      </el-card>
      <el-card style="flex:1;overflow-y:auto">
        <el-row :gutter="10">
          <el-col :span="8" v-for="sku in skuList" :key="sku.id" style="margin-bottom:10px">
            <el-card shadow="hover" style="cursor:pointer;text-align:center" @click="addToCart(sku)">
              <p style="font-weight:bold;margin:0">{{ sku.skuName }}</p>
              <p style="color:#F56C6C;font-size:18px;margin:5px 0">¥{{ sku.defaultPrice }}</p>
              <p style="color:#999;font-size:12px;margin:0">{{ sku.unit }}</p>
            </el-card>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 右侧购物车 -->
    <el-card style="width:400px;display:flex;flex-direction:column">
      <template #header><span style="font-size:16px;font-weight:bold">购物车</span></template>
      <div style="flex:1;overflow-y:auto">
        <el-table :data="cart" size="small" max-height="400">
          <el-table-column prop="skuName" label="商品" />
          <el-table-column prop="quantity" label="数量" width="60">
            <template #default="{row,$index}"><el-input-number v-model="row.quantity" :min="0.1" :step="0.5" size="small" controls-position="right" @change="recalc" /></template>
          </el-table-column>
          <el-table-column prop="finalPrice" label="单价" width="70" />
          <el-table-column label="小计" width="90"><template #default="{row}">{{ (row.finalPrice * row.quantity).toFixed(2) }}</template></el-table-column>
          <el-table-column width="40"><template #default="{ $index }"><el-button size="small" type="danger" :icon="'Delete'" circle @click="cart.splice($index,1);recalc()" /></template></el-table-column>
        </el-table>
      </div>
      <el-divider />
      <div style="font-size:14px">
        <el-row justify="space-between"><span>商品数:</span><span>{{ cart.length }} 种 / {{ totalQty }} kg</span></el-row>
        <el-row justify="space-between"><span>原价合计:</span><span>¥{{ totalAmount.toFixed(2) }}</span></el-row>
        <el-row justify="space-between"><span>优惠:</span><span style="color:#67C23A">-¥{{ discountAmount.toFixed(2) }}</span></el-row>
        <el-row justify="space-between" style="font-size:20px;font-weight:bold;margin-top:8px"><span>应收:</span><span style="color:#F56C6C">¥{{ finalAmount.toFixed(2) }}</span></el-row>
      </div>
      <el-button type="success" size="large" style="width:100%;margin-top:15px" @click="checkout" :loading="submitting">结 账</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { pageSku } from '../../api/purchase'
import { posSale, previewPromotion } from '../../api/sale'
import { ElMessage } from 'element-plus'

const skuSearch = ref('')
const skuList = ref([])
const cart = reactive([])
const submitting = ref(false)

const totalQty = computed(() => cart.reduce((s, i) => s + (i.quantity || 0), 0))
const totalAmount = computed(() => cart.reduce((s, i) => s + (i.finalPrice || i.originalPrice || 0) * (i.quantity || 0), 0))
const discountAmount = ref(0)
const finalAmount = computed(() => Math.max(0, totalAmount.value - discountAmount.value))

onMounted(() => searchSku())

async function searchSku() {
  try {
    const res = await pageSku({ pageNum: 1, pageSize: 30, skuName: skuSearch.value })
    skuList.value = res.data.data.records || []
  } catch (e) { /* */ }
}

function addToCart(sku) {
  const exist = cart.find(i => i.skuId === sku.id)
  if (exist) { exist.quantity += 1; recalc(); return }
  cart.push({
    skuId: sku.id,
    skuName: sku.skuName,
    quantity: 1,
    originalPrice: sku.defaultPrice || 0,
    finalPrice: sku.defaultPrice || 0,
    amount: sku.defaultPrice || 0
  })
  recalc()
}

async function recalc() {
  if (cart.length === 0) { discountAmount.value = 0; return }
  try {
    const order = {
      items: cart.map(i => ({ skuId: i.skuId, quantity: i.quantity, finalPrice: i.finalPrice, originalPrice: i.originalPrice }))
    }
    const res = await previewPromotion(order)
    discountAmount.value = res.data.data?.totalDiscount || 0
  } catch (e) { discountAmount.value = 0 }
}

async function checkout() {
  if (cart.length === 0) { ElMessage.warning('请添加商品'); return }
  submitting.value = true
  try {
    const order = {
      items: cart.map(i => ({ skuId: i.skuId, quantity: i.quantity, finalPrice: i.finalPrice, originalPrice: i.originalPrice }))
    }
    await posSale(order)
    ElMessage.success(`收银成功！应收 ¥${finalAmount.value.toFixed(2)}`)
    cart.length = 0
    discountAmount.value = 0
  } catch (e) { /* error handled by interceptor */ }
  finally { submitting.value = false }
}
</script>
