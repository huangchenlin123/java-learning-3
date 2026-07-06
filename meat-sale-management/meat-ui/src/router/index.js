import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/dashboard/Dashboard.vue'), meta: { title: '经营概览' } },
      { path: 'system/user', name: 'UserList', component: () => import('../views/system/UserList.vue'), meta: { title: '用户管理' } },
      { path: 'system/role', name: 'RoleList', component: () => import('../views/system/RoleList.vue'), meta: { title: '角色管理' } },
      { path: 'system/menu', name: 'MenuList', component: () => import('../views/system/MenuList.vue'), meta: { title: '菜单管理' } },
      { path: 'system/dict', name: 'DictList', component: () => import('../views/system/DictList.vue'), meta: { title: '字典管理' } },
      { path: 'purchase/supplier', name: 'SupplierList', component: () => import('../views/purchase/SupplierList.vue'), meta: { title: '供应商管理' } },
      { path: 'purchase/category', name: 'CategoryList', component: () => import('../views/purchase/CategoryList.vue'), meta: { title: '商品分类' } },
      { path: 'purchase/sku', name: 'SkuList', component: () => import('../views/purchase/SkuList.vue'), meta: { title: '商品SKU' } },
      { path: 'purchase/order', name: 'PurchaseOrderList', component: () => import('../views/purchase/PurchaseOrderList.vue'), meta: { title: '采购订单' } },
      { path: 'purchase/receipt', name: 'PurchaseReceiptList', component: () => import('../views/purchase/PurchaseReceiptList.vue'), meta: { title: '采购验收' } },
      { path: 'warehouse/stock', name: 'StockList', component: () => import('../views/warehouse/StockList.vue'), meta: { title: '库存查询' } },
      { path: 'warehouse/stockin', name: 'StockInList', component: () => import('../views/warehouse/StockInList.vue'), meta: { title: '入库管理' } },
      { path: 'warehouse/process', name: 'ProcessOrderList', component: () => import('../views/warehouse/ProcessOrderList.vue'), meta: { title: '分割加工' } },
      { path: 'warehouse/expiry', name: 'ExpiryWarningList', component: () => import('../views/warehouse/ExpiryWarningList.vue'), meta: { title: '效期预警' } },
      { path: 'sale/pos', name: 'PosSale', component: () => import('../views/sale/PosSale.vue'), meta: { title: 'POS收银' } },
      { path: 'sale/order', name: 'SaleOrderList', component: () => import('../views/sale/SaleOrderList.vue'), meta: { title: '销售订单' } },
      { path: 'sale/customer', name: 'CustomerList', component: () => import('../views/sale/CustomerList.vue'), meta: { title: '客户管理' } },
      { path: 'sale/promotion', name: 'PromotionList', component: () => import('../views/sale/PromotionList.vue'), meta: { title: '促销管理' } },
      { path: 'finance/report', name: 'FinanceReport', component: () => import('../views/finance/FinanceReport.vue'), meta: { title: '财务报表' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
