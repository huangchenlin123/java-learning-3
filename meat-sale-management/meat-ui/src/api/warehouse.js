import request from './request'

export const pageStock = (params) => request.get('/warehouse/stock', { params })

export const pageStockIn = (params) => request.get('/warehouse/stockin', { params })
export const confirmStockIn = (id) => request.put(`/warehouse/stockin/${id}/confirm`)

export const pageProcessOrder = (params) => request.get('/warehouse/process', { params })
export const getProcessOrder = (id) => request.get(`/warehouse/process/${id}`)
export const saveProcessOrder = (data) => request.post('/warehouse/process', data)
export const completeProcess = (id, data) => request.put(`/warehouse/process/${id}/complete`, data)

export const pageBom = () => request.get('/warehouse/process/bom')
export const saveBom = (data) => request.post('/warehouse/process/bom', data)

export const listExpiryWarnings = (params) => request.get('/warehouse/expiry-warning', { params })
export const handleExpiryWarning = (id) => request.put(`/warehouse/expiry-warning/${id}/handle`)
export const manualScanExpiry = () => request.post('/warehouse/expiry-warning/scan')
