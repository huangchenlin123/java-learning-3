import request from './request'

export const pageSupplier = (params) => request.get('/purchase/supplier', { params })
export const saveSupplier = (data) => request.post('/purchase/supplier', data)
export const updateSupplier = (data) => request.put(`/purchase/supplier/${data.id}`, data)
export const deleteSupplier = (id) => request.delete(`/purchase/supplier/${id}`)

export const listCategory = () => request.get('/purchase/category')
export const saveCategory = (data) => request.post('/purchase/category', data)
export const updateCategory = (data) => request.put(`/purchase/category/${data.id}`, data)

export const pageSku = (params) => request.get('/purchase/sku', { params })
export const saveSku = (data) => request.post('/purchase/sku', data)
export const updateSku = (data) => request.put(`/purchase/sku/${data.id}`, data)

export const pagePurchaseOrder = (params) => request.get('/purchase/order', { params })
export const savePurchaseOrder = (data) => request.post('/purchase/order', data)
export const approvePurchaseOrder = (id, approved) => request.put(`/purchase/order/${id}/approve`, { approved })

export const pageReceipt = (params) => request.get('/purchase/receipt', { params })
export const getReceipt = (id) => request.get(`/purchase/receipt/${id}`)
export const saveReceipt = (data) => request.post('/purchase/receipt', data)
export const confirmReceipt = (id) => request.put(`/purchase/receipt/${id}/confirm`)
