import request from './request'

export const dashboard = () => request.get('/finance/dashboard')
export const salesReport = (period) => request.get('/finance/sales/report', { params: { period } })
export const categoryRanking = () => request.get('/finance/category/ranking')
export const supplierReconciliation = () => request.get('/finance/supplier/reconciliation')
export const customerReconciliation = () => request.get('/finance/customer/reconciliation')
export const inventoryReport = () => request.get('/finance/inventory/report')
export const profitReport = (period) => request.get('/finance/profit/report', { params: { period } })
